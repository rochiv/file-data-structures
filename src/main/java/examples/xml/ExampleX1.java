package examples.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tables.SearchTable;
import tables.Table;

public class ExampleX1 {
	public static void main(String[] args) {
		write();

		Table table = read();
		System.out.println(table);
	}

	// Using DOM (Document Object Model)

	public static void write() {
		try {
			String filename = Paths.get("data", "example_x1.xml").toString();

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

			Element root = doc.createElement("table");
			root.setAttribute("name", "example_x1");
			doc.appendChild(root);

				Element columns = doc.createElement("columns");
				columns.setAttribute("primary", "0");
				root.appendChild(columns);

					Element column = doc.createElement("column");
					column.setAttribute("name", "letter");
					column.setAttribute("type", "string");
					columns.appendChild(column);

					column = doc.createElement("column");
					column.setAttribute("name", "order");
					column.setAttribute("type", "integer");
					columns.appendChild(column);

					column = doc.createElement("column");
					column.setAttribute("name", "vowel");
					column.setAttribute("type", "boolean");
					columns.appendChild(column);

		    Source from = new DOMSource(doc);
		    Result to = new StreamResult(new FileWriter(filename));
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    transformer.transform(from, to);
		}
		catch (IOException | ParserConfigurationException | TransformerException e) {
			throw new RuntimeException(e);
		}
	}

	public static Table read() {
		try {
			String filename = Paths.get("data", "example_x1.xml").toString();

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(filename));

			Element root = doc.getDocumentElement();
			root.normalize();

			String table_name = root.getAttribute("name");

			List<String> column_names = new LinkedList<>();
			List<String> column_types = new LinkedList<>();

			Element columns_elem = (Element) root.getElementsByTagName("columns").item(0);

			NodeList column_nodes = columns_elem.getElementsByTagName("column");
			for (int i = 0; i < column_nodes.getLength(); i++) {
				Element column_elem = (Element) column_nodes.item(i);
				column_names.add(column_elem.getAttribute("name"));
				column_types.add(column_elem.getAttribute("type"));
			}

			int primary_index = Integer.parseInt(columns_elem.getAttribute("primary"));

			Table table = new SearchTable(
				table_name,
				column_names,
				column_types,
				primary_index
			);

			return table;
		}
		catch (IOException | ParserConfigurationException | SAXException e) {
			throw new RuntimeException(e);
		}
	}
}
