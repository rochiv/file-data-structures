package grade;

import static drivers.Status.*;

import org.junit.jupiter.api.BeforeAll;

public class Module4 extends SQLModule {
	@BeforeAll
	public static void setup() {
		module_tag = "M4";

		query_data = new Object[][]{
			// BASICS
			{ SUCCESSFUL, "m4_table01", "CREATE TABLE m4_table01 (id INTEGER PRIMARY, name STRING, flag BOOLEAN)", null },
			{ FAILED, "m4_table01", "CREATE TABLE m4_table01 (id INTEGER PRIMARY, name STRING, flag BOOLEAN)", "the table name must not already be in use" },

			// CASE, WHITESPACE
			{ SUCCESSFUL, "m4_table02", "create table m4_table02 (ID integer primary, NAME string, flag BOOLEAN)", "lowercase keywords and uppercase table names are allowed" },
			{ SUCCESSFUL, "m4_table03", " CREATE TABLE m4_table03 (id INTEGER PRIMARY, name STRING, flag BOOLEAN) ", "untrimmed whitespace is allowed" },
			{ SUCCESSFUL, "m4_table04", "CREATE  TABLE  m4_table04  (id INTEGER PRIMARY, name STRING, flag BOOLEAN)", "excess internal whitespace is allowed" },
			{ SUCCESSFUL, "m4_table05", "CREATE TABLE m4_table05 ( id INTEGER PRIMARY , name STRING , flag BOOLEAN )", "excess internal whitespace is allowed" },
			{ SUCCESSFUL, "m4_table06", "CREATE TABLE m4_table06 (id INTEGER PRIMARY,name STRING,flag BOOLEAN)", "whitespace around punctuation is not required" },
			{ UNRECOGNIZED, "m4_table07", "CREATETABLE m4_table07 (id INTEGERPRIMARY, name STRING, flag BOOLEAN)", "whitespace between keywords is required " },
			{ UNRECOGNIZED, "m4_table08", "CREATE TABLEm4_table08 (idINTEGER PRIMARY, nameSTRING, flag BOOLEAN)", "whitespace between keywords and names is required" },

			// NAMES, KEYWORDS, PUNCTUATION
			{ SUCCESSFUL, "t", "CREATE TABLE t (i INTEGER PRIMARY, n STRING, f BOOLEAN)", "names can be 1 letter" },
			{ SUCCESSFUL, "m4_table10_____", "CREATE TABLE m4_table10_____ (n23456789012345 INTEGER PRIMARY)", "names can be up to 15 characters" },
			{ UNRECOGNIZED, "m4_table11______", "CREATE TABLE m4_table11______ (n234567890123456 INTEGER PRIMARY)", "names can be no more than 15 characters" },
			{ UNRECOGNIZED, "1m_table12", "CREATE TABLE 1m_table12 (2id INTEGER PRIMARY, 3name STRING, 4flag BOOLEAN)", "a name cannot start with a number" },
			{ UNRECOGNIZED, "_m1table13", "CREATE TABLE _m1table13 (_id INTEGER PRIMARY, _name STRING, _flag BOOLEAN)", "a name cannot start with an underscore" },
			{ UNRECOGNIZED, "", "CREATE TABLE (id INTEGER PRIMARY, name STRING, flag BOOLEAN)", "the table name cannot be omitted" },
			{ UNRECOGNIZED, "m4_table15", "CREATE m4_table15 (id INTEGER PRIMARY, name STRING, flag BOOLEAN)", "the TABLE keyword is required" },
			{ UNRECOGNIZED, "m4_table16", "CREATE TABLE m4_table16 (id INTEGER PRIMARY name STRING flag BOOLEAN)", "the commas between definitions are required" },
			{ UNRECOGNIZED, "m4_table17", "CREATE TABLE m4_table17 id INTEGER PRIMARY, name STRING, flag BOOLEAN", "the parentheses are required" },

			// COLUMNS, PRIMARY
			{ SUCCESSFUL, "m4_table18", "CREATE TABLE m4_table18 (c1 INTEGER PRIMARY)", "a single column is allowed" },
			{ SUCCESSFUL, "m4_table19", "CREATE TABLE m4_table19 (c1 INTEGER PRIMARY, c2 STRING)", "two columns are allowed" },
			{ SUCCESSFUL, "m4_table20", "CREATE TABLE m4_table20 (c1 INTEGER PRIMARY, c2 INTEGER, c3 INTEGER, c4 INTEGER, c5 INTEGER, c6 INTEGER, c7 INTEGER, c8 INTEGER, c9 INTEGER, c10 INTEGER, c11 INTEGER, c12 INTEGER, c13 INTEGER, c14 INTEGER, c15 INTEGER)", "up to 15 columns are allowed" },
			{ SUCCESSFUL, "m4_table21", "CREATE TABLE m4_table21 (id INTEGER, name STRING PRIMARY, flag BOOLEAN)", "the primary column need not be the first" },
			{ FAILED, "m4_table22", "CREATE TABLE m4_table22 (id INTEGER PRIMARY, other STRING, other BOOLEAN)", "the column names cannot have duplicates" },
			{ FAILED, "m4_table23", "CREATE TABLE m4_table23 (id INTEGER, name STRING, flag BOOLEAN)", "there must be a primary column" },
			{ FAILED, "m4_table24", "CREATE TABLE m4_table24 (id INTEGER PRIMARY, name STRING PRIMARY, flag BOOLEAN PRIMARY)", "there can be only one primary column" },
			{ UNRECOGNIZED, "m4_table25", "CREATE TABLE m4_table25 ()", "there must be at least one column" },
			{ UNRECOGNIZED, "m4_table26", "CREATE TABLE m4_table26 (c1 INTEGER PRIMARY, c2 INTEGER, c3 INTEGER, c4 INTEGER, c5 INTEGER, c6 INTEGER, c7 INTEGER, c8 INTEGER, c9 INTEGER, c10 INTEGER, c11 INTEGER, c12 INTEGER, c13 INTEGER, c14 INTEGER, c15 INTEGER, c16 INTEGER)", "more than 15 columns are not allowed" },

			// DROP TABLE
			{ SUCCESSFUL, "m4_table01", "DROP TABLE m4_table01", null },
			{ FAILED, "m4_table01", "DROP TABLE m4_table01", "previously dropped table cannot be dropped again" },
			{ SUCCESSFUL, "m4_table01", "CREATE TABLE m4_table01 (ps STRING PRIMARY)", "previously dropped table name can be reused" },
			{ SUCCESSFUL, "m4_table02", "drop table m4_table02", "lowercase keywords and uppercase table names are allowed" },
			{ SUCCESSFUL, "m4_table03", " DROP TABLE m4_table03 ", "untrimmed whitespace is allowed" },
			{ SUCCESSFUL, "m4_table04", "DROP  TABLE  m4_table04", "excess internal whitespace is allowed" },
			{ SUCCESSFUL, "t", "DROP TABLE t", "names can be a single letter" },
			{ UNRECOGNIZED, "m4_table05", "DROPTABLE m4_table05", "whitespace between keywords is required " },
			{ UNRECOGNIZED, "m4_table06", "DROP TABLEm4_table06", "whitespace between keywords and names is required" },
			{ UNRECOGNIZED, "m4_table17", "DROP m4_table17", "the TABLE keyword is required" },
			{ UNRECOGNIZED, "", "DROP TABLE", "the table name cannot be omitted" },

			// SHOW TABLES
			{ SUCCESSFUL, "_tables", "SHOW TABLES", "upper case keywords are allowed" },
			{ SUCCESSFUL, "_tables", "show tables", "lower case keywords are allowed" },
			{ SUCCESSFUL, "_tables", "ShOw tAbLeS", "mixed case keywords are allowed" },
			{ SUCCESSFUL, "_tables", "  SHOW  TABLES  ", "excess internal whitespace and untrimmed whitespace is allowed" },
			{ UNRECOGNIZED, "_tables", "SHOWTABLES", "whitespace between keywords is required " },
			{ UNRECOGNIZED, "_tables", "SHOW", "the TABLES keyword is required" },
			{ UNRECOGNIZED, "_tables", "TABLES", "the SHOW keyword is required" },

			// INTERPRETER
			{ SUCCESSFUL, null, "ECHO \"Hello, world!\"", "the ECHO query must be supported" },
			{ SUCCESSFUL, "_range", "RANGE 5", "the RANGE query must be supported" },
			{ SUCCESSFUL, "m4_table01", "DUMP TABLE m4_table01", "the DUMP TABLE query must be supported" },
			{ SUCCESSFUL, "_range", "ECHO \"Goodbye, world!\"; RANGE 100; DUMP TABLE m4_table05", "multiple semicolon-delimited queries are allowed" },
			{ UNRECOGNIZED, null, "A MALFORMED QUERY", "a malformed query should be unrecognized" },
		};

		serial_data = new Object[][]{
			{ "m4_table01", 3, 0, "id", "name", "flag", "integer", "string", "boolean" },
			{ "m4_table01", 3, 0, "id", "name", "flag", "integer", "string", "boolean" },
			{ "m4_table02", 3, 0, "ID", "NAME", "flag", "integer", "string", "boolean" },
			{ "m4_table03", 3, 0, "id", "name", "flag", "integer", "string", "boolean" },
			{ "m4_table04", 3, 0, "id", "name", "flag", "integer", "string", "boolean" },
			{ "m4_table05", 3, 0, "id", "name", "flag", "integer", "string", "boolean" },
			{ "m4_table06", 3, 0, "id", "name", "flag", "integer", "string", "boolean" },
			null,
			null,
			{ "t", 3, 0, "i", "n", "f", "integer", "string", "boolean" },
			{ "m4_table10_____", 1, 0, "n23456789012345", "integer" },
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			{ "m4_table18", 1, 0, "c1", "integer" },
			{ "m4_table19", 2, 0, "c1", "c2", "integer", "string" },
			{ "m4_table20", 15, 0, "c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9", "c10", "c11", "c12", "c13", "c14", "c15", "integer", "integer", "integer", "integer", "integer", "integer", "integer", "integer", "integer", "integer", "integer", "integer", "integer", "integer", "integer" },
			{ "m4_table21", 3, 1, "id", "name", "flag", "integer", "string", "boolean" },
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			{ "m4_table01", 1, 0, "ps", "string" },
			null,
			null,
			null,
			null,
			{ "m4_table05", 3, 0, "id", "name", "flag", "integer", "string", "boolean" },
			{ "m4_table06", 3, 0, "id", "name", "flag", "integer", "string", "boolean" },
			null,
			null,
			{ "_tables", 3, 0, "table_name", "column_count", "row_count", "string", "integer", "integer", "m4_table10_____", 1, 0, "m4_table06", 3, 0, "m4_table05", 3, 0, "m4_table01", 1, 0, "m4_table20", 15, 0, "m4_table21", 3, 0, "m4_table19", 2, 0, "m4_table18", 1, 0 },
			{ "_tables", 3, 0, "table_name", "column_count", "row_count", "string", "integer", "integer", "m4_table10_____", 1, 0, "m4_table06", 3, 0, "m4_table05", 3, 0, "m4_table01", 1, 0, "m4_table20", 15, 0, "m4_table21", 3, 0, "m4_table19", 2, 0, "m4_table18", 1, 0 },
			{ "_tables", 3, 0, "table_name", "column_count", "row_count", "string", "integer", "integer", "m4_table10_____", 1, 0, "m4_table06", 3, 0, "m4_table05", 3, 0, "m4_table01", 1, 0, "m4_table20", 15, 0, "m4_table21", 3, 0, "m4_table19", 2, 0, "m4_table18", 1, 0 },
			{ "_tables", 3, 0, "table_name", "column_count", "row_count", "string", "integer", "integer", "m4_table10_____", 1, 0, "m4_table06", 3, 0, "m4_table05", 3, 0, "m4_table01", 1, 0, "m4_table20", 15, 0, "m4_table21", 3, 0, "m4_table19", 2, 0, "m4_table18", 1, 0 },
			null,
			null,
			null,
			null,
			{ "_range", 1, 0, "number", "integer", 4, 3, 2, 1, 0 },
			{ "m4_table01", 1, 0, "ps", "string" },
			{ "m4_table05", 3, 0, "id", "name", "flag", "integer", "string", "boolean" },
			null
		};
	}
}