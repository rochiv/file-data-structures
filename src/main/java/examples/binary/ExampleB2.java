package examples.binary;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Demonstrates de/serialization in binary
 * for the state of a table, assuming:
 * <p>
 * Each record encodes a row,
 * disregarding nulls and tombstones.
 * <p>
 * Rows only contain non-null fields.
 */
public class ExampleB2 {
	private static FileChannel channel;

	private static final int MAX_STRING = 5;

	public static void main(String[] args) {
		try {
			channel = FileChannel.open(
				Paths.get("data", "example_b2.bin"),
				StandardOpenOption.CREATE,
				StandardOpenOption.READ,
				StandardOpenOption.WRITE
			);
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}

		Set<List<Object>> a = new LinkedHashSet<>();
		a.add(List.of("alpha", 1, true));
		a.add(List.of("beta", 2, false));
		a.add(List.of("gamma", 3, false));
		a.add(List.of("delta", 4, false));
		a.add(List.of("tau", 19, false));
		a.add(List.of("pi", 16, false));
		a.add(List.of("omega", 24, true));
		write(a);
		System.out.printf("Write: %s\n", a);

		Set<List<Object>> b = readAll();
		System.out.printf("Read:  %s\n", b);

		int index = 3;
		System.out.printf("Row %d: %s\n", index, read(index));

		System.out.printf("Equal by Value:     %s\n", a.equals(b));
		System.out.printf("Equal by Reference: %s\n", a == b);
	}

	public static void write(Set<List<Object>> rows) {
		try {
			final int rowCount = rows.size();

			MappedByteBuffer buf = channel.map(
				FileChannel.MapMode.READ_WRITE,
				0,
				(4) + (rowCount*(1 + MAX_STRING + 2 + 1))
			);

			buf.putInt(rowCount);

		    for (List<Object> row: rows) {
		    	final String letter = (String) row.get(0);
		    	final byte[] chars = letter.getBytes(StandardCharsets.UTF_8);
		    	buf.put((byte) chars.length);
		    	buf.put(chars);
//		    	buf.put(new byte[MAX_STRING - chars.length]);
		    	buf.position(buf.position() + MAX_STRING - chars.length);

		    	final short order = (short) ((int) row.get(1));
		    	buf.putShort(order);

		    	final boolean vowel = (boolean) row.get(2);
		    	buf.put(vowel ? (byte) 1 : 0);
		    }
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public static Set<List<Object>> readAll() {
		try {
			MappedByteBuffer buf = channel.map(
				FileChannel.MapMode.READ_ONLY,
				0,
				4
			);

			final int rowCount = buf.getInt();

			buf = channel.map(
				FileChannel.MapMode.READ_ONLY,
				4,
				rowCount*(1 + MAX_STRING + 2 + 1)
			);

			Set<List<Object>> rows = new LinkedHashSet<>();
			for (int i = 0; i < rowCount; i++) {
				final byte[] chars = new byte[buf.get()];
				buf.get(chars);
//		    	buf.get(new byte[MAX_STRING - chars.length]);
				buf.position(buf.position() + MAX_STRING - chars.length);
				final String letter = new String(chars, StandardCharsets.UTF_8);

				final int order = buf.getShort();

				final boolean vowel = buf.get() == 1;

				List<Object> row = List.of(letter, order, vowel);
				rows.add(row);
		    }
			return rows;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static List<Object> read(int index) {
		try {
			MappedByteBuffer buf = channel.map(
				FileChannel.MapMode.READ_ONLY,
				(4) + (index*(1 + MAX_STRING + 2 + 1)),
				(1 + MAX_STRING + 2 + 1)
			);

			final byte[] chars = new byte[buf.get()];
			buf.get(chars);
//		    buf.get(new byte[MAX_STRING - chars.length]);
			buf.position(buf.position() + MAX_STRING - chars.length);
			final String letter = new String(chars, StandardCharsets.UTF_8);

			final int order = buf.getShort();

			final boolean vowel = buf.get() == 1;

			List<Object> row = List.of(letter, order, vowel);
			return row;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
