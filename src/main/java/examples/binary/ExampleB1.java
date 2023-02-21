package examples.binary;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

import tables.SearchTable;
import tables.Table;

/**
 * Demonstrates de/serialization in binary
 * for the schema of a table, assuming:
 * <p>
 * The table name can be determined
 * from the file name.
 */
public class ExampleB1 {
	private static FileChannel channel;

	private static final int MAX_NAME = 6;

	public static void main(String[] args) {
		try {
			channel = FileChannel.open(
				Paths.get("data", "example_b1.bin"),
				StandardOpenOption.CREATE,
				StandardOpenOption.READ,
				StandardOpenOption.WRITE
			);
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}

		Table a = new SearchTable(
			"example_b1",
			List.of("letter", "order", "vowel"),
			List.of("string", "integer", "boolean"),
			0
		);
		write(a);
		System.out.printf("Write: %s\n", a);

		Table b = read();
		System.out.printf("Read:  %s\n", b);

		System.out.printf("Equal by Value:     %s\n", a.equals(b));
		System.out.printf("Equal by Reference: %s\n", a == b);
	}

	public static void write(Table table) {
		try {
			final int columnCount = table.getColumnNames().size();
			final int primaryIndex = table.getPrimaryIndex();

			MappedByteBuffer buf = channel.map(
				FileChannel.MapMode.READ_WRITE,
				0,
				(4 + 4) + (columnCount*(1 + MAX_NAME + 1))
			);

			buf.putInt(columnCount);
			buf.putInt(primaryIndex);

		    for (int i = 0; i < columnCount; i++) {
		    	final String name = table.getColumnNames().get(i);
		    	final byte[] chars = name.getBytes(StandardCharsets.UTF_8);
		    	buf.put((byte) chars.length);
		    	buf.put(chars);
//		    	buf.put(new byte[MAX_NAME - chars.length]);
		    	buf.position(buf.position() + MAX_NAME - chars.length);

		    	final String type = table.getColumnTypes().get(i);
		    	buf.put(switch (type) {
			    	case "string" -> (byte) 1;
			    	case "integer" -> (byte) 2;
			    	case "boolean" -> (byte) 3;
			    	default -> throw new IllegalArgumentException();
		    	});
		    }
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public static Table read() {
		try {
			MappedByteBuffer buf = channel.map(
				FileChannel.MapMode.READ_ONLY,
				0,
				4 + 4
			);

			final int columnCount = buf.getInt();
			final int primaryIndex = buf.getInt();

			buf = channel.map(
				FileChannel.MapMode.READ_ONLY,
				4 + 4,
				columnCount*(1 + MAX_NAME + 1)
			);

			String tableName = "example_b1";
			List<String> columnNames = new LinkedList<>();
			List<String> columnTypes = new LinkedList<>();
			for (int i = 0; i < columnCount; i++) {
				final byte[] chars = new byte[buf.get()];
				buf.get(chars);
//		    	buf.get(new byte[MAX_NAME - chars.length]);
				buf.position(buf.position() + MAX_NAME - chars.length);

				columnNames.add(new String(chars, StandardCharsets.UTF_8));
				columnTypes.add(switch (buf.get()) {
			    	case 1 -> "string";
			    	case 2 -> "integer";
			    	case 3 -> "boolean";
			    	default -> throw new IOException();
		    	});
		    }

			Table table = new SearchTable(
				tableName,
				columnNames,
				columnTypes,
				primaryIndex
			);
			return table;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
