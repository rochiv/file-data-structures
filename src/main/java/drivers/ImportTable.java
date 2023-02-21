package drivers;

import java.io.File;

import java.io.FileInputStream;

import java.io.IOException;

import java.nio.file.Paths;

import java.util.LinkedList;

import java.util.List;

import java.util.regex.Pattern;



import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.parsers.ParserConfigurationException;



import org.w3c.dom.Document;

import org.w3c.dom.Element;

import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;



import apps.Database;

import jakarta.json.Json;

import jakarta.json.JsonArray;

import jakarta.json.JsonObject;

import jakarta.json.JsonReader;

import tables.SearchTable;

import tables.Table;

public class ImportTable implements Driver {
	////IMPORT\s*((?:[a-z][a-z0-9_]*).(?:XML|JSON))\s*(TO\s*[a-z][a-zA-Z0-9_]*)?
	static final Pattern pattern = Pattern.compile(
			"IMPORT\\s+(([a-zA-Z][a-zA-Z0-9_]*\\.(json|xml)))\\s*(?:\\s*TO\\s+([a-zA-Z][a-zA-Z0-9_]*))?",
			Pattern.CASE_INSENSITIVE);
	// IMPORT\s+([a-zA-Z][a-zA-Z0-9_]*.(json|xml))\s+(?:\s*TO\s+[a-zA-Z][a-zA-Z0-9_]*)?

	@Override
	public Response execute(String query, Database db) {
		var matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) {
			return new Response(query, Status.UNRECOGNIZED, null, null);
		}
		String file = matcher.group(1);
		String fileName = matcher.group(2);
		String dataType = matcher.group(3);
		String tableName = matcher.group(4);
		
		if (tableName == null) {
			tableName = fileName; 
		}

		Table result = null;

		if (dataType.equalsIgnoreCase("xml")) {
			result = readXML(file);
		}

		if (dataType.equalsIgnoreCase("json")) {
			result = readJson(file);
		}

		int i = 1;
		if (db.exists(tableName)) {
			while (db.exists(tableName)) {
				String[] parts = tableName.split("_");
				tableName = parts[0] + "_" + i;
				i++;
				result.setTableName(tableName);
			}
		}
		else {
			result.setTableName(tableName);
		}
		

//		System.out.println(tableName);
		result.setTableName(tableName);
		db.create(result);
		

		return new Response(query, Status.SUCCESSFUL, 
				"Successfully imported " + result.rows().size() + " rows from " + file, result);
	}

	public static Table readXML(String fileName) {
		try {
			String file = Paths.get("data", fileName).toString();
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(file));
			Element root = doc.getDocumentElement();
			root.normalize();

			Element schema = (Element) root.getElementsByTagName("schema").item(0);
			String table_name = schema.getElementsByTagName("tableName").item(0).getTextContent();

			List<String> column_names = new LinkedList<>();
			List<String> column_types = new LinkedList<>();

			Element columns_elem = (Element) schema.getElementsByTagName("columns").item(0);

			NodeList column_nodes = columns_elem.getElementsByTagName("column");

			for (int i = 0; i < column_nodes.getLength(); i++) {
				Element column_elem = (Element) column_nodes.item(i);
				column_names.add(column_elem.getAttribute("name"));
				column_types.add(column_elem.getAttribute("type"));
			}

			int primary_index = Integer.parseInt(columns_elem.getAttribute("primary"));
			Table table = new SearchTable(table_name, column_names, column_types, primary_index);
			Element state = (Element) root.getElementsByTagName("state").item(0);

			NodeList row_nodes = state.getElementsByTagName("row");

			for (int i = 0; i < row_nodes.getLength(); i++) {
				Element row_elem = (Element) row_nodes.item(i);
				NodeList field_nodes = row_elem.getElementsByTagName("field");
				List<Object> row = new LinkedList<Object>();

				for (int j = 0; j < field_nodes.getLength(); j++) {
					
					String type = column_types.get(j);

					if (((Element) field_nodes.item(j)).hasAttribute("null")) {
						row.add(null);
					} 
					else if (type.equalsIgnoreCase("STRING")) {
						row.add(field_nodes.item(j).getTextContent());
					} 
					else if (type.equalsIgnoreCase("BOOLEAN")) {
						row.add(Boolean.parseBoolean(field_nodes.item(j).getTextContent()));
					}
					else if (type.equalsIgnoreCase("INTEGER")) {
						row.add(Integer.parseInt(field_nodes.item(j).getTextContent()));
					}
				}

				table.put(row);
			}

			return table;

		}
		catch (IOException | ParserConfigurationException | SAXException e) {
			throw new RuntimeException(e);
		}
	}

	public static Table readJson(String fileName) {
		try {
			String file = Paths.get("data", fileName).toString();
			JsonReader reader = Json.createReader(new FileInputStream(file));
			JsonObject root_object = reader.readObject();
			reader.close();

			JsonObject schema = root_object.getJsonObject("schema");
			String table_name = schema.getString("table_name");

			JsonArray column_names_array = schema.getJsonArray("column_names");

			List<String> column_names = new LinkedList<>();

			for (int i = 0; i < column_names_array.size(); i++) {
				column_names.add(column_names_array.getString(i));
			}

			JsonArray column_types_array = schema.getJsonArray("column_types");
			List<String> column_types = new LinkedList<>();

			for (int i = 0; i < column_types_array.size(); i++) {
				column_types.add(column_types_array.getString(i));
			}

			int primary_index = schema.getInt("primary_index");

			Table resultTable = new SearchTable(table_name, column_names, column_types, primary_index);

			JsonObject state = (JsonObject) root_object.get("state");
			JsonArray row = state.getJsonArray("row");

			for (int i = 0; i < state.size(); i++) {

				List<Object> rowType = new LinkedList<Object>();

				for (int j = 0; j < column_names.size(); j++) {
					String type = column_types.get(j);
					if (row.isNull(j)) {
						rowType.add(null);
					} 
					else if (type.equalsIgnoreCase("STRING")) {
						rowType.add(row.getString(j));

					} 
					else if (type.equalsIgnoreCase("BOOLEAN")) {
						rowType.add((row.getBoolean(j)));
					} 
					else if (type.equalsIgnoreCase("INTEGER")) {
						rowType.add((row.getInt(j)));
					}
				}
				resultTable.put(rowType);
			}

			return resultTable;

		} 
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

//package drivers;
//
//import java.util.regex.Pattern;
//
//import apps.Database;
//
//public class ImportTable implements Driver{
//	
//	//IMPORT\s*((?:[a-z][a-z0-9_]*).(?:XML|JSON))\s*(TO\s*[a-z][a-zA-Z0-9_]*)?
//	static final Pattern pattern = Pattern.compile(
//			"IMPORT\\s*((?:[a-z][a-z0-9_]*).(?:XML|JSON))\\s*(TO\\s*[a-z][a-zA-Z0-9_]*)?",
//			Pattern.CASE_INSENSITIVE);
//
//	@Override
//	public Response execute(String query, Database db) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
