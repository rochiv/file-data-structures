package grade;

import static drivers.Status.SUCCESSFUL;
import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import apps.Database;
import drivers.Response;
import drivers.Status;
import tables.Table;

public abstract class SQLModule {
	/**
	 * Whether to log the testing queries
	 * to a file at the root of the project.
	 * <p>
	 * The logged queries can be copied
	 * into an interpreter for manual execution.
	 * <p>
	 * You may reassign this when debugging.
	 */
	public static final boolean LOG_TO_FILE = true;

	protected static int passed;

	protected static String module_tag;
	protected static Object[][] query_data;
	protected static Object[][] serial_data;

	protected static Database subject_db;
	protected static Table subject_table;

	protected static PrintStream LOG_FILE;

	protected static Arguments[] data() {
		var arguments = new Arguments[query_data.length];

		for (var a = 0; a < arguments.length; a++) {
			String tableName = null;
			List<String> columnNames = null;
			List<String> columnTypes = null;
			Integer primaryIndex = null;
			Map<Object, List<Object>> rows = null;

			if (serial_data[a] != null) {
				var i = 0;

				tableName = (String) serial_data[a][i++];

				var columnCount = (Integer) serial_data[a][i++];

				primaryIndex = (Integer) serial_data[a][i++];

				columnNames = new LinkedList<>();
				for (var j = 1; j <= columnCount; j++)
					columnNames.add((String) serial_data[a][i++]);

				columnTypes = new LinkedList<>();
				for (var j = 1; j <= columnCount; j++)
					columnTypes.add((String) serial_data[a][i++]);

				rows = new HashMap<>();
				for (var j = i; j < serial_data[a].length; j += columnCount) {
					var row = new LinkedList<>();
					for (var k = 0; k < columnCount; k++)
						row.add(serial_data[a][j+k]);

					var key = row.get(primaryIndex);
					rows.put(key, row);
				}
			}

			arguments[a] = Arguments.of(
				query_data[a][0],
				query_data[a][1],
				query_data[a][2],
				query_data[a][3],
				tableName,
				columnNames,
				columnTypes,
				primaryIndex,
				rows
			);
		}

		return arguments;
	}

	@BeforeAll
	protected static final void initialize() throws IOException {
		try {
			subject_db = new Database(false);
		}
		catch (Exception e) {
			fail("Database constructor must not throw exceptions", e);
		}

		passed = 0;
	}

	@DisplayName("Queries")
	@ParameterizedTest(name = "[{index}] {2}")
	@MethodSource("data")
	protected void testQuery(
		Status status,
		String table_name,
		String sql,
		String purpose,
		String tableName,
		List<String> columnNames,
		List<String> columnTypes,
		Integer primaryIndex,
		Map<Object, List<Object>> rows
	) {
		logQuery(sql);

		var queries = Arrays.asList(sql.split("\\s*;\\s*"));

		List<Response> responses;
		try {
			responses = subject_db.interpret(queries);
		}
		catch (Exception e) {
			fail("Interpreter must not throw exceptions", e);
			return;
		}

		assertNotNull(
			responses,
			"Interpreter must respond with non-null list"
		);

		var count = queries.size();

		assertEquals(
			count,
			responses.stream().filter(it -> it != null).count(),
			"Interpreter must respond with one non-null response per query"
		);

		var last = responses.get(responses.size()-1);

		assertEquals(
			count == 1 ? status : Stream.concat(Stream.generate(() -> SUCCESSFUL).limit(count-1), Stream.of(status)).collect(Collectors.toList()),
			count == 1 ? last.status() : responses.stream().map(it -> it.status()).collect(Collectors.toList()),
			"expected %s to be %s, purpose of test: <%s>, message in response: <%s>".formatted(
				count == 1 ? "query" : "script",
				status.toString().toLowerCase(),
				purpose != null ? purpose : "none provided",
				last.message() != null ? last.message() : "none included"
			)
		);

		subject_table = null;

		String friendly_name = null;
		if (table_name != null) {
			if (table_name.startsWith("_")) {
				subject_table = last.table();
				friendly_name = "result table <%s>".formatted(table_name);
			}
			else {
				subject_table = subject_db.find(table_name);
				friendly_name = "table <%s> in the database".formatted(table_name);
			}
		}

		if (tableName != null) {
			assertNotNull(
				subject_table,
				"%s is null".formatted(friendly_name)
			);

			assertEquals(
				tableName,
				subject_table.getTableName(),
				"%s has incorrect table name in schema".formatted(friendly_name)
			);

			assertEquals(
				columnNames,
				subject_table.getColumnNames(),
				"%s has incorrect column names in schema".formatted(friendly_name)
			);

			assertEquals(
				columnTypes,
				subject_table.getColumnTypes(),
				"%s has incorrect column types in schema".formatted(friendly_name)
			);

			assertEquals(
				primaryIndex,
				subject_table.getPrimaryIndex(),
				"%s has incorrect primary index in schema".formatted(friendly_name)
			);

			for (var entry: rows.entrySet()) {
				var e_key = entry.getKey();
				var e_row = entry.getValue();

				if (!subject_table.contains(e_key))
					fail(
						"%s doesn't contain expected key <%s> with type <%s> in state".formatted(
							friendly_name,
							e_key,
							typeOf(e_key)
						)
					);

				var a_row = subject_table.get(e_key);

				assertEquals(
					typesOf(e_row),
					typesOf(a_row),
					"%s has unexpected types of row <%s> in state".formatted(
						friendly_name,
						a_row
					)
				);

				assertEquals(
					stringsOf(e_row),
					stringsOf(a_row),
					"%s has unexpected field values of row with key <%s> in state".formatted(
						friendly_name,
						e_key
					)
				);
			}

			for (var a_key: subject_table.keys()) {
				if (!rows.containsKey(a_key))
					fail(
						"%s contains unexpected key <%s> with type <%s> in state".formatted(
							friendly_name,
							a_key,
							typeOf(a_key)
						)
					);
			}
		}

		passed++;
	}

	@AfterAll
	protected static void report() throws IOException {
		System.out.printf(
			"[%s PASSED %d%% OF UNIT TESTS]\n",
			module_tag,
			(int) Math.ceil(passed / (double) query_data.length * 100)
		);

		try {
			subject_db.close();
		}
		catch (Exception e) {
			fail("Database close should not throw exceptions", e);
		}
	}

	private static final String typeOf(Object obj) {
		if (obj == null)
			return "null";

		return obj.getClass().getSimpleName().toLowerCase();
	}

	private static final List<String> typesOf(List<Object> list) {
		if (list == null)
			return null;

		return list.stream().map(v -> typeOf(v)).collect(Collectors.toList());
	}

	private static final List<String> stringsOf(List<Object> list) {
		if (list == null)
			return null;

		return list.stream().map(v -> String.valueOf(v)).collect(Collectors.toList());
	}

	protected static final void startLog() {
		if (LOG_TO_FILE) {
			try {
				LOG_FILE = new PrintStream("%s.log.sql".formatted(module_tag));
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		else {
			LOG_FILE = null;
		}
	}

	protected static final void logQuery(String line) {
		if (LOG_FILE == null)
			startLog();

		if (LOG_FILE != null)
			LOG_FILE.println("%s;".formatted(line));
	}
}