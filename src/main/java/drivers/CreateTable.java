package drivers;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import apps.Database;
import tables.SearchTable;
import tables.Table;

public class CreateTable implements Driver{

	static final Pattern pattern = Pattern.compile(
			"CREATE\\s+TABLE\\s+([a-z][a-z0-9_]*)\\s+\\((\\s*[a-z][a-z0-9_]*\\s*(?:STRING|INTEGER|BOOLEAN)\\s*(?:PRIMARY)?(?:\\s*,\\s*[a-z][a-z0-9_]*\\s*(?:STRING|INTEGER|BOOLEAN)\\s*(?:PRIMARY)?)*\\s*)\\)",
			Pattern.CASE_INSENSITIVE
		);
	
	@Override
	public Response execute(String query, Database db) {
		var matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) {
			return new Response(query, Status.UNRECOGNIZED, null, null);
		}

		if (db.exists(matcher.group(1))) {
			return new Response(query, Status.FAILED, "Table <%s> already exists", null);

		}
		if (matcher.group(1).length() > 15) {
			return new Response(query, Status.UNRECOGNIZED, "Table exceeds the 15 character length limit", null);
		}


		String[] column_definition = matcher.group(2).strip().split("\\s*,\\s*");

		if (column_definition.length > 15) {
			return new Response(query, Status.UNRECOGNIZED, "Number of columns should not exceeed 15 characters", null);
		}

		List<String> column_names = new LinkedList<>();
		List<String> column_types = new LinkedList<>();
		int primary_index = -1;

		for (int i = 0; i < column_definition.length; i++) {

			String[] column = column_definition[i].split("\\s+");

			if (column[0].length() > 15) {
				return new Response(query, Status.FAILED, "Column name can not exceeed 15 characters", null);
			}

			if (column_names.contains(column[0])) {
				return new Response(query, Status.FAILED, "Column Name is a duplicate ", null);
			}
			column_names.add(i, column[0]);
			column_types.add(i, column[1].toLowerCase());

			if (column.length == 3) {
				if (primary_index > 0) {
					return new Response(query, Status.FAILED, "Primary index not found ", null);
				} else {
					primary_index = i;
				}
			}
		}

		if (primary_index < 0) {
			return new Response(query, Status.FAILED, "Column is invalid", null);
		}

		Table table_name = new SearchTable(matcher.group(1), column_names, column_types, primary_index);

		db.create(table_name);

		return new Response(query, Status.SUCCESSFUL, "Table created successfully", table_name);
	}

}
