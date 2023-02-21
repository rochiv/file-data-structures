package grade;

import static drivers.Status.*;

import org.junit.jupiter.api.BeforeAll;

public class Module6 extends SQLModule {
	@BeforeAll
	public static void setup() {
		module_tag = "M6";

		query_data = new Object[][]{
			// PREREQUISITES
			{ SUCCESSFUL, "m6_table", "CREATE TABLE m6_table (ps STRING PRIMARY, i INTEGER, b BOOLEAN); INSERT INTO m6_table VALUES (\"x\", null, true); INSERT INTO m6_table VALUES (\"xy\", 5, false); INSERT INTO m6_table VALUES (\"xx\", 4, true); INSERT INTO m6_table VALUES (\"y\", 1, null); INSERT INTO m6_table VALUES (\"yy\", 2, true); INSERT INTO m6_table VALUES (\"yx\", 3, false); INSERT INTO m6_table VALUES (\"z\", null, null); DUMP TABLE m6_table", "selection depends on table creation and insertion" },

			// STAR FORM
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table", "star form is allowed" },
			{ SUCCESSFUL, "_select", "select * from m6_table", "lowercase keywords are allowed" },
			{ UNRECOGNIZED, "_select", "SELECT FROM m6_table", "star or column definition is missing" },
			{ UNRECOGNIZED, "_select", "SELECT *, ps, i, b FROM m6_table", "star is not supported in a list of column names" },

			// LONG FORM
			{ SUCCESSFUL, "_select", "SELECT ps FROM m6_table", "a single column is allowed" },
			{ SUCCESSFUL, "_select", "SELECT ps, i, b FROM m6_table", "multiple columns are allowed" },
			{ SUCCESSFUL, "_select", "select ps, i, b from m6_table", "lowercase keywords are allowed" },
			{ FAILED, "_select", "SELECT PS FROM m6_table", "column and table names are case sensitive" },
			{ UNRECOGNIZED, "_select", "SELECT ps m6_table", "the FROM keyword is required" },
			{ UNRECOGNIZED, "_select", "ps FROM m6_table", "the SELECT keyword is required" },
			{ UNRECOGNIZED, "_select", "SELECT ps, i, b", "the FROM keyword and a table name are required" },
			{ UNRECOGNIZED, "_select", "SELECT ps i b FROM m6_table", "column definitions must be separated by commas" },

			// ALIASING
			{ SUCCESSFUL, "_select", "SELECT ps AS primary FROM m6_table", "aliasing is supported" },
			{ SUCCESSFUL, "_select", "SELECT ps AS primary, i, b FROM m6_table", "partial aliasing is supported" },
			{ SUCCESSFUL, "_select", "SELECT ps AS primary, i AS number, b AS flag FROM m6_table", "aliasing is supported" },
			{ SUCCESSFUL, "_select", "select ps as primary, i as number, b as flag from m6_table", "lowercase aliasing keywords are allowed" },
			{ SUCCESSFUL, "_select", "SELECT ps, i AS number, b AS flag FROM m6_table", "partial aliasing is supported" },

			// REORDERING
			{ SUCCESSFUL, "_select", "SELECT ps, b, i FROM m6_table", "reordering defined columns is allowed" },
			{ SUCCESSFUL, "_select", "SELECT i, b, ps FROM m6_table", "reordering defined columns is allowed" },
			{ SUCCESSFUL, "_select", "SELECT i AS number, b AS flag, ps AS primary FROM m6_table", "reordering defined columns with aliasing is allowed" },
			{ SUCCESSFUL, "_select", "SELECT i AS number, b AS flag, ps FROM m6_table", "reordering defined columns with partial aliasing is allowed" },

			// REPETITION
			{ SUCCESSFUL, "_select", "SELECT ps, i AS number_1, i AS number_2 FROM m6_table", "selecting repeated columns with unambiguous aliasing is allowed" },
			{ SUCCESSFUL, "_select", "SELECT ps, i, i AS i_copy FROM m6_table", "selecting repeated columns with unambiguous aliasing is allowed" },
			{ SUCCESSFUL, "_select", "SELECT ps, i AS i_copy, i FROM m6_table", "selecting repeated columns with unambiguous aliasing is allowed" },
			{ SUCCESSFUL, "_select", "SELECT b, b AS b_copy, ps FROM m6_table", "selecting repeated columns with unambiguous aliasing is allowed" },
			{ SUCCESSFUL, "_select", "SELECT b AS b_copy, b, ps FROM m6_table", "selecting repeated columns with unambiguous aliasing is allowed" },
			{ FAILED, "_select", "SELECT ps, i, i FROM m6_table", "selecting repeated columns without unambiguous aliasing is not allowed" },
			{ FAILED, "_select", "SELECT ps, b, i, b FROM m6_table", "selecting repeated columns without unambiguous aliasing is not allowed" },
			{ FAILED, "_select", "SELECT ps, ps FROM m6_table", "selecting repeated columns without unambiguous aliasing is not allowed" },
			{ FAILED, "_select", "SELECT ps AS primary, ps AS primary FROM m6_table", "selecting repeated columns without unambiguous aliasing is not allowed" },
			{ FAILED, "_select", "SELECT i, b, ps AS primary, ps, ps FROM m6_table", "selecting repeated columns without unambiguous aliasing is not allowed" },

			// CROSS-ALIASING
			{ SUCCESSFUL, "_select", "SELECT ps, i AS b, b AS i FROM m6_table", "cross-aliasing columns with unambiguous results is allowed" },
			{ SUCCESSFUL, "_select", "SELECT ps, i AS b, i AS i_copy, b AS i, b AS b_copy, ps AS ps_copy FROM m6_table", "cross-aliasing columns with unambiguous results is allowed" },
			{ SUCCESSFUL, "_select", "SELECT i AS b, b AS ps, ps AS i FROM m6_table", "cross-aliasing columns with unambiguous results is allowed" },

			// PRIMARY COLUMN
			{ SUCCESSFUL, "_select", "SELECT ps AS primary_1, ps AS primary_2 FROM m6_table", "selecting repeated primary column with unambiguous aliasing is allowed" },
			{ SUCCESSFUL, "_select", "SELECT ps AS primary_1, ps FROM m6_table", "selecting repeated primary column with unambiguous partial aliasing is allowed" },
			{ SUCCESSFUL, "_select", "SELECT ps, ps AS primary_2 FROM m6_table", "selecting repeated primary column with unambiguous partial aliasing is allowed" },
			{ FAILED, "_select", "SELECT i, b FROM m6_table", "the primary column must be selected" },

			// STRING COMPARISONS
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps = \"y\"", "strings must be compared in ascending lexicographic order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps <> \"y\"", "strings must be compared in ascending lexicographic order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps < \"y\"", "strings must be compared in ascending lexicographic order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps > \"y\"", "strings must be compared in ascending lexicographic order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps <= \"y\"", "strings must be compared in ascending lexicographic order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps >= \"y\"", "strings must be compared in ascending lexicographic order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps = \"\"", "strings must be compared in ascending lexicographic order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps <> \"\"", "strings must be compared in ascending lexicographic order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps < \"\"", "strings must be compared in ascending lexicographic order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps > \"\"", "strings must be compared in ascending lexicographic order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps <= \"\"", "strings must be compared in ascending lexicographic order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps >= \"\"", "strings must be compared in ascending lexicographic order" },

			// INTEGER COMPARISONS
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE i = 3", "integers must be compared in ascending numeric order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE i <> 3", "integers must be compared in ascending numeric order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE i < 3", "integers must be compared in ascending numeric order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE i > 3", "integers must be compared in ascending numeric order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE i <= 3", "integers must be compared in ascending numeric order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE i >= 3", "integers must be compared in ascending numeric order" },

			// BOOLEAN COMPARISONS
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE b = true", "booleans must be compared in ascending canonical order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE b <> true", "booleans must be compared in ascending canonical order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE b = false", "booleans must be compared in ascending canonical order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE b <> false", "booleans must be compared in ascending canonical order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE b < true", "booleans must be compared in ascending canonical order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE b > false", "booleans must be compared in ascending canonical order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE b <= true", "booleans must be compared in ascending canonical order" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE b >= false", "booleans must be compared in ascending canonical order" },

