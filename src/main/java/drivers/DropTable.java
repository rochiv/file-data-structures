package drivers;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import apps.Database;
import tables.SearchTable;
import tables.Table;

public class DropTable implements Driver {

	static final Pattern pattern = Pattern.compile(
			"DROP\\s+TABLE\\s+([a-z][a-z0-9_]*)",
			Pattern.CASE_INSENSITIVE
		);
	
	@Override
	public Response execute(String query, Database db) {
		var matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) {
			return new Response(query, Status.UNRECOGNIZED, null, null);
		}
		String table_name = matcher.group(1);

		if (!db.exists(matcher.group(1))) {
			return new Response(query, Status.FAILED, "Table name doesn't exist", null);
		}
		Table reference = db.find(table_name);


		db.drop(table_name);

		return new Response(query, Status.SUCCESSFUL,
				"table name and rows".formatted(table_name, reference.rows().size()), null);

	}

}
