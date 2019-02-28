package csi311;


import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.derby.jdbc.EmbeddedDriver;


/*
 * A demonstration of using JDBC style interaction between a Java app and a relational DB, 
 * in this case, Apache Derby used as an in-memory RDBMS server.  
 */
public class SqlDemo {

	// The URL to the DB server and specific DB on that server.  Notice we're not naming 
	// a server name - its going to be in-memory.  JDBC protocol.  "csi311-testdb1" will be 
	// the name of the database - the file on the filesystem Derby will use to store our database.
	private static final String DB_URL = "jdbc:derby:csi311-testdb1;create=true";
	
	// A Connection to the DB server, and a Statement object we use to issue SQL commands.
	// My guess is something about the scope of these variables will offend the better 
	// sensibilities of our esteemed TA... 8-)
    private Connection conn = null;
    private Statement stmt = null;
	
    // Constructor.  No op.
	public SqlDemo() {
	}
	
	
	// Will be called by our main().  The major parts of this app are initiated from here.
    public void run() throws Exception {
    	// Make connection to the DB server (in this case, a Derby in-memory server).
        createConnection();
        // Make our table, if not already made.
        createTable();
        // Insert some data
        insertRestaurant("LaVals", "Berkeley");
        insertRestaurant("Canalis", "Rotterdam NY");
        // Produce a report
        selectRestaurants();
        // Shutdown the DB server cleanly
        shutdown();
    }
    
    
    // Make a connection to the DB.  In this case the Derby in-memory server.
    private void createConnection() {
        try {
        	// Driver (specific implementation of the JDBC interface) for "Embedded" Derby 
            Driver derbyEmbeddedDriver = new EmbeddedDriver();
            DriverManager.registerDriver(derbyEmbeddedDriver);
            // Make a connection.  To be used later for issuing specific commands.
            conn = DriverManager.getConnection(DB_URL);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    // Make a restaurant table.
    private void createTable() {
        try {
        	// Ask the connection for a structure to issue an SQL statement.
            stmt = conn.createStatement();
            // Construct the statement as a string.  In this case, the syntax to create a new table.
            stmt.execute("create table restaurants (" +  				// make a table 
            		"id INT NOT NULL GENERATED ALWAYS AS IDENTITY " + 	// primary key will be auto-generated
            		"CONSTRAINT id_PK PRIMARY KEY," + 					
            	    "name varchar(30) not null," + 						// name is a string of 30 chars, can't be null
            		"city varchar(30) not null" + 						// city is also a string
                    ")");
            stmt.close();												// done defining - run it
        }
        catch (SQLException sqlExcept) {
        	// We caught an error... might have been something really bad, or it might be 
        	// that the table we just tried to create already exists, in which case we 
        	// want to ignore the error.  
        	// Call a function specific to Derby to find out what kind of error it was.
        	if(!tableAlreadyExists(sqlExcept)) {					
        		sqlExcept.printStackTrace();
        	}
        }
    }
    
    
    // Some Derby-specific stuff.  Check if the error which was just thrown on table creation
    // was a "table already exists" error.
    private boolean tableAlreadyExists(SQLException e) {
        boolean exists;
        if(e.getSQLState().equals("X0Y32")) { 		// whacky implementation specific shizzle...
        	exists = true;
        } else {
            exists = false;
        }
        return exists;
    }
    
    
    // Insert the restName and cityName into the table as a new restaurant.
    private void insertRestaurant(String restName, String cityName) {
        try {
        	// Make a new SQL statement
            stmt = conn.createStatement();
            // Populate the statement with a string which should be valid SQL
            stmt.execute("insert into restaurants (name,city) values (" +
                    "'" + restName + "','" + cityName +"')");
            // Done defining - run it.
            stmt.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }
    
    
    // Generate a report.  
    private void selectRestaurants()
    {
        try {
        	// New SQL statement from the connection to the DB server 
            stmt = conn.createStatement();
            // Select all records from the restaurant table.  Get a result set back.
            ResultSet results = stmt.executeQuery("select * from restaurants");
            // Ask the result set to tell us about the data... not the data itself, but the 
            // metadata about the data...
            ResultSetMetaData rsmd = results.getMetaData();
            // We asked for "select *".  How many columns did we get back?
            int numberCols = rsmd.getColumnCount();
            // Iterate over the field names and display them as headers on the report.
            for (int i=1; i<=numberCols; i++) {
                // print Column Names
                System.out.print(rsmd.getColumnLabel(i)+"\t\t");  
            }

            // Some cosmetics for the report.
            System.out.println("\n-------------------------------------------------");

            // Iterate over the records/rows in the result set.
            while(results.next()) {
            	// Get each field from this row.  Note the first field is field 1.
            	// Each field has a specific type.  
                int id = results.getInt(1);
                String restName = results.getString(2);
                String cityName = results.getString(3);
                // Print out the values in tabular form
                System.out.println(id + "\t\t" + restName + "\t\t" + cityName);
            }
            // All done with this result set and SQL statement.  Close them up.
            results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }
    
    
    // Shut down the connection to the DB server cleanly.
    private void shutdown() {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                DriverManager.getConnection(DB_URL + ";shutdown=true");
                conn.close();
            }           
        }
        catch (SQLException sqlExcept) {
        }
    }
        
    
    // The main() which will be invoked when we "java -jar" on our executable jar.
    public static void main(String[] args) {
    	SqlDemo theApp = new SqlDemo();
    	try { 
    		theApp.run();
    	}
    	catch (Exception e) {
    		System.out.println("Something bad happened!");
    		e.printStackTrace();
    	}
    }	

}
