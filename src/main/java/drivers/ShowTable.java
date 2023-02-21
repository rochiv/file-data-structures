package drivers;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import apps.Database;
import tables.SearchTable;
import tables.Table;

public class ShowTable implements Driver {
	
	static final Pattern pattern = Pattern.compile(
			"SHOW\\s+TABLES",
			Pattern.CASE_INSENSITIVE
		);
	
	
	@Override
	public Response execute(String query, Database db) {
		var matcher = pattern.matcher(query.strip());
		if (!matcher.matches())
			return new Response(query, Status.UNRECOGNIZED, null, null);

		Table _tables = new SearchTable(
				"_tables", 
				List.of("table_name", "column_count", "row_count"),
				List.of("string", "integer", "integer"), 0);

		for(Table i : db.tables()) {
			List<Object> row = new LinkedList<Object>();
			row.add(i.getTableName());
			row.add(i.getColumnNames().size());
			row.add(i.rows().size());
			_tables.put(row);
}
			
		return new Response(query, Status.SUCCESSFUL, "# of tables".formatted(db.tables().size()), _tables);
	}

}
