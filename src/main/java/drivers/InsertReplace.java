package drivers;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import apps.Database;
import drivers.Driver;
import drivers.Response;
import drivers.Status;
import tables.Table;

public class InsertReplace implements Driver {

	static final Pattern pattern = Pattern.compile(
	// (INSERT|REPLACE)\s+INTO\s+([a-z0-9]*_*[a-z0-9]*)\s+(?:\(([^()]*)\)\s+)?VALUES\s+(\(.*\)\,*\s*)*

	"(INSERT|REPLACE)\\s+INTO\\s+([a-z0-9]*_*[a-z0-9]*)\\s+(?:\\(([^()]*)\\)\\s+)?VALUES\\s+(\\(.*\\))",
	Pattern.CASE_INSENSITIVE);
	
	@Override
	public Response execute(String query, Database db) {
		var matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) {
			return new Response(query, Status.UNRECOGNIZED, null, null);
		}

		//matcher group 1; stores insert or replace 
		String insertOrReplace = matcher.group(1);
		//System.out.println(insertOrReplace);
		
		//matcher group 2; stores table name 
		String tableName = matcher.group(2);
		
		//matcher group 3 (optional); stores column names as a string, not separated  
		String colNames = matcher.group(3);
		//System.out.println(colNames);
		
		//matcher group 4; stores rows values as a string, not separated 
		String rowValues = matcher.group(4);
		//System.out.println(rowValues);

		
		//condition to check if the table exists 
		if (!db.exists(tableName)) {
			return new Response(query, Status.FAILED, "Table does not exist", null);
		}

		Table findTable = db.find(tableName);
		List<String> listColNames = findTable.getColumnNames();
		List<String> listColTypes = findTable.getColumnTypes();

		//checking if user entered column names
		if (colNames == null) {
			//if null, will use column names from findTable column names
			colNames = listColNames.toString();
		}

		String[] splitColNames = colNames.strip().split("\\s*,\\s*");

		if (colNames.length() == 0) {
			return new Response(query, Status.UNRECOGNIZED, "No data entered", null);
		}

		//sanitizing the data to remove the brackets 
		for (int i = 0; i < splitColNames.length; i++) {
			splitColNames[i] = splitColNames[i].replace(')', ' ');
			splitColNames[i] = splitColNames[i].replace('(', ' ');
			splitColNames[i] = splitColNames[i].replace(']', ' ');
			splitColNames[i] = splitColNames[i].replace('[', ' ');
			
			//removing leading and trailing white spaces 
			splitColNames[i] = splitColNames[i].strip();
		}

		//declaring a pointers list 
		List<Integer> pointersList = new LinkedList<>();
		
		//declaring a repeat list that checks for repeats or duplicates 
		List<String> repeat = new LinkedList<>();
		
		//flag to check if there is a primary index or not 
		Boolean primaryFlag = false;
		
		//stores the primary index
		int primaryIndex = 0;

		//for loop traversing through each element in splitColNames (user entered)
		for (int i = 0; i < splitColNames.length; i++) {
			
			//for loop traversing through each element in listColNames (from table class)
			for (int j = 0; j < listColNames.size(); j++) {
				
				//checking whether the column names user entered and the column names from the existing table are equal 
				if (splitColNames[i].equals(listColNames.get(j))) {
					
					if (repeat.contains(splitColNames[i])) {
						return new Response(query, Status.FAILED, "Column names are repeating...", null);
					} 
					else {
						repeat.add(listColNames.get(j));
						pointersList.add(j);
					}

					//finding primary index
					if (j == findTable.getPrimaryIndex()) {
						primaryIndex = j;
						primaryFlag = true;
					}
				}

			}
		}

		if (primaryFlag == false) {
			return new Response(query, Status.FAILED, "Primary index does not exist", null);
		}
		
		//we have a valid list of column names and the corresponding pointers list is also valid 

		
		//list of data from the query
		String[] rawData = rowValues.strip().replaceAll("\\(|\\)", "").split("\\s*,\\s*");

		
		if (rawData.length == 0) {
			return new Response(query, Status.FAILED, "Data not entered...", null);
		}
		
