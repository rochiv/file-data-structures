package drivers;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import apps.Database;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;
import tables.Table;

public class ExportTable implements Driver {

	//EXPORT\s*([a-z][a-zA-Z0-9_]*)\s*(?:(?:TO\s*((?:[a-z][a-z0-9_]*).(?:XML|JSON))\s*)|(?:AS\s*(XML|JSON)))*
	//EXPORT\s+([a-zA-Z][a-zA-Z0-9_]*)+\s+(TO+\s+([a-zA-Z][a-zA-Z0-9_]*).(xml|json)|AS\s+(XML|JSON))
	
	static final Pattern pattern = Pattern.compile(
			"EXPORT\\s*([a-z][a-zA-Z0-9_]*)\\s*(?:(?:TO\\s*((?:[a-z][a-z0-9_]*).(XML|JSON))\\s*)|(?:AS\\s*(XML|JSON)))",
			Pattern.CASE_INSENSITIVE);

	@Override
	public Response execute(String query, Database db) {
		var matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) {
			return new Response(query, Status.UNRECOGNIZED, null, null);
		}

		String tableName = matcher.group(1);
		String fileName = matcher.group(2);
		String dataType1 = matcher.group(3);
		String dataType2 = matcher.group(4);

		if (!db.exists(tableName)) {
			return new Response(query, Status.FAILED, "No table", null);
		}

		Table table = db.find(tableName);
		
		if (dataType2 != null) {
			if (dataType2.equalsIgnoreCase("json")) {

				writeJson(tableName + "." + dataType2, table);

			}

			if (dataType2.equalsIgnoreCase("xml")) {

				writeXML(tableName + "." + dataType2, table);

			}
		}
		
		if (fileName != null) {
			if (dataType1.equalsIgnoreCase("json")) {

				writeJson(fileName, table);

			}

			if (dataType1.equalsIgnoreCase("xml")) {

				writeXML(fileName, table);

			}
		}
		
		Driver select = new SelectTable();
		
		Response res = select.execute("SELECT * FROM " + tableName, db);
		
		Table newTable = res.table(); 
		
		newTable.setTableName("_exported");


		return new Response(query, Status.SUCCESSFUL, 
				"Successfully exported " + newTable.rows().size() + " rows from " + tableName + " ", newTable);
		 
	}

	// Using JSON-P (JSON Processing API)

	public static void writeJson(String fileName, Table table) {
		try {
			String file = Paths.get("data", fileName).toString();
			
			JsonObjectBuilder root_object_builder = Json.createObjectBuilder();
			JsonObjectBuilder schema_builder = Json.createObjectBuilder();
			
			schema_builder.add("table_name", table.getTableName());
			
			JsonArrayBuilder column_names_builder = Json.createArrayBuilder();
			
			List<String> colNames = table.getColumnNames();

			for (int i = 0; i < colNames.size(); i++) {
				column_names_builder.add(colNames.get(i));
			}

			schema_builder.add("column_names", column_names_builder.build());

			JsonArrayBuilder column_types_builder = Json.createArrayBuilder();

			List<String> colTypes = table.getColumnTypes();

			for (int i = 0; i < colTypes.size(); i++) {
				column_types_builder.add(colTypes.get(i));
			}

			schema_builder.add("column_types", column_types_builder.build());

			schema_builder.add("primary_index", table.getPrimaryIndex());

			root_object_builder.add("schema", schema_builder);

			JsonObjectBuilder state_builder = Json.createObjectBuilder();

			for (List<Object> row : table.rows()) {

				JsonArrayBuilder row_builder = Json.createArrayBuilder();

				for (int i = 0; i < row.size(); i++) {
					
					String type = colTypes.get(i);

					if (row.get(i) == null) {
						row_builder.addNull();
					} 
					else if (type.equalsIgnoreCase("STRING")) {
						row_builder.add(row.get(i).toString());
					} 
					else if (type.equalsIgnoreCase("BOOLEAN")) {
						row_builder.add((Boolean) row.get(i));
					} 
					else if (type.equalsIgnoreCase("INTEGER")) {
						row_builder.add((Integer) row.get(i));
					}
				}
				state_builder.add("row", row_builder);
			}

			root_object_builder.add("state", state_builder.build());

			JsonObject root_object = root_object_builder.build();

			JsonWriterFactory factory = Json.createWriterFactory(Map.of(JsonGenerator.PRETTY_PRINTING, true));
			JsonWriter writer = factory.createWriter(new FileOutputStream(file));

			writer.writeObject(root_object);
			writer.close();

		} 
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// Using DOM (Document Object Model)

	public static void writeXML(String fileName, Table table) {
		try {
			String file = Paths.get("data", fileName).toString();
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

			Element root = doc.createElement("table");
			doc.appendChild(root);

			Element schema = doc.createElement("schema");
			root.appendChild(schema);

			Element tableName = doc.createElement("tableName");
			tableName.setTextContent(table.getTableName());

			schema.appendChild(tableName);
			Element columnNames = doc.createElement("columns");
			columnNames.setAttribute("primary", String.valueOf(table.getPrimaryIndex()));

			List<String> colNames = table.getColumnNames();
			List<String> colTypes = table.getColumnTypes();

			for (int i = 0; i < colNames.size(); i++) {
				Element column = doc.createElement("column");
				column.setAttribute("name", colNames.get(i));
				column.setAttribute("type", colTypes.get(i));
				columnNames.appendChild(column);
			}

			schema.appendChild(columnNames);

			Element state = doc.createElement("state");

			root.appendChild(state);

			for (List<Object> row : table.rows()) {
				Element elementRow = doc.createElement("row");
				state.appendChild(elementRow);

				for (int i = 0; i < row.size(); i++) {
					Element field = doc.createElement("field");
					field.setTextContent(String.valueOf(row.get(i)));
					elementRow.appendChild(field);
				}
			}

			Source from = new DOMSource(doc);
			Result to = new StreamResult(new FileWriter(file));

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(from, to);

		}
		catch (IOException | ParserConfigurationException | TransformerException e) {
			throw new RuntimeException(e);
		}
	}
}