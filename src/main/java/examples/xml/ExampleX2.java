package examples.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
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

public class ExampleX2 {
	public static void main(String[] args) {
		write();

		Table table = read();
		System.out.println(table);
	}

	// Using DOM (Document Object Model)

	public static void write() {
		try {
			String filename = Paths.get("data", "example_x2.xml").toString();

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

			Element root = doc.createElement("table");
			doc.appendChild(root);

				Element row = doc.createElement("row");
				root.appendChild(row);

					Element field = doc.createElement("field");
					field.setTextContent("alpha");
					row.appendChild(field);

					field = doc.createElement("field");
					field.setTextContent(String.valueOf(1));
					row.appendChild(field);

					field = doc.createElement("field");
					field.setTextContent(String.valueOf(true));
					row.appendChild(field);

				row = doc.createElement("row");
				root.appendChild(row);

					field = doc.createElement("field");
					field.setTextContent("beta");
					row.appendChild(field);

					field = doc.createElement("field");
					field.setTextContent(String.valueOf(2));
					row.appendChild(field);

					field = doc.createElement("field");
					field.setTextContent(String.valueOf(false));
					row.appendChild(field);

				row = doc.createElement("row");
				root.appendChild(row);

					field = doc.createElement("field");
					field.setTextContent("");
					row.appendChild(field);

					field = doc.createElement("field");
					field.setAttribute("null", "yes");
					row.appendChild(field);

					field = doc.createElement("field");
					field.setAttribute("null", "yes");
					row.appendChild(field);

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
			String filename = Paths.get("data", "example_x2.xml").toString();

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(filename));

			Element root = doc.getDocumentElement();
			root.normalize();

			Table table = new SearchTable(
		    	"example_x2",
				List.of("letter", "order", "vowel"),
				List.of("string", "integer", "boolean"),
				0
			);

			NodeList row_nodes = root.getElementsByTagName("row");

			Element row_elem = (Element) row_nodes.item(0);
			NodeList field_nodes = row_elem.getElementsByTagName("field");
		    table.put(List.of(
		    	field_nodes.item(0).getTextContent(),
		    	Integer.parseInt(field_nodes.item(1).getTextContent()),
		    	Boolean.parseBoolean(field_nodes.item(2).getTextContent())
		    ));

			row_elem = (Element) row_nodes.item(1);
			field_nodes = row_elem.getElementsByTagName("field");
		    table.put(List.of(
		    	field_nodes.item(0).getTextContent(),
		    	Integer.parseInt(field_nodes.item(1).getTextContent()),
		    	Boolean.parseBoolean(field_nodes.item(2).getTextContent())
		    ));

			row_elem = (Element) row_nodes.item(2);
			field_nodes = row_elem.getElementsByTagName("field");
		    table.put(Arrays.asList(
		    	((Element) field_nodes.item(0)).hasAttribute("null") ? null : field_nodes.item(0).getTextContent(),
		    	((Element) field_nodes.item(1)).hasAttribute("null") ? null : Integer.parseInt(field_nodes.item(1).getTextContent()),
		    	((Element) field_nodes.item(2)).hasAttribute("null") ? null : Boolean.parseBoolean(field_nodes.item(2).getTextContent())
		    ));

			return table;
		}
		catch (IOException | ParserConfigurationException | SAXException e) {
			throw new RuntimeException(e);
		}
	}
}