		else if (rawData.length > pointersList.size()) {
			return new Response(query, Status.FAILED, "Data greater than columns length...", null);
		} 
		
		else if (rawData.length < pointersList.size()) {
			return new Response(query, Status.FAILED, "Data lesser than columns length...", null);
		}

		List<Object> rows = new LinkedList<>();
		for (int i = 0; i < listColNames.size(); i++) {
			rows.add(null);
		}

		Object value = null;

		// for each raw value in pointers list
		for (int i = 0; i < rawData.length; i++) {
			
			
			int getPointer = pointersList.get(i);
			String expectedType = listColTypes.get(getPointer);
			int key = 0;

			if (findTable.getPrimaryIndex() == getPointer && rawData[i].equals("null")) {
				return new Response(query, Status.FAILED, "There is no primary index", null);
			} 
			
			else if (!rawData[i].equals(rawData[primaryIndex])) {
				key = 1;
			}

			//check for a string pattern 
			if (rawData[i].matches("\\\".*\\\"")) {

				rawData[i] = rawData[i].replaceAll("\\\"", "");

				//check for expected type
				if (!expectedType.equalsIgnoreCase("STRING")) {
					return new Response(query, Status.FAILED, "Type mismatch: expected type is String ", null);
				} 
				
				//check for length of string 
				else if (rawData[i].length() > 127) {
					return new Response(query, Status.UNRECOGNIZED, "String size is greater than limit", null);
				}
				
				else {
					//value has no quotation marks 
					value = rawData[i];
				}
			} 
			
			//check for an integer pattern 
			else if (rawData[i].matches("(?:\\+|\\-)*[\\d.]+")) {
				
				//check for expected type 
				if (!expectedType.equalsIgnoreCase("INTEGER")) {
					return new Response(query, Status.FAILED, "Type mismatch: expected type is Integer" , null);
				} 
				
				
				else {
					String temp = rawData[i].replaceAll("\\+*\\-*", "");

					if (temp.charAt(0) == '0' && temp.length() > 1) {
						return new Response(query, Status.UNRECOGNIZED, "There is no leading '0' ", null);
					} 
					else if (temp.toString().contains(".")) {
						return new Response(query, Status.UNRECOGNIZED, "There is no leading '.' ", null);
					} 
					else if (temp.length() > 32) {
						return new Response(query, Status.FAILED, "Integer size is greater than limit", null);
					}

					try {
						value = Integer.parseInt(rawData[i]);
					} 
					catch (NumberFormatException e) {
						return new Response(query, Status.FAILED, "Type mismatch: Expected type is int", null);
					}
				}
			} 
			
			//check for a boolean pattern
			else if (rawData[i].toUpperCase().matches("TRUE|FALSE")) {

				if (!expectedType.equalsIgnoreCase("BOOLEAN")) {
					return new Response(query, Status.FAILED, "Type mismatch: Expected type is Boolean", null);
				} 
				
				//parsing the raw boolean value
				else {
					value = Boolean.parseBoolean(rawData[i]);
				}
			} 
			
			//processed value that is neither String, int, boolean
			else {
				value = null;
			}

			
			//insert or replace 
			
			//replace
			if (insertOrReplace.equalsIgnoreCase("REPLACE")) {
				rows.set(getPointer, value);
			} 
			
			//insert
			else if (insertOrReplace.equalsIgnoreCase("INSERT")) {
				
				//check to see if primary key is unique 
				if (!findTable.keys().contains(rawData[primaryIndex])) {
					if (key == 0 && findTable.keys().contains(value)) {
						return new Response(query, Status.FAILED, "Primary column name is not unique", null);
					} 
					else {
						rows.set(getPointer, value);
					}
				} 
				else {
					return new Response(query, Status.FAILED, "Primary column name is not unique", null);
				}

			}

			rows.set(getPointer, value);
		}
		
		findTable.put(rows);
		db.find(tableName).put(rows);
		return new Response(query, Status.SUCCESSFUL, "Table was created!", db.find(tableName));
	}

}











