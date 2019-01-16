package csi311;


import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.derby.jdbc.EmbeddedDriver;


public class SqlDemo {

	private static final String DB_URL = "jdbc:derby:csi311-testdb1;create=true";
    private Connection conn = null;
    private Statement stmt = null;
	
	public SqlDemo() {
	}
	
	
    public void run(String filename) throws Exception {
        createConnection();
        createTable();
        insertRestaurant("LaVals", "Berkeley");
        selectRestaurants();
        shutdown();
    }
    
    
    private void createConnection() {
        try {
            Driver derbyEmbeddedDriver = new EmbeddedDriver();
            DriverManager.registerDriver(derbyEmbeddedDriver);
            conn = DriverManager.getConnection(DB_URL);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    private void createTable() {
        try {
            stmt = conn.createStatement();
            stmt.execute("create table restaurants (" +
            		"id INT NOT NULL GENERATED ALWAYS AS IDENTITY " + 
            		"CONSTRAINT id_PK PRIMARY KEY," + 
            	    "name varchar(30) not null," + 
            		"city varchar(30) not null" + 
                    ")");
            stmt.close();
        }
        catch (SQLException sqlExcept) {
        	if(!tableAlreadyExists(sqlExcept)) {
        		sqlExcept.printStackTrace();
        	}
        }
    }
    
    
    private boolean tableAlreadyExists(SQLException e) {
        boolean exists;
        if(e.getSQLState().equals("X0Y32")) {
            exists = true;
        } else {
            exists = false;
        }
        return exists;
    }
    
    
    private void insertRestaurant(String restName, String cityName) {
        try {
            stmt = conn.createStatement();
            stmt.execute("insert into restaurants (name,city) values (" +
                    "'" + restName + "','" + cityName +"')");
            stmt.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }
    
    
    private void selectRestaurants()
    {
        try {
            stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("select * from restaurants");
            ResultSetMetaData rsmd = results.getMetaData();
            int numberCols = rsmd.getColumnCount();
            for (int i=1; i<=numberCols; i++) {
                //print Column Names
                System.out.print(rsmd.getColumnLabel(i)+"\t\t");  
            }

            System.out.println("\n-------------------------------------------------");

            while(results.next()) {
                int id = results.getInt(1);
                String restName = results.getString(2);
                String cityName = results.getString(3);
                System.out.println(id + "\t\t" + restName + "\t\t" + cityName);
            }
            results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }
    
    
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
        
    
    public static void main(String[] args) {
    	SqlDemo theApp = new SqlDemo();
    	String filename = null; 
    	if (args.length > 0) {
    		filename = args[0]; 
    	}
    	try { 
    		theApp.run(filename);
    	}
    	catch (Exception e) {
    		System.out.println("Something bad happened!");
    		e.printStackTrace();
    	}
    }	

}
