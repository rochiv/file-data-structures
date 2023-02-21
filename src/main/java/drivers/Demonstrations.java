package drivers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apps.Database;
import tables.SearchTable;
import tables.Table;

public class Demonstrations implements Driver{

	static final Pattern pattern = Pattern.compile(
			"DEMO\\s+(\\d+)",
			Pattern.CASE_INSENSITIVE
			);

	@Override
	public Response execute(String query, Database db) {
				
		var matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) {
			return new Response(query, Status.UNRECOGNIZED, null, null);
		}
		
		var demo = Integer.parseInt(matcher.group(1));
		
		if (demo == 1) {
			Table t1 = new SearchTable(
					"example_1",
					List.of("letter", "order", "vowel"),
					List.of("string", "integer", "boolean"),
					0
				);
			
			t1.put(Arrays.asList("alpha", 1, true));
			t1.put(Arrays.asList("beta", 2, false));
			t1.put(Arrays.asList("gamma", 3, false));
			t1.put(Arrays.asList("delta", 4, false));
			t1.put(Arrays.asList("tau", 19, false));
			t1.put(Arrays.asList("pi", 2, false));
			t1.put(Arrays.asList("omega", 24, true));
			
			System.out.println("example_1 shows a basic table.\n\n");

			return new Response(query, Status.SUCCESSFUL, "Demonstration %d".formatted(demo), t1);
		}
		
		if (demo == 2) {
			Table t2 = new SearchTable(
					"example_2",
					List.of("letter", "order", "vowel"),
					List.of("string", "integer", "boolean"),
					1
				);
			
			t2.put(Arrays.asList("alpha", 1, true));
			t2.put(Arrays.asList("beta", 2, false));
			t2.put(Arrays.asList("gamma", 3, false));
			t2.put(Arrays.asList("delta", 4, false));
			t2.put(Arrays.asList("tau", 19, false));
			t2.put(Arrays.asList("pi", 2, false));
			t2.put(Arrays.asList("omega", 24, true));
			
			System.out.println("example_2 shows a table with a non-zero primary index.\n\n");
		
			return new Response(query, Status.SUCCESSFUL, "Demonstration %d".formatted(demo), t2);

		}
		
		if (demo == 3) {
			Table t3 = new SearchTable(
					"example_3",
					List.of("letter", "order", "vowel"),
					List.of("string", "integer", "boolean"),
					1
				);
			
			System.out.println("example_3 shows a table with no state.\n\n");
			
			return new Response(query, Status.SUCCESSFUL, "Demonstration %d".formatted(demo), t3);

		}
		
		if (demo == 4) {
			Table t4 = new SearchTable(
					"example_4",
					List.of("letter", "order", "vowel"),
					List.of("string", "integer", "boolean"),
					0
				);
			
			t4.put(Arrays.asList("alpha", 1, true));
			t4.put(Arrays.asList("beta", 2, null));
			t4.put(Arrays.asList("gamma", null, false));
			t4.put(Arrays.asList("delta", 4, false));
			t4.put(Arrays.asList("tau", 19, null));
			t4.put(Arrays.asList("pi", 2, false));
			t4.put(Arrays.asList("omega", null, true));
			
			System.out.println("example_4 shows a basic table with null values.\n\n");

			return new Response(query, Status.SUCCESSFUL, "Demonstration %d".formatted(demo), t4);

		}
		
		if (demo == 5) {
			Table t5 = new SearchTable(
					"example_1",
					List.of("letter", "order", "vowel", "capital", "even"),
					List.of("string", "integer", "boolean", "string", "integer"),
					3
				);
			
			t5.put(Arrays.asList("alpha", 1, true, "A", 2));
			t5.put(Arrays.asList("beta", 2, false, "B", null));
			t5.put(Arrays.asList("gamma super long", 3, false, "C", 6));
			t5.put(Arrays.asList(null, 4, false, "D", 8));
			t5.put(Arrays.asList("tau", 19, false, "E", null));
			t5.put(Arrays.asList("pi", 2, false, "F", 12));
			t5.put(Arrays.asList(null, 24, true, "G", 14));
			
			System.out.println("example_5 shows a table with 5 columns, "
					+ "\nnon-zero primary index, and null values.\n\n");
			
			return new Response(query, Status.SUCCESSFUL, "Demonstration %d".formatted(demo), t5);
			
		}
		else {
			return new Response(query, Status.FAILED, "Unkown Demonstration", null);
		}
	}
	
}
