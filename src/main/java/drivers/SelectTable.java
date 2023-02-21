package drivers;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apps.Database;
import tables.SearchTable;
import tables.Table;

public class SelectTable implements Driver {
	
	static final Pattern pattern = Pattern.compile(
			"\\s*SELECT\\s(\\*|[a-z][a-z0-9_]*(?:\\s*AS\\s+[a-z][a-z0-9_]*)?(?:\\,\\s+[a-z][a-z0-9_]*(?:\\s*AS\\s+[a-z][a-z0-9_]*)?)*)\\s+FROM\\s+([a-z][a-z0-9_]*)\\s*(?:WHERE\\s+([a-z_1-9]+)\\s*(<=|>=|<>|=|<|>)\\s*([^()=<>]+))?",
			// "\\\\s*SELECT\\\\s(\\\\*|[a-z][a-z0-9_]*(?:\\\\s*AS\\\\s+[a-z][a-z0-9_]*)?(?:\\\\,\\\\s+[a-z][a-z0-9_]*(?:\\\\s*AS\\\\s+[a-z][a-z0-9_]*)?)*)\\\\s+FROM\\\\s+([a-z][a-z0-9_]*)\\\\s*(?:WHERE\\\\s+([a-z_1-9]+)\\\\s*(<=|>=|<>|=|<|>)\\\\s*([^()=<>]+))?",
			Pattern.CASE_INSENSITIVE);
	
	static final Pattern stringPattern = Pattern.compile("\"(?:.+)?\"", Pattern.CASE_INSENSITIVE);
	static final Pattern integerPattern = Pattern.compile("[0-9+-.]*", Pattern.CASE_INSENSITIVE);
	static final Pattern booleanPattern = Pattern.compile("(TRUE|FALSE)", Pattern.CASE_INSENSITIVE);
	static final Pattern nullPattern = Pattern.compile("NULL", Pattern.CASE_INSENSITIVE);

	@Override
	public Response execute(String query, Database db) {

		var matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) {
			return new Response(query, Status.UNRECOGNIZED, null, null);
		}

		// table name
		String columnName = matcher.group(1);
		String tableName = matcher.group(2);

		if (!db.exists(tableName)) {
			return new Response(query, Status.FAILED, "Table does not exist...", null);
		}
		if (tableName == null) {
			return new Response(query, Status.FAILED, "Table is null...", null);
		}

		Table sourceTable = db.find(tableName);
		List<String> columnNames = sourceTable.getColumnNames();
		List<String> columnTypes = sourceTable.getColumnTypes();
		int primaryIndex = sourceTable.getPrimaryIndex();

		List<Integer> pointer = new LinkedList<Integer>();
		List<String> pColNames = new LinkedList<String>();
		List<String> pColTypes = new LinkedList<String>();
		List<String> newNames = new LinkedList<String>();
		String primaryCol = " ";

		if (columnName.equals("*")) {
			for (int i = 0; i < columnNames.size(); i++) {
				pointer.add(i);
			}
			pColNames.addAll(columnNames);
			newNames.addAll(columnNames);
			pColTypes.addAll(columnTypes);
			primaryCol = columnNames.get(primaryIndex);

		} else {
			String[] column_name = columnName.split("\\s*,\\s*");
			if (column_name.length < 1 || column_name == null) {
				return new Response(query, Status.FAILED, "Column name not provided...", null);
			}
			String random;
			if (column_name.length >= 1) {
				for (int i = 0; i < column_name.length; i++) {
					String[] patternArray = column_name[i].split("\\s+");

					pColNames.add(i, patternArray[0]);

					if (patternArray.length == 3) {
						random = patternArray[2];
					} else {
						random = patternArray[0];
					}
					if (newNames.contains(random)) {
						return new Response(query, Status.FAILED, "Duplicate not allowed...", null);
					}

					newNames.add(i, random);
				}
				for (int j = 0; j < pColNames.size(); j++) {
					String l = pColNames.get(j);
					int k = columnNames.indexOf(l);

					if (k == -1) {
						return new Response(query, Status.FAILED, "Column index not provided...", null);
					}
					pointer.add(k);
					if (primaryIndex == k) {
						primaryCol = l;
					}
				}
				for (int a : pointer) {
					pColTypes.add(columnTypes.get(a));
				}
			}
		}

		int newIndex = pColNames.indexOf(primaryCol);
		Table resultTable = new SearchTable("_select", newNames, pColTypes, newIndex);
		if (newIndex < 0) {
			return new Response(query, Status.FAILED, "Primary index not initialized...", null);
		}
		String lhs = matcher.group(3);
		String operator = matcher.group(4);
		String rhs = matcher.group(5);

		if (lhs != null) {
			if (!columnNames.contains(lhs)) {
				return new Response(query, Status.FAILED, "Column does not exist...", null);
			}
			
			resultTable = helperMethod(lhs, rhs, columnNames, columnTypes, operator, sourceTable, pointer, resultTable);
			
			if (resultTable == null) {
				return new Response(query, Status.UNRECOGNIZED, "Table is null...", null);
			}
		} else {
			for (List<Object> row : sourceTable.rows()) {
				List<Object> newRow = new LinkedList<Object>();
				for (int i : pointer) {
					newRow.add(row.get(i));
				}
				resultTable.put(newRow);
			}
		}

