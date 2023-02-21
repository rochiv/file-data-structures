package drivers;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apps.Database;
import tables.SearchTable;
import tables.Table;

/*
 * Examples:
 * 	 SQUARES BELOW 20
 * 	 SQUARES BELOW 30 AS a
 * 	 SQUARES BELOW 15 AS a, b
 *
 * Response 1:
 *   query: SQUARES BELOW 20
 *   successful
 *   message: "There were 5 results"
 * 	 result table:
 * 	   primary integer column "x", integer column "x_squared"
 *	   rows [0, 0]; [1, 1]; [2, 4]; [3, 9]; [4, 16]
 *
 * Response 2:
 *   query: SQUARES BELOW 30 AS a
 *   successful
 *   message: "There were 6 results"
 * 	 result table:
 * 	   primary integer column "a", integer column "a_squared"
 *	   rows [0, 0]; [1, 1]; [2, 4]; [3, 9]; [4, 16]; [5, 25]
 *
 * Response 3:
 *   query: SQUARES BELOW 15 AS a, b
 *   successful
 *   message: "There were 4 results"
 * 	 result table:
 * 	   primary integer column "a", integer column "b"
 *	   rows [0, 0]; [1, 1]; [2, 4]; [3, 9]
 */
@Deprecated
public class SquaresBelow implements Driver {
	static final Pattern pattern = Pattern.compile(
			//SQUARES\s+BELOW\s+([0-9]+)(?:\s+AS\s+(\w+)(?:\s*,\s*(\w+))?)?
			"SQUARES\\s+BELOW\\s+([0-9]+)(?:\\s+AS\\s+(\\w+)(?:\\s*,\\s*(\\w+))?)?",
			Pattern.CASE_INSENSITIVE
		);
	
	@Override
	public Response execute(String query, Database db) {
		/*
		 * TODO: For Lab 2 (optional),
		 * implement this driver.
		 */
		
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches())
			return new Response(query, Status.UNRECOGNIZED, null, null);
		
		
		int upper = Integer.parseInt(matcher.group(1));
		String base_name = matcher.group(2) != null ? matcher.group(2) : "x";
		String squared_name = matcher.group(3) != null ? matcher.group(3) : base_name + "_squared";
		
		if (matcher.group(2) != null && matcher.group(3) != null) {
			if (matcher.group(2).equals(matcher.group(3))) {
				return new Response(query, Status.FAILED, null, null);
			}
		}

		Table result_table = new SearchTable(
				"_squares",
				List.of(base_name, squared_name),
				List.of("integer","integer"),
				0
			);

			for (int i = 0; i < upper; i++) {
				List<Object> row = new LinkedList<>();
				
				if (!(i * i < upper)) {
					break;
				}
				
				row.add(i);
				row.add(i * i);
				result_table.put(row);
			}

			return new Response(query, Status.SUCCESSFUL, null, result_table);
	}
}
