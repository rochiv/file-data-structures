package tables;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Implements a hash-based table using a random access file structure.
 */
public class HashFileTable extends Table {
	
	int size;
	int headerSize;
	int rowSize;
	
	FileChannel channel;
	MappedByteBuffer buf;
	
	private static final int MAX_STRING = 127;
	private static final int CAPACITY = 139;
	private static final short TOMBSTONE = (short) 0xFFFF;

	/**
	 * Creates a table and initializes the file structure.
	 *
	 * @param tableName    a table name
	 * @param columnNames  the column names
	 * @param columnTypes  the column types
	 * @param primaryIndex the primary index
	 */
	public HashFileTable(String tableName, List<String> columnNames, List<String> columnTypes, Integer primaryIndex) {
		
		setTableName(tableName);
		setColumnNames(columnNames);
		setColumnTypes(columnTypes);
		setPrimaryIndex(primaryIndex);
		
		File fileTableName = new File(tableName);

		if (fileTableName.exists()) {
			fileTableName.delete();
		}
		try {
			channel = FileChannel.open(
					Paths.get(tableName), 
					StandardOpenOption.CREATE, 
					StandardOpenOption.READ,
					StandardOpenOption.WRITE);
		} 
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		headerSize = headerSize(columnNames.size());
		rowSize = rowSize();

		try {
			buf = channel.map(FileChannel.MapMode.READ_WRITE, 
					0, 
					(headerSize) + (CAPACITY * rowSize));
		} 
		catch (IOException e) {
			e.printStackTrace();

		}
		writeHeader();
	}
	
