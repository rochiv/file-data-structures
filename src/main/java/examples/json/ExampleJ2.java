package examples.json;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;

import tables.SearchTable;
import tables.Table;

public class ExampleJ2 {
	public static void main(String[] args) {
		write();

		Table table = read();
		System.out.println(table);
	}

	// Using JSON-P (JSON Processing API)

	public static void write() {
		try {
			String filename = Paths.get("data", "example_j2.json").toString();

			JsonArrayBuilder root_array_builder = Json.createArrayBuilder();

			JsonArrayBuilder row_builder = Json.createArrayBuilder();
			row_builder.add("alpha");
			row_builder.add(1);
			row_builder.add(true);
			root_array_builder.add(row_builder.build());

			row_builder = Json.createArrayBuilder();
			row_builder.add("beta");
			row_builder.add(2);
			row_builder.add(false);
			root_array_builder.add(row_builder.build());

			row_builder = Json.createArrayBuilder();
			row_builder.add("");
			row_builder.addNull();
			row_builder.addNull();
			root_array_builder.add(row_builder.build());

			JsonArray root_array = root_array_builder.build();

		    JsonWriterFactory factory = Json.createWriterFactory(Map.of(JsonGenerator.PRETTY_PRINTING, true));
		    JsonWriter writer = factory.createWriter(new FileOutputStream(filename));
		    writer.writeArray(root_array);
		    writer.close();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Table read() {
		try {
			String filename = Paths.get("data", "example_j2.json").toString();

			JsonReader reader = Json.createReader(new FileInputStream(filename));
		    JsonArray root_array = reader.readArray();
		    reader.close();

		    Table table = new SearchTable(
		    	"example_j2",
				List.of("letter", "order", "vowel"),
				List.of("string", "integer", "boolean"),
				0
		    );

	    	JsonArray row_array = root_array.getJsonArray(0);
		    table.put(List.of(
		    	row_array.getString(0),
		    	row_array.getInt(1),
		    	row_array.getBoolean(2)
		    ));

		    row_array = root_array.getJsonArray(1);
		    table.put(List.of(
		    	row_array.getString(0),
		    	row_array.getInt(1),
		    	row_array.getBoolean(2)
		    ));

		    row_array = root_array.getJsonArray(2);
		    table.put(Arrays.asList(
		    	row_array.isNull(0) ? null : row_array.getString(0),
		    	row_array.isNull(1) ? null : row_array.getInt(1),
		    	row_array.isNull(2) ? null : row_array.getBoolean(2)
		    ));

		    return table;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
