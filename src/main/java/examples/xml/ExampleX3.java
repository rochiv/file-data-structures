package examples.xml;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

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

public class ExampleX3 {
	public static void main(String[] args) {
		write();

		System.out.println("DONE");
	}

	// Using DOM (Document Object Model)

	public static void write() {
		try {
			String filename = Paths.get("data", "example_x3.xml").toString();

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

			Element root = doc.createElement("table");
			doc.appendChild(root);

				Element schema = doc.createElement("schema");
				root.appendChild(schema);

					Element tableName = doc.createElement("tableName");
					tableName.setTextContent("example_x3");
					schema.appendChild(tableName);

					Element columnNames = doc.createElement("columnNames");
					schema.appendChild(columnNames);

					Element columnTypes = doc.createElement("columnTypes");
					schema.appendChild(columnTypes);

					Element primaryIndex = doc.createElement("primaryIndex");
					schema.appendChild(primaryIndex);

				Element state = doc.createElement("state");
				root.appendChild(state);

					Element row = doc.createElement("row");
					state.appendChild(row);

					row = doc.createElement("row");
					state.appendChild(row);

					row = doc.createElement("row");
					state.appendChild(row);

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
}