			// NULL COMPARISONS
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps = null", "null comparisons must be false" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps <> null", "null comparisons must be false" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE i = null", "null comparisons must be false" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE i <> null", "null comparisons must be false" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE b = null", "null comparisons must be false" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE b <> null", "null comparisons must be false" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps < null", "null comparisons must be false" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps > null", "null comparisons must be false" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps <= null", "null comparisons must be false" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps >= null", "null comparisons must be false" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE i < null", "null comparisons must be false" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE i > null", "null comparisons must be false" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE i <= null", "null comparisons must be false" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE i >= null", "null comparisons must be false" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE b < null", "null comparisons must be false" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE b > null", "null comparisons must be false" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE b <= null", "null comparisons must be false" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE b >= null", "null comparisons must be false" },

			// MIXED TYPES
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps = 3", "mixed types must be compared as strings" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps = true", "mixed types must be compared as strings" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE i = \"y\"", "mixed types must be compared as strings" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE i = true", "mixed types must be compared as strings" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE b = \"y\"", "mixed types must be compared as strings" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE b = 3", "mixed types must be compared as strings" },

			// WHERE CLAUSE
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table  WHERE  ps = \"y\"", "excess internal whitespace is allowed" },
			{ SUCCESSFUL, "_select", "SELECT * FROM m6_table WHERE ps=\"y\"", "whitespace is not required around operators" },
			{ SUCCESSFUL, "_select", "SELECT b, i, ps FROM m6_table WHERE ps <> \"z\"", "conditions support column reordering" },
			{ SUCCESSFUL, "_select", "SELECT ps AS s FROM m6_table WHERE ps <> \"z\"", "conditions support unaliased column names" },
			{ FAILED, "_select", "SELECT * FROM m6_table WHERE x = 3", "the column name must be valid" },
			{ UNRECOGNIZED, "_select", "SELECT * FROM m6_table  i = 3", "the WHERE keyword is required" },
			{ UNRECOGNIZED, "_select", "SELECT * FROM m6_table WHERE  = 3", "the column is required" },
			{ UNRECOGNIZED, "_select", "SELECT * FROM m6_table WHERE i = ", "the literal is required" },
			{ UNRECOGNIZED, "_select", "SELECT * FROM m6_table WHERE ps equals \"y\"", "the operator must be defined" },
			{ UNRECOGNIZED, "_select", "SELECT * FROM m6_table WHERE ps = asdf", "the literal type must be valid" },
			{ UNRECOGNIZED, "_select", "SELECT * FROM m6_tableWHEREps = \"y\"", "whitespace between keywords and names is required" },
		};

		serial_data = new Object[][]{
			{ "m6_table", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "z", null, null, "xx", 4, true, "x", null, true, "y", 1, null, "xy", 5, false, "yx", 3, false, "yy", 2, true },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "z", null, null, "xx", 4, true, "x", null, true, "y", 1, null, "xy", 5, false, "yx", 3, false, "yy", 2, true },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "z", null, null, "xx", 4, true, "x", null, true, "y", 1, null, "xy", 5, false, "yx", 3, false, "yy", 2, true },
			null,
			null,
			{ "_select", 1, 0, "ps", "string", "yy", "z", "y", "x", "yx", "xy", "xx" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "z", null, null, "xx", 4, true, "x", null, true, "y", 1, null, "xy", 5, false, "yx", 3, false, "yy", 2, true },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "z", null, null, "xx", 4, true, "x", null, true, "y", 1, null, "xy", 5, false, "yx", 3, false, "yy", 2, true },
			null,
			null,
			null,
			null,
			null,
			{ "_select", 1, 0, "primary", "string", "yy", "z", "y", "x", "yx", "xy", "xx" },
			{ "_select", 3, 0, "primary", "i", "b", "string", "integer", "boolean", "z", null, null, "xx", 4, true, "x", null, true, "y", 1, null, "xy", 5, false, "yx", 3, false, "yy", 2, true },
			{ "_select", 3, 0, "primary", "number", "flag", "string", "integer", "boolean", "z", null, null, "xx", 4, true, "x", null, true, "y", 1, null, "xy", 5, false, "yx", 3, false, "yy", 2, true },
			{ "_select", 3, 0, "primary", "number", "flag", "string", "integer", "boolean", "z", null, null, "xx", 4, true, "x", null, true, "y", 1, null, "xy", 5, false, "yx", 3, false, "yy", 2, true },
			{ "_select", 3, 0, "ps", "number", "flag", "string", "integer", "boolean", "z", null, null, "xx", 4, true, "x", null, true, "y", 1, null, "xy", 5, false, "yx", 3, false, "yy", 2, true },
			{ "_select", 3, 0, "ps", "b", "i", "string", "boolean", "integer", "z", null, null, "yy", true, 2, "x", true, null, "y", null, 1, "yx", false, 3, "xx", true, 4, "xy", false, 5 },
			{ "_select", 3, 2, "i", "b", "ps", "integer", "boolean", "string", null, true, "x", 1, null, "y", null, null, "z", 4, true, "xx", 2, true, "yy", 5, false, "xy", 3, false, "yx" },
			{ "_select", 3, 2, "number", "flag", "primary", "integer", "boolean", "string", null, true, "x", 1, null, "y", null, null, "z", 4, true, "xx", 2, true, "yy", 5, false, "xy", 3, false, "yx" },
			{ "_select", 3, 2, "number", "flag", "ps", "integer", "boolean", "string", null, true, "x", 1, null, "y", null, null, "z", 4, true, "xx", 2, true, "yy", 5, false, "xy", 3, false, "yx" },
			{ "_select", 3, 0, "ps", "number_1", "number_2", "string", "integer", "integer", "z", null, null, "yx", 3, 3, "x", null, null, "y", 1, 1, "xx", 4, 4, "yy", 2, 2, "xy", 5, 5 },
			{ "_select", 3, 0, "ps", "i", "i_copy", "string", "integer", "integer", "z", null, null, "yx", 3, 3, "x", null, null, "y", 1, 1, "xx", 4, 4, "yy", 2, 2, "xy", 5, 5 },
			{ "_select", 3, 0, "ps", "i_copy", "i", "string", "integer", "integer", "z", null, null, "yx", 3, 3, "x", null, null, "y", 1, 1, "xx", 4, 4, "yy", 2, 2, "xy", 5, 5 },
			{ "_select", 3, 2, "b", "b_copy", "ps", "boolean", "boolean", "string", true, true, "xx", false, false, "yx", true, true, "yy", null, null, "z", null, null, "y", true, true, "x", false, false, "xy" },
			{ "_select", 3, 2, "b_copy", "b", "ps", "boolean", "boolean", "string", true, true, "xx", false, false, "yx", true, true, "yy", null, null, "z", null, null, "y", true, true, "x", false, false, "xy" },
			null,
			null,
			null,
			null,
			null,
			{ "_select", 3, 0, "ps", "b", "i", "string", "integer", "boolean", "z", null, null, "xx", 4, true, "x", null, true, "y", 1, null, "xy", 5, false, "yx", 3, false, "yy", 2, true },
			{ "_select", 6, 0, "ps", "b", "i_copy", "i", "b_copy", "ps_copy", "string", "integer", "integer", "boolean", "boolean", "string", "xx", 4, 4, true, true, "xx", "yx", 3, 3, false, false, "yx", "z", null, null, null, null, "z", "xy", 5, 5, false, false, "xy", "yy", 2, 2, true, true, "yy", "x", null, null, true, true, "x", "y", 1, 1, null, null, "y" },
			{ "_select", 3, 2, "b", "ps", "i", "integer", "boolean", "string", null, true, "x", 1, null, "y", null, null, "z", 4, true, "xx", 2, true, "yy", 5, false, "xy", 3, false, "yx" },
			{ "_select", 2, 0, "primary_1", "primary_2", "string", "string", "y", "y", "xy", "xy", "x", "x", "yy", "yy", "xx", "xx", "yx", "yx", "z", "z" },
			{ "_select", 2, 0, "primary_1", "ps", "string", "string", "y", "y", "xy", "xy", "x", "x", "yy", "yy", "xx", "xx", "yx", "yx", "z", "z" },
			{ "_select", 2, 0, "ps", "primary_2", "string", "string", "y", "y", "xy", "xy", "x", "x", "yy", "yy", "xx", "xx", "yx", "yx", "z", "z" },
			null,
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "y", 1, null },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "x", null, true, "yx", 3, false, "yy", 2, true, "z", null, null, "xy", 5, false, "xx", 4, true },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "xx", 4, true, "xy", 5, false, "x", null, true },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "yx", 3, false, "yy", 2, true, "z", null, null },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "xx", 4, true, "xy", 5, false, "y", 1, null, "x", null, true },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "z", null, null, "yx", 3, false, "y", 1, null, "yy", 2, true },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "z", null, null, "xx", 4, true, "x", null, true, "y", 1, null, "xy", 5, false, "yx", 3, false, "yy", 2, true },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "z", null, null, "xx", 4, true, "x", null, true, "y", 1, null, "xy", 5, false, "yx", 3, false, "yy", 2, true },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "z", null, null, "xx", 4, true, "x", null, true, "y", 1, null, "xy", 5, false, "yx", 3, false, "yy", 2, true },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "yx", 3, false },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "xx", 4, true, "xy", 5, false, "y", 1, null, "yy", 2, true },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "y", 1, null, "yy", 2, true },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "xx", 4, true, "xy", 5, false },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "yx", 3, false, "yy", 2, true, "y", 1, null },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "yx", 3, false, "xx", 4, true, "xy", 5, false },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "yy", 2, true, "xx", 4, true, "x", null, true },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "xy", 5, false, "yx", 3, false },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "xy", 5, false, "yx", 3, false },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "yy", 2, true, "xx", 4, true, "x", null, true },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "xy", 5, false, "yx", 3, false },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "yy", 2, true, "xx", 4, true, "x", null, true },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "yx", 3, false, "x", null, true, "yy", 2, true, "xx", 4, true, "xy", 5, false },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "yx", 3, false, "x", null, true, "yy", 2, true, "xx", 4, true, "xy", 5, false },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean" },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "y", 1, null },
			{ "_select", 3, 0, "ps", "i", "b", "string", "integer", "boolean", "y", 1, null },
			{ "_select", 3, 2, "b", "i", "ps", "boolean", "integer", "string", null, 1, "y", true, null, "x", true, 2, "yy", false, 3, "yx", false, 5, "xy", true, 4, "xx" },
			{ "_select", 1, 0, "s", "string", "yy", "yx", "xx", "y", "xy", "x" },
			null,
			null,
			null,
			null,
			null,
			null,
			null
		};
	}
}