		return new Response(query, Status.SUCCESSFUL, "Table found successfully", resultTable);
	}

	private static Table helperMethod(String lhs, String rhs, List<String> colNames, List<String> colTypes,
			String operator, Table source_table, List<Integer> pointer, Table result_table) {
		int leftIndex = colNames.indexOf(lhs);
		String leftType = colTypes.get(leftIndex);
		
		String rightType = "";

		Matcher stringMatcher = stringPattern.matcher(rhs.strip());
		Matcher integerMatcher = integerPattern.matcher(rhs.strip());
		Matcher booleanMatcher = booleanPattern.matcher(rhs.strip());
		Matcher nullMatcher = nullPattern.matcher(rhs.strip());

		boolean flag = true;
		for (List<Object> row : source_table.rows()) {
			if (operator != null) {
				if (stringMatcher.matches()) {
					rightType = "STRING";
					rhs = rhs.replaceAll("\"", "");
				} else if (integerMatcher.matches()) {
					rightType = "INTEGER";
				} else if (booleanMatcher.matches()) {
					rightType = "BOOLEAN";
				} else if (nullMatcher.matches()) {
					rightType = "NULL";
				}
				else {
					return null;
				}
			}

			Object lhsValue = row.get(leftIndex);
			if (leftType.equalsIgnoreCase(rightType)) {
				if (lhsValue == null || rhs == null) {
					flag = false;
				} else if (leftType.equalsIgnoreCase("STRING")) {
					flag = compareString(lhsValue.toString(), operator, rhs);
				} else if (leftType.equalsIgnoreCase("INTEGER")) {
					flag = compareInteger(Integer.parseInt(lhsValue.toString()), operator, Integer.parseInt(rhs));
				} else if (leftType.equalsIgnoreCase("BOOLEAN")) {
					flag = compareBoolean(lhsValue.toString(), operator, rhs);
				}
				if (flag == true) {
					List<Object> newRow = new LinkedList<>();
					for (int i = 0; i < pointer.size(); i++) {
						newRow.add(row.get(pointer.get(i)));
					}
					result_table.put(newRow);
				}
			}
		}
		return result_table;
	}
	

	private static boolean compareBoolean(String lhsString, String operator, String rhsString) {
		boolean flag = true;
		Boolean lhsValue = null;
		Boolean rhsValue = null;
		lhsValue = Boolean.parseBoolean(lhsString);
		if (rhsString.equalsIgnoreCase("TRUE")) {
			rhsValue = true;
		} else if (rhsString.equalsIgnoreCase("FALSE")) {
			rhsValue = false;
		}
		int result = lhsValue.compareTo(rhsValue);
		if (operator.equals("=")) {
			if (result != 0) {
				flag = false;
			}
		} else if (operator.equals("<")) {
			if (result >= 0) {
				flag = false;
			}
		} else if (operator.equals("<=")) {
			if (result > 0) {
				flag = false;
			}
		} else if (operator.equals(">")) {
			if (result <= 0) {
				flag = false;
			}
		} else if (operator.equals(">=")) {
			if (result < 0) {
				flag = false;
			}
		} else if (operator.equals("<>")) {
			if (result == 0) {
				flag = false;
			}
		}
		return flag;
	}

	private static boolean compareInteger(Integer lhsValue, String operator, Integer rhsValue) {
		boolean flag = true;
		int result = lhsValue.compareTo(rhsValue);
		if (operator.equals("=")) {
			if (result != 0) {
				flag = false;
			}
		} else if (operator.equals("<")) {
			if (result >= 0) {
				flag = false;
			}
		} else if (operator.equals("<=")) {
			if (result > 0) {
				flag = false;
			}
		} else if (operator.equals(">")) {
			if (result <= 0) {
				flag = false;
			}
		} else if (operator.equals(">=")) {
			if (result < 0) {
				flag = false;
			}
		} else if (operator.equals("<>")) {
			if (result == 0) {
				flag = false;
			}
		}
		return flag;
	}

	private static boolean compareString(String lhsValue, String operator, String rhsValue) {
		boolean flag = true;
		int result = lhsValue.compareTo(rhsValue);
		if (operator.equals("=")) {
			if (result != 0) {
				flag = false;
			}
		} else if (operator.equals("<")) {
			if (result >= 0) {
				flag = false;
			}
		} else if (operator.equals("<=")) {
			if (result > 0) {
				flag = false;
			}
		} else if (operator.equals(">")) {
			if (result <= 0) {
				flag = false;
			}
		} else if (operator.equals(">=")) {
			if (result < 0) {
				flag = false;
			}
		} else if (operator.equals("<>")) {
			if (result == 0) {
				flag = false;
			}
		}
		return flag;
	}
	
	//SELECT * FROM m6_table WHERE ps = asdf

}