package tables;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implements a hash-based table
 * using an array data structure.
 */

public class HashArrayTable extends Table {
	
	private Object[] array;		//this array stores the table
	private int size;		//the size variable keeps track of how many positions are populated in the array
	private int contamination;		//the contamination variable keeps track of the number of positions removed from the array

	private static final List<Object> TOMBSTONE = List.of();	
	
	/**
	 * Creates a table and initializes
	 * the data structure.
	 *
	 * @param tableName the table name
	 * @param columnNames the column names
	 * @param columnTypes the column types
	 * @param primaryIndex the primary index
	 */ 
	
	public HashArrayTable(String tableName, List<String> columnNames, List<String> columnTypes, Integer primaryIndex) {
		
		setTableName(tableName);
		setColumnNames(columnNames);
		setColumnTypes(columnTypes);
		setPrimaryIndex(primaryIndex);
		
		array = new Object[19];		//initializing the size of the array
		size = 0; 
		contamination = 0;
	}

	@Override
	public void clear() {
		array = new Object[19];		
		size = 0; 
		contamination = 0;
	}

	@Override
	public boolean put(List<Object> row) {
		
		Object key = row.get(getPrimaryIndex());	
		
		int hashValue = hashFunction(key);
		int position = hashValue % capacity();
		
		if (contains(key)) {
			List<Object> rowAtIndex = (List<Object>) array[position];
			if (rowAtIndex != null && !rowAtIndex.equals(TOMBSTONE) && rowAtIndex.get(getPrimaryIndex()).equals(key)) {  //how do i check if the key in position is the same as key in row??  List<Object> rowAtIndex = (List<Object>) array[pos]	
				array[position] = row;
			}
			else {
				position = colResAltSign(position, key);
				array[position] = row;
			}
			return true;
		}
		
		List<Object> rowAtIndex = (List<Object>) array[position];
		
		if (array[position] == null || rowAtIndex.equals(TOMBSTONE)) {
			array[position] = row;
			size = size + 1;	
			if (rowAtIndex != null && rowAtIndex.equals(TOMBSTONE)) {
				contamination--;
			}
			double loadFactor = (size + contamination)/(double) capacity();
			if (loadFactor > 0.75) {
				rehash();
			}
			return false;		//it is a miss
		}
		
		position = colResAltSign(position);
		rowAtIndex = (List<Object>) array[position];
		
		array[position] = row;
		size = size + 1;	
		if (rowAtIndex != null && rowAtIndex.equals(TOMBSTONE)) {
			contamination--;
		}
		double loadFactor = (size + contamination)/(double) capacity();
		if (loadFactor > 0.75) {
			rehash();
		}
		return false;		//it is a miss
	}

	@Override
	public boolean remove(Object key) {
		
		if (!contains(key))
			return false;
		
		int hashValue = this.hashFunction(key);
		int position = hashValue % capacity();
		
		List<Object> rowAtIndex = (List<Object>) array[position];
		
		
		
		if (!rowAtIndex.equals(TOMBSTONE) && rowAtIndex.get(getPrimaryIndex()).equals(key)) {
			array[position] = TOMBSTONE;
			size--;
			contamination++;
			return true;
		}
		position = colResAltSign(position, key);
		array[position] = TOMBSTONE;
		size--;
		contamination++;
		
		return true;
	}

	@Override
	public List<Object> get(Object key) {
		
		int hashValue = this.hashFunction(key);
		int position = hashValue % capacity();
		
		List<Object> rowAtIndex = (List<Object>) array[position];
		
		if (rowAtIndex != null && !rowAtIndex.equals(TOMBSTONE) && rowAtIndex.get(getPrimaryIndex()).equals(key)) {
			return rowAtIndex;
		}
		
		position = colResAltSign(position, key);
		
		if (position == -1) {
			return null;
		}
		
		rowAtIndex = (List<Object>) array[position];
		return rowAtIndex;
	}

	@Override
	public int size() {
		return size;		
	}

	@Override
	public int capacity() {	
		return array.length; //returns the length of the array, this is a constant and is complete
		
		
	}

	@Override
	public Iterator<List<Object>> iterator() {		
		return new Iterator<>() {
			int index = 0;
			@Override
			public boolean hasNext() {
				if (index >= capacity())
					return false;
				List<Object> row = (List<Object>) array[index];
				while (index < capacity() && (array[index] == null || row.equals(TOMBSTONE))) {
					index++;
					if (index >= capacity())
						return false;
					row = (List<Object>) array[index];
				}
				return index < capacity();
				
			}
			@Override
			public List<Object> next() {
				if (!hasNext())
				{
					throw new NoSuchElementException();
				}
				List<Object> row = (List<Object>) array[index];
				index++;
				return row;
				
			}
		};
	}
	
	private int hashFunction(Object key) {		
		//condition to check if the key is a null value 
		if (key == null) {
			throw new NullPointerException();
		}
		
		if (key instanceof String) {
			int hashValue = 0;
			
			char[] charKey = ((String) key).toCharArray();		//converting string key to a char array
			int charKeyLength = charKey.length;		//storing the length of the char array

			for (int i = 0; i < charKeyLength; i++) {
				int ascii = charKey[i];
				hashValue = hashValue + ((ascii*(i+1)));	
			}
			return hashValue;	
		}
		return key.hashCode();	
	}
	
	private int colResAltSign(int currentPos) {
		
		int jump = 0;
		int pos = currentPos;
		
		for (int iteration = 1; iteration <= capacity(); iteration++) {
			if (iteration % 2 == 1) {
				jump += iteration^2;
			}
			else if (iteration % 2 == 0) {
				jump -= iteration^2;
			}
			
			pos = Math.floorMod(pos + jump, capacity());
			
			List<Object> row = (List<Object>) array[pos];
			if (array[pos] == null || row.equals(TOMBSTONE)) {
				return pos;
			}
			
		}
		return -1;
	}
	
	private int colResAltSign(int currentPos, Object key) {
		int jump = 0;
		int pos = currentPos;
		
		for (int iteration = 1; iteration <= capacity(); iteration++) {
			if (iteration % 2 == 1) {
				//jump = iteration * iteration;
				jump += iteration^2;
			}
			else if (iteration % 2 == 0) {
				//jump = -(iteration * iteration);
				jump -= iteration^2;
			}
			
			pos = Math.floorMod(pos + jump, capacity());

			
			List<Object> rowAtIndex = (List<Object>) array[pos];
			if (rowAtIndex != null && !rowAtIndex.equals(TOMBSTONE) && rowAtIndex.get(getPrimaryIndex()).equals(key)) {
				return pos;
			}
		}
		return -1;
	}
	
	private void rehash() {
		
		int updCap = 2 * capacity();
		while (!(isPrime(updCap) && (updCap%4 == 3))) {
			updCap++;
		}
		Object[] tempArr = array;
		
		array = new Object[updCap];
		size = 0; 
		contamination = 0;
		
		for (int i = 0; i < tempArr.length; i++) {
			List<Object> rowAtIndex = (List<Object>) tempArr[i];
			if (rowAtIndex != null && !rowAtIndex.equals(TOMBSTONE)) {
				put(rowAtIndex);
			}	
		}	
	}
	
	private boolean isPrime(int num) {
        if (num <= 1) {
            return false;
        }
        for (int i = 2; i <= Math.sqrt(num); i++) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
	}
}
