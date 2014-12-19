package com.mse.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Dao {

	private DriverLoader_Connector connector;
	private Connection conn;
	private long timestampInit = System.currentTimeMillis();
	private int idRun;
	
	
	public Dao(String fileName, String executionMode){
		try{
			this.connector = new DriverLoader_Connector();
			this.conn = connector.getConnection();
			this.idRun = this.insertRun(timestampInit, fileName, executionMode);

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public Dao(){
		try{
			this.connector = new DriverLoader_Connector();
			this.conn = connector.getConnection();
			this.idRun = this.insertRun(timestampInit, "", "");
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}




	/**
	 * Insert: method to execute a generic insert query.
	 * 
	 * @param this method take a Map<String, String> called parameters.
	 * The Map contains as first String the name of the field to insert, followed by a space and then by the java type.
	 * The second string is the value of the field to insert.
	 * For Example:
	 * "field1 int" -> "3". 
	 * The map must contain a key called "table"
	 * (that is also a reserved word in mysql, so you can't use it, normally, as a column name),
	 * and as value, the name of the table.
	 * 
	 */
	private int insert(Map<String, String> parameters){
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int id_toReturn = -1; 

		try {

			String insert = "INSERT INTO "+parameters.get("table")+"(";
			/*After saving the table name, we can remove the mapping for that, so as to leave only parameters*/
			String table = parameters.remove("table");

			int count= 0;

			for (String param_type : parameters.keySet()){
				String parameter = param_type.split(" ")[0];

				if(count < parameters.keySet().size()-1)
					insert+=parameter+", ";
				else
					insert+=parameter+") ";

				count++;
			}

			String valuesString = "VALUES (?";
			for (int i=1; i<parameters.keySet().size(); i++){
				valuesString+=",?";
			}
			valuesString+=")";
			insert+=valuesString;

			stmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);

			int index=1;
			for (String param_type : parameters.keySet()){
				String type =  param_type.split(" ")[1];
				String value = parameters.get(param_type);

				switch (type) {
				case "int":
					int intToInsert=Integer.parseInt(value);
					if(intToInsert == -1)
						stmt.setNull(index, Types.INTEGER);
					else
						stmt.setInt(index, intToInsert);
					break;
				case "double":
					stmt.setDouble(index, Double.parseDouble(value));
					break;
				case "long":
					stmt.setLong(index, Long.parseLong(value));
					break;
				case "String":
					stmt.setString(index, value);
					break;
				case "string":
					stmt.setString(index, value);
					break;
				case "boolean":
					stmt.setBoolean(index, Boolean.parseBoolean(value));
					break;

				default:
					stmt.setString(index, value);
					break;
				}

				index++;
			}

			stmt.executeUpdate();

			ResultSet generatedKeys = stmt.getGeneratedKeys();
			if (generatedKeys.next()) {
				id_toReturn = generatedKeys.getInt(1);
				System.out.println("Table: "+table);
				System.out.println("ID: "+id_toReturn);
			} else {
				id_toReturn = -1;
			}


			return id_toReturn;

		}
		catch (SQLException ex){
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

			return id_toReturn;
		}
		finally {
			finalizeOperation(stmt, rs);
		}

	}


	private int insertRun(long timestamp, String fileName, String executionMode) {
		
		Map<String, String> params = new HashMap<String, String>();

		params.put("table", "Run");
		params.put("timestamp long", timestamp+"");
		params.put("fileName String", fileName+"");
		params.put("executionMode String", executionMode);
		
		return insert(params);
	}


	public int insertExperimentResults(int idExperiment, double precisionValue,
			double recallValue, double fMeasureValue, double tpRate, double fpRate,
			int correctlyClassifiedInstances, int incorrectlyClassifiedInstances,
			String confusionMatrix){

		Map<String, String> params = new HashMap<String, String>();

		params.put("table", "ExperimentResults");
		//		params.put("idResult int", idResult+"");
		params.put("idExperiment int", idExperiment+"");
		params.put("precisionValue double", precisionValue+"");
		params.put("recallValue double", recallValue+"");
		params.put("fMeasureValue double", fMeasureValue+"");
		params.put("tpRate double", tpRate+"");
		params.put("fpRate double", fpRate+"");
		params.put("correctlyClassifiedInstances int", correctlyClassifiedInstances+"");
		params.put("incorrectlyClassifiedInstances int", incorrectlyClassifiedInstances+"");
		params.put("confusionMatrix String", confusionMatrix+"");

		return insert(params);

	}





	public int insertRule(int idExperiment, int idClass, String rule){

		Map<String, String> params = new HashMap<String, String>();

		params.put("table", "Rule");
		//		params.put("idRule int", idRule+"");
		params.put("idClass int", idClass+"");
		params.put("idExperiment int", idExperiment+"");
		params.put("rule String", rule+"");

		return insert(params);
	}




	public int insertExperiment(int idPrecExperiment, String info){

		Map<String, String> params = new HashMap<String, String>();

		params.put("table", "Experiment");
		//		params.put("idExperiment int", idExperiment+"");
		params.put("idPrecExperiment int", idPrecExperiment+"");
		params.put("idRun int", this.idRun+"");
		params.put("info String", info+"");

		return insert(params);

	}


	public int insertClass(String name, String description){

		Map<String, String> params = new HashMap<String, String>();

		params.put("table", "Class");
		//		params.put("idClass int", idClass+"");
		params.put("name String", name+"");
		params.put("description String", description+"");

		return insert(params);

	}


	public int insertExperimentResultsPerClass(int idExperiment, int idClass, double precisionValue,
			double recallValue, double fMeasureValue, double tpRate, double fpRate){

		Map<String, String> params = new HashMap<String, String>();

		params.put("table", "ExperimentResultsPerClass");
		//		params.put("idResult int", idResult+"");
		params.put("idExperiment int", idExperiment+"");
		params.put("idClass int", idClass+"");
		params.put("precisionValue double", precisionValue+"");
		params.put("recallValue double", recallValue+"");
		params.put("fMeasureValue double", fMeasureValue+"");
		params.put("tpRate double", tpRate+"");
		params.put("fpRate double", fpRate+"");

		return insert(params);

	}

	public int insertLiteral(int idLiteralsSet, int idAttribute, String operator, double value){

		Map<String, String> params = new HashMap<String, String>();

		params.put("table", "Literal");
		//		params.put("idLiteral int", idLiteral+"");
		params.put("idLiteralsSet int", idLiteralsSet+"");
		params.put("idAttribute int", idAttribute+"");
		params.put("operator String", operator+"");
		params.put("value double", value+"");

		return insert(params);

	}


	public int insertAttribute(String name, String description){

		Map<String, String> params = new HashMap<String, String>();

		params.put("table", "Attribute");
		//		params.put("idAttribute int", idAttribute+"");
		params.put("name String", name+"");
		params.put("description String", description+"");

		return insert(params);

	}

	public int insertLiteralsSet(int idRule, String description, String ruleFragment, int correctlyClassifiedInstances, int incorrectlyClassifiedInstances){

		Map<String, String> params = new HashMap<String, String>();

		params.put("table", "LiteralsSet");
		//		params.put("idAttributeSet int", idAttributeSet+"");
		params.put("idRule int", idRule+"");
		params.put("description String", description+"");
		params.put("ruleFragment String", ruleFragment+"");
		params.put("correctlyClassifiedInstances int", correctlyClassifiedInstances+"");
		params.put("incorrectlyClassifiedInstances int", incorrectlyClassifiedInstances+"");		

		return insert(params);

	}



	public int findIdClass(String className){

		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {

			String find = "select * from Class where name=?";
			stmt = conn.prepareStatement(find);
			stmt.setString(1, className);
			rs = stmt.executeQuery();

			if(rs.next())
				return rs.getInt(1);
			else
				return -1;

		}
		catch (SQLException ex){
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

			return -1;
		}
		finally {
			finalizeOperation(stmt, rs);
		}


	}


	public int findIdAttribute(String attribute) {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {

			String find = "select * from Attribute where name=?";
			stmt = conn.prepareStatement(find);
			stmt.setString(1, attribute);
			rs = stmt.executeQuery();

			if(rs.next())
				return rs.getInt(1);
			else
				return -1;

		}
		catch (SQLException ex){
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

			return -1;
		}
		finally {
			finalizeOperation(stmt, rs);
		}

	}

	public int findIdFoundAttributesSetInThisRun(String[] window){
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Map<Integer, List<String>> tmpMapOfLists = new HashMap<Integer, List<String>>();
		int idFoundAttributeSet_tmp = -1;
		List<String> tmpList = new LinkedList<String>();

		try {

			String findMap = "select FoundAttributesSet_Attribute.idFoundAttributesSet, Attribute.name from FoundAttributesSet_Attribute, Attribute, FoundAttributesSet where FoundAttributesSet_Attribute.idAttribute = Attribute.idAttribute and FoundAttributesSet_Attribute.idFoundAttributesSet = FoundAttributesSet.idFoundAttributesSet and FoundAttributesSet.idRun = ? group by FoundAttributesSet_Attribute.idFoundAttributesSet, Attribute.name;";
			stmt = conn.prepareStatement(findMap);
			stmt.setInt(1, this.idRun);
			
			rs = stmt.executeQuery();

			while(rs.next()){

				if(idFoundAttributeSet_tmp == -1){
					idFoundAttributeSet_tmp=rs.getInt(1);
				}

				int idFoundAttributeSet = rs.getInt(1);
				String attributeName = rs.getString(2);

				if(idFoundAttributeSet == idFoundAttributeSet_tmp)
					tmpList.add(attributeName);
				else{
					tmpMapOfLists.put(idFoundAttributeSet_tmp, tmpList);
					idFoundAttributeSet_tmp = idFoundAttributeSet;
					tmpList = new LinkedList<String>();
					tmpList.add(attributeName);
				}
			}
			tmpMapOfLists.put(idFoundAttributeSet_tmp, tmpList);

			for(int idFASet : tmpMapOfLists.keySet()){
				List<String> list = tmpMapOfLists.get(idFASet);
				if(list.size() == window.length){
					String[] array = new String[list.size()];
					int i=0;
					for(String s : list){
						array[i] = s;
						i++;
					}
					Arrays.sort(array);
					Arrays.sort(window);

					if(areStringArraysEquals(array, window))
						return idFASet;

				}
			}

			return -1;
		}catch (SQLException ex){
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());

		}
		finally {
			finalizeOperation(stmt, rs);
		}

		return -1;
	}



	public void insertFoundAttributesSet(String[] window) {
		Map<String, String> params = new HashMap<String, String>();

		params.put("table", "FoundAttributesSet");
		params.put("idRun int", this.idRun+"");
		params.put("counterFoundTimes int", 1+"");
		
		int idFA_Set = insert(params);
		
		for(String attribute : window){
			params = new HashMap<String, String>();
	
			params.put("table", "FoundAttributesSet_Attribute");
			
			params.put("idFoundAttributesSet int", idFA_Set+"");
			params.put("idAttribute int", findIdAttribute(attribute)+"");
			
			insert(params);
		}
	}

	
	
	
	public void insertRemovedAttributesSet(int idExperiment, String[] window) {
		Map<String, String> params = new HashMap<String, String>();

		params.put("table", "RemovedAttributesSet");
		params.put("idExperiment int", idExperiment+"");
		
		int idRA_Set = insert(params);
		
		for(String attribute : window){
			params = new HashMap<String, String>();
	
			params.put("table", "RemovedAttributesSet_Attribute");
			
			params.put("idRemovedAttributesSet int", idRA_Set+"");
			params.put("idAttribute int", findIdAttribute(attribute)+"");
			
			insert(params);
		}
	}
	
	


	private boolean areStringArraysEquals(String[] array, String[] array2) {
		if(array.length != array2.length)
			return false;

		for(int z=0; z<array.length; z++){
			if(!array[z].equals(array2[z]))
				return false;
		}
		return true;
	}


	private void finalizeOperation(Statement stmt, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException sqlEx) { } // ignore

			rs = null;
		}

		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException sqlEx) { } // ignore
			stmt = null;
		}

	}


	public void closeConnection(){

		if (this.conn != null) {
			try {
				this.conn.close();
			} catch (SQLException sqlEx) { } // ignore
			this.conn = null;
		}

	}


	public void incrementCounterWindow(String[] window, int id) {
		
		if(id != -1){
			PreparedStatement stmt = null;
			ResultSet rs = null;

			try {
				String stringToPrint = "";
				for(String attribute : window)
					stringToPrint += attribute + "   ";
				System.out.println("Window to increment: "+stringToPrint);
				
				String increment = "UPDATE FoundAttributesSet SET counterFoundTimes=counterFoundTimes+1 WHERE idFoundAttributesSet=?";
				stmt = conn.prepareStatement(increment);
				stmt.setInt(1, id);
				stmt.executeUpdate();

			}
			catch (SQLException ex){
				// handle any errors
				System.out.println("SQLException: " + ex.getMessage());
				System.out.println("SQLState: " + ex.getSQLState());
				System.out.println("VendorError: " + ex.getErrorCode());

			}
			finally {
				finalizeOperation(stmt, rs);
			}
		}
		
		
	}

	


	public void saveOrIncrementFoundAttributesSet(List<String[]> windows) {
		for(String[] window : windows){
			try{
				//TODO in questo modo il findID lo fa 2 volte, una qui e una dentro increment counter window...
				
				int id = findIdFoundAttributesSetInThisRun(window);
				if(id!=-1){
					incrementCounterWindow(window, id);
				}
				else{
					insertFoundAttributesSet(window);
				}	
			}catch(Exception e){
				e.printStackTrace();
			}

			
		}
		
	}



	

}