package com.mse.analisys;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import weka.classifiers.Evaluation;
import weka.classifiers.rules.JRip;

import com.mse.persistence.Dao;

public class AnalysisUtilities {

	private Dao dao;
	private BufferedWriter writer;
	private double reachedPercentage;

	public AnalysisUtilities(){
		this.dao = new Dao();
		this.reachedPercentage = 1;
	}

	public AnalysisUtilities(String fileName, String executionMode){
		this.dao = new Dao(fileName, executionMode);
		this.reachedPercentage = 1;
	}

	public void setWriter(BufferedWriter writer){
		this.writer = writer;
	}

	public BufferedWriter getWriter(){
		return this.writer;
	}

	public double getReachedPercentage(){
		return this.reachedPercentage;
	}


	public void printResults(JRip jrip, Evaluation eval) {

		try{

			String rules;

			String separatorTripleLine =	"____________________________________________________________________________\n"
					+ "____________________________________________________________________________\n"
					+ "____________________________________________________________________________";


			String separatorDoubleLine =	"____________________________________________________________________________\n"
					+ "____________________________________________________________________________";

			String separatorLine =	"____________________________________________________________________________";

			System.out.println(separatorDoubleLine);
			System.out.println("RESULTS:\n");
			System.out.println("Rules: ");
			rules = jrip.toString();
			System.out.println(rules);
			System.out.println(separatorLine);
			System.out.println(eval.toClassDetailsString("DETAILS:"));
			System.out.println(separatorLine);
			System.out.println(eval.toSummaryString("SUMMARY:", true));
			System.out.println(separatorLine);
			System.out.println(eval.toMatrixString("MATRIX:"));
			System.out.println(separatorTripleLine);

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void saveResultsToFile(JRip jrip, Evaluation eval) {


		try{


			String rules;

			String separatorTripleLine =	"____________________________________________________________________________\n"
									  	  + "____________________________________________________________________________\n"
										  + "____________________________________________________________________________";


			String separatorDoubleLine =	"____________________________________________________________________________\n"
										  + "____________________________________________________________________________";

			String separatorLine =	"____________________________________________________________________________";

			this.writer.write(separatorDoubleLine+"\n");
			this.writer.write("RESULTS:\n\n");
			this.writer.write("Rules: \n");
			rules = jrip.toString();
			this.writer.write(rules+"\n");
			this.writer.write(separatorLine+"\n");
			this.writer.write(eval.toClassDetailsString("DETAILS:\n"));
			this.writer.write(separatorLine+"\n");
			this.writer.write(eval.toSummaryString("SUMMARY:\n", true));
			this.writer.write(separatorLine+"\n");
			this.writer.write(eval.toMatrixString("MATRIX:\n"));
			this.writer.write(separatorTripleLine+"\n");

		}catch(Exception e){
			e.printStackTrace();
		}
	}



	/**
	 * Append the given string at the end of output file, and wraps.
	 * @param toSave
	 */
	public void saveStringToFile(String toSave){
		try{

			this.writer.write(toSave+"\n");



		}catch(Exception e){
			e.printStackTrace();
		}

	}





	public void closeFile() {
		try {

			this.writer.flush();
			this.writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	/**
	 * 
	 * This Method saves all the results into the database, but it returns only the ExperimentId,
	 * that is an identifier of all Experiment data inserted.
	 * 
	 * @param jrip
	 * @param eval
	 * @param precExperimentId
	 * @return newExperimentId
	 * 
	 */
	public int saveResultsIntoDataBase(JRip jrip, Evaluation eval, int idPrecExperiment, long duration){
		String info = "Running time in milliseconds: " + duration;
		int idExperiment = -1;
		try {
			//TODO taking jrip and eval, save into the database.
			//Into the table experiment, save the precExperimentId.
			//After inseriment, return the ExperimentID returned.

			//			What we have..

			//		this.writer.write(jrip.toString()+"\n");
			//jrip.toString() = 
			//		   JRIP rules:
			//		   ===========
			//		   
			//		   ( TMEM220|388335 >= 2.621171) and ( LIMS2|55679 >= 10.901747) =>  Class= Normal (96.0/0.0)
			//		   ( GLT25D1|79709 <= 49.909964) and ( ?|391714 >= 0.011218) =>  Class= Normal (5.0/1.0)
			//		   ( AACS|65985 >= 148.193743) =>  Class= Normal (2.0/0.0)
			//		    =>  Class= BRC (781.0/0.0)
			//   
			//   Number of Rules : 4


			//			this.writer.write(eval.toSummaryString("SUMMARY:\n", true));
			//			this.writer.write(eval.toMatrixString("MATRIX:\n"));


			//Experiment
			idExperiment = this.dao.insertExperiment(idPrecExperiment, info);

			//ExperimentResults
			String details = eval.toClassDetailsString();
			String summary = eval.toSummaryString();

			Map<String, String> experimentResults = getExperimentResults(details, summary);
			this.dao.insertExperimentResults(idExperiment,
					Double.parseDouble(experimentResults.get("precisionValue")),
					Double.parseDouble(experimentResults.get("recallValue")),
					Double.parseDouble(experimentResults.get("fMeasureValue")),
					Double.parseDouble(experimentResults.get("tpRate")),
					Double.parseDouble(experimentResults.get("fpRate")),
					Integer.parseInt(experimentResults.get("correctlyClassifiedInstances")),
					Integer.parseInt(experimentResults.get("incorrectlyClassifiedInstances")),
					eval.toMatrixString());

			double fMeasurePercentage = Double.parseDouble(experimentResults.get("fMeasureValue"));

			if(fMeasurePercentage < this.reachedPercentage)
				this.reachedPercentage = fMeasurePercentage;

			Map<String, Map<String, String>> experimentResultsPerClass = getExperimentResultsPerClass(details);

			List<String> rules = getRules(jrip.toString());//jripRules);
			Map<String, List<String>> rulesPerClass = getRulesPerClass(rules);

			for(String classString : rulesPerClass.keySet()){	
				int idClass = this.dao.findIdClass(classString);

				if(idClass == -1){
					idClass = this.dao.insertClass(classString, "");
				}

				Map<String, String> experimentResultsForThisClass = experimentResultsPerClass.get(classString);
				this.dao.insertExperimentResultsPerClass(idExperiment,
						idClass,
						Double.parseDouble(experimentResultsForThisClass.get("precisionValue")),
						Double.parseDouble(experimentResultsForThisClass.get("recallValue")),
						Double.parseDouble(experimentResultsForThisClass.get("fMeasureValue")),
						Double.parseDouble(experimentResultsForThisClass.get("tpRate")),
						Double.parseDouble(experimentResultsForThisClass.get("fpRate")));

				Map<String, List<String>> literalsPerRuleFragment = getLiteralsPerRuleFragment(rulesPerClass.get(classString));		

				List<String> thisClassRulesList = rulesPerClass.get(classString);

				String completeRule = thisClassRulesList.get(0).split("\\s*=>\\s*")[0];

				if (completeRule.equals("")){
					completeRule = thisClassRulesList.get(0).split("\\s*=>\\s*")[1];
				}


				for(int i=1; i<thisClassRulesList.size(); i++){
					if (thisClassRulesList.get(i).split("\\s*=>\\s*")[0].equals("")){
						completeRule += " || "+thisClassRulesList.get(i).split("\\s*=>\\s*")[1];
					}else{
						completeRule += " || "+thisClassRulesList.get(i).split("\\s*=>\\s*")[0];
					}
				}				

				int idRule = this.dao.insertRule(idExperiment, idClass, completeRule);

				for(int i=0; i<thisClassRulesList.size(); i++){
					String ruleFragment = thisClassRulesList.get(i);
					List<String> literals = literalsPerRuleFragment.get(ruleFragment);
					if(!literals.get(0).equals("")&&!literals.get(0).equals(" ")&&!literals.get(0).equals("\t")){

						int correctlyClassifiedInstances = 0;
						int incorrectlyClassifiedInstances = 0;

						String[] splittedRuleFragment = ruleFragment.split("\\s");
						String[] splittedClassifiedInstancesValues = splittedRuleFragment[splittedRuleFragment.length-1].replace("(", "").replace(")", "").split("/");

						correctlyClassifiedInstances = (int) Double.parseDouble(splittedClassifiedInstancesValues[0]);
						incorrectlyClassifiedInstances = (int) Double.parseDouble(splittedClassifiedInstancesValues[1]);

						//now we can delete last part of string
						ruleFragment = ruleFragment.substring(0, ruleFragment.lastIndexOf(" ")+1);

						int idLiteralsSet = this.dao.insertLiteralsSet(idRule, "", ruleFragment, correctlyClassifiedInstances, incorrectlyClassifiedInstances);

						for(String literal : literals){
							literal = literal.replaceAll("\\(", "").replaceAll("\\)", "");
							String[] literalSplitted = literal.replaceFirst("^\\s", "").split("\\s");

							for(String l : literalSplitted)
								System.out.println("Literal Splitted Line: "+l);

							String attribute = literalSplitted[0];
							int idAttribute = this.dao.findIdAttribute(attribute);
							if(idAttribute == -1)
								idAttribute = this.dao.insertAttribute(attribute, "");

							this.dao.insertLiteral(idLiteralsSet, idAttribute, literalSplitted[1], Double.parseDouble(literalSplitted[2]) );
						}
					}
				}				


			}

			/*			String jripRules = "\nJRIP rules:"
							 + "\n==========="
							 + "\n"
							 + "\n( TMEM220|388335 >= 2.621171) and ( LIMS2|55679 >= 10.901747) =>  Class= Normal (96.0/0.0)"
							 + "\n( GLT25D1|79709 <= 49.909964) and ( ?|391714 >= 0.011218) =>  Class= Normal (5.0/1.0)"
							 + "\n( AACS|65985 >= 148.193743) =>  Class= Normal (2.0/0.0)"
							 + "\n =>  Class= BRC (781.0/0.0)"
							 + "\n"
							 + "\nNumber of Rules : 4"
							 + "\n";
			 */






		} catch (Exception e) {
			e.printStackTrace();
		}
		return idExperiment;
	}


	private Map<String, Map<String, String>> getExperimentResultsPerClass(String details) {
		System.out.println("StringDetails: "+details);
		String[] detailsLines = details.replaceFirst("^\n", "").split("\n");

		Map<String, Map<String, String>> classToValuesMap = new HashMap<String, Map<String, String>>();

		for(int i=3; i<detailsLines.length-1; i++){
			String[] line = detailsLines[i].replaceFirst("^\\s+", "").split("\\s+");
			//			System.out.println("DETAILS LINE: "+detailsLines[i]);
			//			System.out.println("LINE: "+line[0]);
			//			System.out.println("Line number "+i+", number of fields: "+line.length);
			Map<String, String> toInsert = new HashMap<String, String>();

			String tpRate = line[0];
			String fpRate = line[1];
			String precisionValue = line[2];
			String recallValue = line[3];
			String fMeasureValue = line[4];
			String classValue = line[line.length-1];

			toInsert.put("tpRate", tpRate);
			toInsert.put("fpRate", fpRate);
			toInsert.put("precisionValue", precisionValue);
			toInsert.put("recallValue", recallValue);
			toInsert.put("fMeasureValue", fMeasureValue);

			classToValuesMap.put(classValue, toInsert);

		}
		return classToValuesMap;
	}


	private Map<String, String> getExperimentResults(String details, String summary) {

		Map<String, String> nameValueToValue = new HashMap<String, String>();

		String[] detailsLines = details.replaceFirst("^\n", "").split("\n");
		String[] lastLine = detailsLines[detailsLines.length-1].replaceFirst("^\\s+", "").split("\\s+");

		String tpRate = lastLine[2];
		String fpRate = lastLine[3];
		String precisionValue = lastLine[4];
		String recallValue = lastLine[5];
		String fMeasureValue = lastLine[6];

		System.out.println(tpRate);
		System.out.println(fpRate);
		System.out.println(precisionValue);
		System.out.println(recallValue); 
		System.out.println(fMeasureValue);

		nameValueToValue.put("tpRate", tpRate);
		nameValueToValue.put("fpRate", fpRate);
		nameValueToValue.put("precisionValue", precisionValue);
		nameValueToValue.put("recallValue", recallValue);
		nameValueToValue.put("fMeasureValue", fMeasureValue);

		/****** SUMMARY ********/
		String[] summaryLines = summary.replaceFirst("^\n", "").split("\n");
		String[] lineZero = summaryLines[0].split("\\s+");
		String[] lineOne = summaryLines[1].split("\\s+");

		String correctlyClassifiedInstances = lineZero[3];
		String incorrectlyClassifiedInstances = lineOne[3];

		System.out.println(correctlyClassifiedInstances);
		System.out.println(incorrectlyClassifiedInstances);

		nameValueToValue.put("correctlyClassifiedInstances", correctlyClassifiedInstances);
		nameValueToValue.put("incorrectlyClassifiedInstances", incorrectlyClassifiedInstances);


		return nameValueToValue;
	}


	private List<String> getRules(String totalRules){
		/*
		 * List<rule>
		 */
		List<String> toReturn = new LinkedList<String>();

		String[] totalRulesSplitted  = totalRules.replaceFirst("^\n+", "").split("\n");
		for(int i=3; i<totalRulesSplitted.length-2; i++){
			toReturn.add(totalRulesSplitted[i]);
		}

		return toReturn;
	}


	private Map<String, List<String>> getRulesPerClass(List<String> totalRules){
		Map<String, List<String>> toReturn = new HashMap<String, List<String>>();
		for(String rule : totalRules){
			System.out.println("Rule:\t"+rule);
			String classString = "";
			try{	
				classString = rule.replaceFirst("\\(\\s*", "").split("\\)\\s*=>\\s*")[1].split("\\s*=\\s*|\\ ")[1];
				System.out.println("CLASS STRING: "+classString);
			}
			catch(Exception e){
				classString = rule.split("\\s*=>\\s*")[1].split("\\s*=\\s*|\\ ")[1];
				System.out.println("CLASS STRING: "+classString);
			}
			if(toReturn.keySet().contains(classString)){
				toReturn.get(classString).add(rule);
			}
			else{
				List<String> newList = new LinkedList<String>();
				newList.add(rule);
				toReturn.put(classString, newList);
			}

		}		
		return toReturn;
	}



	private Map<String, List<String>> getLiteralsPerRuleFragment(List<String> rulesWithSingleClass){
		Map<String, List<String>> toReturn = new HashMap<String, List<String>>();
		for(String rule : rulesWithSingleClass){
			String[] literals = rule.replaceFirst("\\(\\s*", "").split("\\)\\s*=>\\s*")[0].split("\\)\\s*and\\s*\\(|\\s*=>\\s*");
			List<String> literalsList = new LinkedList<String>();

			for(String literal : literals)
				literalsList.add(literal);


			toReturn.put(rule, literalsList);
		}

		return toReturn;
	}

	//	public void insertRemovedWindow(String[] window) {
	//		this.dao.insertRemovedAttributesSet(window);
	//	}

	public void incrementCounterWindow(String[] window){
		int id = this.dao.findIdFoundAttributesSetInThisRun(window);
		this.dao.incrementCounterWindow(window, id);
	}

	public void closeConnection(){
		this.dao.closeConnection();
	}

	public void saveOrIncrementFoundAttributesSet(List<String[]> windows) {
		this.dao.saveOrIncrementFoundAttributesSet(windows);
	}

	public void insertRemovedAttributesSet(int idExperiment, String[] window){
		this.dao.insertRemovedAttributesSet(idExperiment, window);
	}

}
