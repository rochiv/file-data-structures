package apps;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import tables.SearchTable;
import tables.Table;

/**
 * Sandbox for execution of arbitrary code
 * for testing or grading purposes.
 */
@Deprecated
public class Sandbox {
	public static void main(String[] args) {
		
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
		
		System.out.println(t1 + "\n");
		System.out.println("example_1 shows a basic table.\n\n");
		
		
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
		
		System.out.println(t2 + "\n");
		System.out.println("example_2 shows a table with a non-zero primary index.\n\n");
		
		Table t3 = new SearchTable(
				"example_3",
				List.of("letter", "order", "vowel"),
				List.of("string", "integer", "boolean"),
				1
			);
		
		System.out.println(t3 + "\n");
		System.out.println("example_3 shows a table with no state.\n\n");
		
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
		
		System.out.println(t4 + "\n");
		System.out.println("example_4 shows a basic table with null values.\n\n");
		
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
		
		System.out.println(t5 + "\n");
		System.out.println("example_5 shows a table with 5 columns, "
				+ "\nnon-zero primary index, and null values.\n\n");
		
		
	}
}
