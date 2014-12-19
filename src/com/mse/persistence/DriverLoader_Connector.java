package com.mse.persistence;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
// Notice, do not import com.mysql.jdbc.*
// or you will have problems!


public class DriverLoader_Connector {

	private boolean driverLoaded=false;
	private Connection conn = null;

	private String dbUri="jdbc:mysql://";
	private String username="";
	private String password="";

	public DriverLoader_Connector(){
		
		String user = "root";
		String password = "root";
		String db_url = "localhost";
		String db_name = "WekaAnalysis";
		
		try{
			BufferedReader fileReader = new BufferedReader(new FileReader("config.txt"));
		
			while(fileReader.ready()){
				String line = fileReader.readLine();
				
				if(!line.matches("\\s*") && !line.matches("\\s*#.*")){
					if(line.matches("^USER=.*")){
						user = line.split("USER=")[1]; 
					}
					else if(line.matches("^PASSWORD=.*")){
						password = line.split("PASSWORD=")[1]; 
					}
					else if(line.matches("^DB_URL=.*")){
						db_url = line.split("DB_URL=")[1]; 
					}
					else if(line.matches("^DB_NAME=.*")){
						db_name = line.split("DB_NAME=")[1]; 
					}
					
				}
				
				
			}
		
		
				
		}catch(Exception e){
			System.out.println("File config.txt not found.\nThe program will be executed with the default values below\nuser:root\npassword:root\ndb_url:localhost\ndb_name:WekaAnalysis");
		}
		
		this.dbUri += db_url + "/" + db_name;
		this.username = user;
		this.password = password;
		
		
	}



	private void start() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			driverLoaded=true;
		} catch (Exception ex) {
			driverLoaded=false;
			ex.printStackTrace();
		}
	}


	public Connection getConnection() throws SQLException{
		if(!driverLoaded)
			start();

		if(conn == null || conn.isClosed()){
			try {
				conn = DriverManager.getConnection(dbUri, username, password);
				return conn;

			} catch (SQLException ex) {
				// handle any errors
				System.out.println("SQLException: " + ex.getMessage());
				System.out.println("SQLState: " + ex.getSQLState());
				System.out.println("VendorError: " + ex.getErrorCode());
			}
		}
		return conn;
	}



}