	/**
	 * Reopens a table from an existing file structure.
	 *
	 * @param tableName a table name
	 * @throws IOException
	 */
	public HashFileTable(String tableName) throws IOException {
		File table_file = new File(tableName);

		if (!table_file.exists()) {
			throw new IOException("File doesn't exist");
		}
		try {
			channel = FileChannel.open(
					Paths.get(tableName), 
					StandardOpenOption.CREATE, 
					StandardOpenOption.READ,
					StandardOpenOption.WRITE);

		} 
		catch (IOException e) {
			e.printStackTrace();
			return;
		}

		readHeader();

		rowSize = rowSize();
		try {
			buf = channel.map(
					FileChannel.MapMode.READ_WRITE, 
					0, 
					(headerSize) + (CAPACITY * rowSize));

		} 
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	public int headerSize(int columnSize) {
		
		int header_size = 0;
		header_size += MAX_STRING + 1;
		header_size += 4;
		header_size += (MAX_STRING + 1) * columnSize;
		header_size += (MAX_STRING + 1) * columnSize;
		header_size += 4;
		header_size += 4;

		return header_size;
	}
	
	public int rowSize() {
		int total_size = 2;

		for (int i = 0; i < columnTypes.size(); i++) {
			if (columnTypes.get(i).equalsIgnoreCase("string")) {
				total_size += MAX_STRING + 1;
			} 
			else if (columnTypes.get(i).equalsIgnoreCase("integer")) {
				total_size += 4;
			} 
			else if (columnTypes.get(i).equalsIgnoreCase("boolean")) {
				total_size += 1;
			}

		}
		return total_size;
	}


	public void writeHeader() {
		
		buf.position(0);
		
		writeString(tableName);
		buf.putInt(columnNames.size());

		for (int i = 0; i < columnNames.size(); i++) {
			writeString(columnNames.get(i));
		}
		
		for(int i = 0; i < columnTypes.size(); i++) {
			writeString(columnTypes.get(i));
		}
		
		buf.putInt(primaryIndex);
		buf.putInt(size);
	}

	public void writeString(String s) {
		
		final byte[] character = s.getBytes(StandardCharsets.UTF_8);
		
		buf.put((byte) character.length);
		buf.put(character);
		buf.position(buf.position() + MAX_STRING - character.length);
	}
	

	public void writeNull(int i) {
		buf.position(headerSize + i * rowSize);

		buf.putShort((short) 0);

	}
	
	private void writeTombstone(int index) {
		buf.position(headerSize + index * rowSize);

		buf.putShort(TOMBSTONE);

	}
	
	private void writeRow(int index, List<Object> row) {

		buf.position(headerSize + index * rowSize);

		short mask = 9;
		for (int i = 0; i < row.size(); i++) {
			if (row.get(i) != null) {
				mask = (short) (mask | (1 << i));
			}
		}

		buf.putShort(mask);

		for (int i = 0; i < columnTypes.size(); i++) {
			if (columnTypes.get(i).equalsIgnoreCase("string")) {
				if (row.get(i) == null) {
					writeString("");
				} else {
					writeString((String) row.get(i));
				}
			} else if (columnTypes.get(i).equalsIgnoreCase("integer")) {
				if (row.get(i) == null) {
					buf.putInt(0);
				} else {
					buf.putInt((int) row.get(i));
				}
			} else if (columnTypes.get(i).equalsIgnoreCase("boolean")) {
				if (row.get(i) == null) {
					buf.get((byte) 0);
				} else {
					buf.put((boolean) row.get(i) ? (byte) 1 : 0);
				}
			}
		}

	}

	private void readHeader() {
		try {
			buf = channel.map(FileChannel.MapMode.READ_WRITE, 
					0, 
					(MAX_STRING + 1) + 4);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		buf.position(0);
		
		tableName = readString();
		int colSize = buf.getInt();

		headerSize = headerSize(colSize);

		try {
			buf = channel.map(FileChannel.MapMode.READ_WRITE, 0, (headerSize));
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		buf.position((MAX_STRING + 1) + 4);

		String[] columnNames = new String[colSize];

		for (int i = 0; i < colSize; i++) {
			columnNames[i] = readString();
		}
		
		setColumnNames(Arrays.asList(columnNames));

		String[] columnTypes = new String[colSize];
		for (int i = 0; i < colSize; i++) {
			columnTypes[i] = readString();
		}

		setColumnTypes(Arrays.asList(columnTypes));

		primaryIndex = buf.getInt();
		size = buf.getInt();

	}


	private List<Object> readRow(int index) {
		buf.position(headerSize + index * rowSize);

		short mask = buf.getShort();

		Object[] array = new Object[columnNames.size()];

		for (int i = 0; i < columnTypes.size(); i++) {
			Boolean isNull = ((mask & (1 << i)) == 0);

			if (columnTypes.get(i).equalsIgnoreCase("string")) {
				String s = readString();
				if (isNull) {
					array[i] = null;
				} else {
					array[i] = s;
				}
			} else if (columnTypes.get(i).equalsIgnoreCase("integer")) {
				int x = buf.getInt();
				if (isNull) {
					array[i] = null;
				} else {
					array[i] = x;
				}
			} else if (columnTypes.get(i).equalsIgnoreCase("boolean")) {
				Boolean b = buf.get() == 1;
				if (isNull) {
					array[i] = null;
				} else {
					array[i] = b;
				}
			}
		}

		return Arrays.asList(array);
	}

	public String readString() {
		final byte[] chars = new byte[buf.get()];

		buf.get(chars);
		buf.position(buf.position() + MAX_STRING - chars.length);
		final String l = new String(chars, StandardCharsets.UTF_8);

		return l;
	}
	


	private boolean isTombstone(int index) {
		buf.position(headerSize + index * rowSize);

		short mask = buf.getShort();

		return mask == TOMBSTONE;
	}

	private boolean isNull(int index) {
		buf.position(headerSize + index * rowSize);

		short mask = buf.getShort();

		return mask == 0;
	}
	
	@Override
	public void clear() {
		size = 0;
		for (int i = 0; i < CAPACITY; i++) {
			writeNull(i);
		}
	}


	@Override
	public boolean put(List<Object> row) {
		Object key = row.get(getPrimaryIndex());
		int h = hashFunction(key);
		int index = h % CAPACITY;
		if (index <= 0) {
			index = -index;
		}

		int firstTombstone = -1;
		Boolean result = false;
		for (int i = 0; i < CAPACITY; i++) {
			int step = i * i;
			if (i % 2 == 0) {
				step = -step;
			}

			int calc = index + step;
			calc = calc % CAPACITY;
			if (calc < 0) {
				calc = calc + CAPACITY;
			}
			if (isNull(calc)) {
				if (firstTombstone != -1) {
					writeRow(firstTombstone, row);
				} else {
					writeRow(calc, row);
				}
				size++;
				writeHeader();
				break;
			} else if (isTombstone(calc)) {
				if (firstTombstone == -1) {
					firstTombstone = calc;
				}
			} else {
				List<Object> full_row = (List<Object>) readRow(calc);
				Object current_key = full_row.get(getPrimaryIndex());
				if (key.equals(current_key)) {
					writeRow(calc, row);
					result = true;
					break;
				}
			}
		}
		return result;
	}

	@Override
	public boolean remove(Object key) {
		int h = hashFunction(key);
		int index = h % CAPACITY;
		if (index <= 0) {
			index = -index;
		}
		for (int i = 0; i < CAPACITY; i++) {
			int step = i * i;
			if (i % 2 == 0) {
				step = -step;
			}

			int calc = step + index;
			calc = calc % CAPACITY;
			if (calc < 0) {
				calc = calc + CAPACITY;
			}
			if (isNull(calc)) {
				return false;
			} else if (!isTombstone(calc)) {
				List<Object> curr = (List<Object>) readRow(calc);
				Object currkey = curr.get(getPrimaryIndex());
				if (key.equals(currkey)) {
					writeTombstone(calc);
					size--;
					writeHeader();
					return true;
				}
			}
		}

		return false;
	}

	

	@Override
	public List<Object> get(Object key) {
		int h = hashFunction(key);
		int index = h % CAPACITY;
		if (index <= 0) {
			index = -index;
		}
		for (int i = 0; i < CAPACITY; i++) {
			int step = i * i;
			if (i % 2 == 0) {
				step = -step;
			}
			int calc = index + step;
			calc = calc % CAPACITY;
			if (calc < 0) {
				calc = calc + CAPACITY;
			}
			if (isNull(calc)) {
				return null;
			} else if (!isTombstone(calc)) {
				List<Object> curr = (List<Object>) readRow(calc);
				Object currKey = curr.get(getPrimaryIndex());
				if (key.equals(currKey)) {
					return curr;
				}
			}
		}
		return null;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public int capacity() {
		return CAPACITY;
	}

	private int hashFunction(Object key) {
		if (key instanceof String) {
			String str = (String) key;
			int hexaDec = 0x2a62f;

			for (int i = 0; i < str.length(); i++) {
				int a = str.charAt(i);
				hexaDec = (hexaDec ^ a << 6) | (hexaDec >> 10) | (hexaDec ^ a << 8) | (hexaDec >> 12) & 0x2a62f;
			}
			return hexaDec;
		} else {
			return key.hashCode();
		}
	}

	@Override
	public Iterator<List<Object>> iterator() {
		return new Iterator<>() {
			int index = 0;

			public boolean hasNext() {
				for (int i = index; i < CAPACITY; i++) {
					if (!isNull(i) && !isTombstone(i)) {
						return true;
					}
				}
				return false;
			}
			
//			public boolean hasNext() {
//				
//				if (index >= capacity()) {
//					return false;
//				}
//				
//				while (index < capacity() && (isRowNull(index) || isRowTombstone(index)) ) {
//					index++;
//					if (index >= capacity()) {
//						return false;
//					}
//				}
//				return index < capacity();
//			}

			public List<Object> next() {
				for (int i = index; i < CAPACITY; i++) {
					if (!isNull(i) && !isTombstone(i)) {
						index = i + 1;
						return (List<Object>) readRow(i);
					}
				}
				return null;
			}
		};
	}
}