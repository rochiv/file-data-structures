package apps;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import drivers.Response;

/**
 * Implements a user console for
 * interacting with a database.
 * <p>
 * Do not modify existing protocols,
 * but you may add new protocols.
 */
public class Console {
	/**
	 * The entry point for execution
	 * with user input/output.
	 *
	 * @param args unused.
	 */
	public static void main(String[] args) {
		try (
			final Database db = new Database(true);
			final Scanner in = new Scanner(System.in);
			final PrintStream out = System.out;
		)
		{
			boolean exitFlag = false; 
			
			while (!exitFlag)
			{
				out.print("Input>> ");

				String text = in.nextLine();
				
				if (text.equalsIgnoreCase("exit")) {  //this is a sentinel 
					exitFlag = true;
					break;
				}
				
				String[] textArray = text.split(";");  //splitting by ';' 
				
				List<String> textList = Arrays.asList(textArray);  //converting to list 
				
				List<String> queries = new LinkedList<>();
				
				queries.addAll(textList);  //adding textList values to queries

				List<Response> responses = db.interpret(queries);
				
				for (int i = 0; i < queries.size(); i++) {
					out.println("Query:   " + responses.get(i).query());
					out.println("Status:  " + responses.get(i).status());
					out.println("Message: " + responses.get(i).message());
					out.println("Table:   " + responses.get(i).table() + "\n");
				}
			
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
