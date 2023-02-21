package apps;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import drivers.CreateTable;
import drivers.Demonstrations;
import drivers.Driver;
import drivers.DropTable;
import drivers.DumpTable;
import drivers.Echo;
import drivers.ExportTable;
import drivers.ImportTable;
import drivers.Range;
import drivers.Response;
import drivers.SelectTable;
import drivers.ShowTable;
import drivers.SquaresBelow;
import drivers.Status;
import tables.Table;
import drivers.InsertReplace;

/**
 * This class implements a
 * database management system.
 * <p>
 * Do not modify existing protocols,
 * but you may add new protocols.
 */
public class Database implements Closeable {
	private final List<Table> tables;
	private final List<Driver> drivers;
	private final boolean persistent;

	/**
	 * Initializes the drivers and the tables.
	 *
	 * @param persistent whether the database is persistent.
	 */
	public Database(boolean persistent) {
		this.persistent = persistent;

		if (isPersistent()) {
			tables = new LinkedList<>();
		} else {
			tables = new LinkedList<>();
		}
		
		drivers = List.of(
				new Echo(),
				new Range(),
				new DumpTable(), 
				new Demonstrations(), 
				new SquaresBelow(), 
				new CreateTable(), 
				new DropTable(), 
				new ShowTable(), 
				new InsertReplace(),
				new SelectTable(), 
				new ExportTable(), 
				new ImportTable()
			);
		
	}

	/**
	 * Returns whether the database is persistent.
	 *
	 * @return whether the database is persistent.
	 */
	public boolean isPersistent() {
		return persistent;
	}

	/**
	 * Returns an unmodifiable list of the tables in the database.
	 *
	 * @return the list of tables.
	 */
	public List<Table> tables() {

		return List.copyOf(tables);
	}

	/**
	 * Returns the table with the given name, or <code>null</code> if there is none.
	 *
	 * @param tableName a table name.
	 * @return the corresponding table, if any.
	 */
	public Table find(String tableName) {
		if (tableName == null) {
			throw new NullPointerException("Table Name is not valid");
		}
		for (int i = 0; i < tables.size(); i++) {
			if (tables().get(i).getTableName().equals(tableName)) {
//				System.out.println("sdfsdfsdf");
				return tables.get(i);
			}

		}
		return null;
	}

	/**
	 * Returns <code>true</code> if a table with the given name exists or
	 * <code>false</code> otherwise.
	 *
	 * @param tableName a table name.
	 * @return whether the corresponding table exists.
	 */
	public boolean exists(String tableName) {
		//change if errors persist
		return find(tableName) != null;
	}

	/**
	 * Creates the given table, unless a table with the corresponding name exists.
	 * <p>
	 * Returns <code>true</code> if created or <code>false</code> otherwise.
	 *
	 * @return whether the table was created.
	 */
	public boolean create(Table table) {
			
			if (table == null) { //guard condition
				//return false;
				throw new NullPointerException("Table is not valid");
			}
			
			if (exists(table.getTableName())) {
				return false;
			}
			else {
				tables.add(table);
				return true;
			}
		}

	/**
	 * Drops the table with the given name, unless no table with the given name
	 * exists.
	 * <p>
	 * Returns <code>true</code> if dropped or <code>false</code> otherwise.
	 *
	 * @return whether the table was dropped.
	 */
	public boolean drop(String tableName) {
			
			if (tables() == null) { 
				throw new NullPointerException("Table is not valid");
			}
			
			if (!exists(tableName)) {
				return false;
			}
			else {
				tables.remove(find(tableName));
				return true;
			}
		}

	/**
	 * Interprets a list of queries and returns a list of responses to each in
	 * sequence.
	 *
	 * @param queries the list of queries.
	 * @return the list of responses.
	 */
	@SuppressWarnings("unlikely-arg-type")
	public List<Response> interpret(List<String> queries) {
		List<Response> responses = new LinkedList<>();
		
		queryLoop:
		for (int i = 0; i < queries.size(); i++) {
			
			driverLoop:
			for (int j = 0; j < drivers.size(); j++) {
				Response res = drivers.get(j).execute(queries.get(i), this);
				
				if (res.status() == Status.SUCCESSFUL || res.status() == Status.FAILED) {  //check if this is correct 
					responses.add(res);
					continue queryLoop;
				}		
				else if (res.equals(Status.UNRECOGNIZED)) {
					continue driverLoop;
				}
			}
			//check if the fall back response is correct 
			Response fallBackRes = new Response(queries.get(i), Status.UNRECOGNIZED , "There is no driver for this query", null);
			responses.add(fallBackRes);
		}

		return responses;
	}

	/**
	 * Executes any required tasks when the database is closed.
	 */
	@Override
	public void close() throws IOException {

	}
}


