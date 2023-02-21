package examples.json;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;

public class ExampleJ3 {
	public static void main(String[] args) {
		write();

		System.out.println("DONE");
	}

	// Using JSON-P (JSON Processing API)

	public static void write() {
		try {
			String filename = Paths.get("data", "example_j3.json").toString();

			JsonObjectBuilder root_object_builder = Json.createObjectBuilder();

			JsonObjectBuilder schema_builder = Json.createObjectBuilder();
			schema_builder.add("table_name", "example_j3");
			schema_builder.addNull("column_names");
			schema_builder.addNull("column_types");
			schema_builder.addNull("primary_index");
			root_object_builder.add("schema", schema_builder.build());

			JsonArrayBuilder state_builder = Json.createArrayBuilder();
			state_builder.addNull();
			state_builder.addNull();
			state_builder.addNull();
			root_object_builder.add("state", state_builder.build());

			JsonObject root_object = root_object_builder.build();

			JsonWriterFactory factory = Json.createWriterFactory(Map.of(JsonGenerator.PRETTY_PRINTING, true));
			JsonWriter writer = factory.createWriter(new FileOutputStream(filename));
			writer.writeObject(root_object);
			writer.close();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
