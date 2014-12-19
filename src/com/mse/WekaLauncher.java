package com.mse;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.rules.JRip;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import com.mse.analisys.AnalysisUtilities;
import com.mse.processing.DataElaborator;
import com.mse.processing.AttributesManager;

public class WekaLauncher {

	private	DataSource source = null;
	private	Instances data = null;

	private	DataElaborator dataElaborator;
	private	AttributesManager attributesManager;
	private	AnalysisUtilities analysisUtilities;

	//Strings for input and output
	private String pathToFile = "";
	//	private String outputFile = "";

	/*
	 * TODO insert max_iterations. Save all found attributes of an experiment,
	 * during the remove_windows_operations, other attributes found could be saved into other lists.
	 * In general, we could make a lists' map (or a list of lists), that contains found attributes from every new experiment,
	 * then, for each list, we could calculate the windows, and so on... (infinite iterations could be managed with
	 * the MAX_ITERATIONS field and with minPercentageCI.
	 * 
	 */	
	private int MAX_ITERATIONS;
	private double MIN_PERCENTAGE_CI;

	private int iterations = 1;
	private double reachedPercentage = 1;

	private List<String> attributes = new ArrayList<String>();

	private List<String[]> windowsFound = new LinkedList<String[]>();

	private int currentExperimentID = -1;

	public WekaLauncher(int max_iterations, double min_percentage){
		this.MAX_ITERATIONS = max_iterations;
		this.MIN_PERCENTAGE_CI = min_percentage;
	}


	public void initialize(String pathToFile, String executionMode){//, String outputFile){
		try {
			this.pathToFile = pathToFile;
			//			this.outputFile = outputFile;

			dataElaborator = new DataElaborator();
			attributesManager = new AttributesManager();
			analysisUtilities = new AnalysisUtilities(new File(pathToFile).getName(), executionMode);
			
			//Reinitialization of iterations and reached percentage
			this.iterations = 1;
			this.reachedPercentage = 1;
			this.attributes = new ArrayList<String>();
			this.windowsFound = new LinkedList<String[]>();
			
			
			//			analysisUtilities.setWriter(new BufferedWriter(new FileWriter(this.outputFile)));

			this.source = new DataSource(this.pathToFile);
			this.data = dataElaborator.initializeData(source);


		} catch (Exception e) {
			e.printStackTrace();
		}	
	}


	public void startStrict(){
		String rules = this.launchJRIP(this.data, -1);

		this.data = this.dataElaborator.initializeData(this.source);

		while(this.iterations < this.MAX_ITERATIONS && this.reachedPercentage > this.MIN_PERCENTAGE_CI){
			System.out.println("ITERATIONS: "+this.iterations +" < "+this.MAX_ITERATIONS+" (MAX_ITERATIONS)");
			System.out.println("REACHED fMEASURE: "+this.reachedPercentage +" > "+this.MIN_PERCENTAGE_CI+" (MIN_PERCENTAGE_CI)");

			attributes = this.attributesManager.attributesFromRules(rules);

			launchWekaRemovingAWindowOneByOneStrict();
			this.iterations++;
		}
	}


	public void startLoose(){

		//		this.analysisUtilities.saveStringToFile("...STARTING...");

		String rules = this.launchJRIP(this.data, -1);

		//		this.analysisUtilities.saveStringToFile("...FIRST JRIP DONE...");

		/** Example String of rules to make tests **/
		//		String rules = "JRIP rules:\n"+
		//				"===========\n"+
		//			"( SDPR|8436 >= 11.638572) and ( CILP2|148113 <= 0.889675) =>  Class= Normal (102.0/2.0)\n"+
		//			"( CGB7|94027 >= 0.638094) =>  Class= Normal (3.0/1.0)\n"+
		//			"=>  Class= BRC (779.0/0.0);";

		while(this.iterations < this.MAX_ITERATIONS && this.reachedPercentage > this.MIN_PERCENTAGE_CI){
			System.out.println("ITERATIONS: "+this.iterations +" < "+this.MAX_ITERATIONS+" (MAX_ITERATIONS)");
			System.out.println("REACHED fMEASURE: "+this.reachedPercentage +" > "+this.MIN_PERCENTAGE_CI+" (MIN_PERCENTAGE_CI)");

			//			this.analysisUtilities.saveStringToFile("Attributes:");
			attributes = this.attributesManager.attributesFromRules(rules);

			//			for(int i=0; i<genes.size(); i++)
			//				this.analysisUtilities.saveStringToFile(i+": "+genes.get(i));

			//			this.analysisUtilities.saveStringToFile("...STARTING REMOVING OF ATTRIBUTES IN WINDOWS...");
			launchWekaRemovingAWindowOneByOneLoose();
			this.iterations++;

		}

	}



	public void finalize(){
		//		this.analysisUtilities.closeFile();
		this.analysisUtilities.closeConnection();
	}


	private void launchWekaRemovingAWindowOneByOneLoose() {
		System.out.println("Launching loose-mode");
		Instances data = null;

		//Now we create a List of String[],
		//where every String array is a window of attributes to remove,
		//before a new launch is performed
		List<String[]> windows = this.attributesManager.getNextWindowsList();


		for (String[] window : windows){
			if(!listContainsWindow(windowsFound, window)){
				this.dataElaborator.deleteTMPFile();
				//reinitialize data
				data = this.dataElaborator.initializeData(this.source);

				for(String attribute : window){
					data = this.dataElaborator.removeAttributeByName(data, attribute);
				}
				launchJRIP(data, this.attributesManager.getNextExperimentId());
				windowsFound.add(window);
				this.analysisUtilities.insertRemovedAttributesSet(this.currentExperimentID, window);

			}

		}

	}
	
	

	private void launchWekaRemovingAWindowOneByOneStrict(){
		System.out.println("Launching strict-mode");

		//Now we create a List of String,
		//where every String is an attribute to remove before a new launch is performed
		List<String> window = this.attributesManager.getNextAttributesList();

		for(String attribute : window){
			String[] windowConsistsOfASingleAttribute = new String[]{attribute};
			if(!listContainsWindow(windowsFound, windowConsistsOfASingleAttribute)){

				this.dataElaborator.deleteTMPFile();

				data = this.dataElaborator.removeAttributeByName(data, attribute);

				launchJRIP(data, this.attributesManager.getNextExperimentId());
				windowsFound.add(windowConsistsOfASingleAttribute);
				this.analysisUtilities.insertRemovedAttributesSet(this.currentExperimentID, windowConsistsOfASingleAttribute);

			}

		}
	}




	private boolean listContainsWindow(List<String[]> list, String[] window) {
		for(String[] win : list){
			if(areStringArraysEquals(win, window))
				return true;
		}
		return false;
	}


	//TODO duplicate method in DAO, to remove with a refactoring
	private boolean areStringArraysEquals(String[] array, String[] array2) {
		if(array.length != array2.length)
			return false;

		Arrays.sort(array);
		Arrays.sort(array2);

		for(int z=0; z<array.length; z++){
			if(!array[z].equals(array2[z]))
				return false;
		}
		return true;
	}


	private String launchJRIP(Instances data, int precExperimentId) {

		String rules="";

		try {
			long startTime = System.currentTimeMillis();
			//Building a classifier
			//JRip

			//Divide the dataset into training and test set
			data.randomize(new Random());

			int maxIndex = data.numInstances();
			int numberOfInstancesToCopy = (int)(maxIndex*0.8);
			Instances dataTrain = new Instances (data, 0, numberOfInstancesToCopy); 
			Instances dataTest = new Instances (data, numberOfInstancesToCopy, maxIndex-numberOfInstancesToCopy);

			System.out.println("Data Insances: "+maxIndex+"\nInstancesToCopy: "+numberOfInstancesToCopy+"\nDataTrainInstances: "+dataTrain.numInstances()+"\nDataTestInstances: "+dataTest.numInstances());


			JRip jrip = new JRip();					// new instance of JRip
			jrip.buildClassifier(data);//dataTrain);				// build classifier			

			//Cross-Validation
			Evaluation eval = new Evaluation(data);//dataTrain);

			eval.evaluateModel(jrip, dataTest);
			rules = jrip.toString();
			//			System.out.println("RULES EVALUATE1:");
			//			System.out.println(rules);
			//			eval.crossValidateModel(jrip, data, 10, new Random(1));

			//			rules = jrip.toString();

			//			System.out.println("RULES EVALUATE2:");
			//			System.out.println(rules);


			long duration = System.currentTimeMillis() - startTime;

			//this.analysisUtilities.printResults(jrip, eval);
			//			this.analysisUtilities.saveResultsToFile(jrip, eval);
			int newExperimentId = this.analysisUtilities.saveResultsIntoDataBase(jrip, eval, precExperimentId, duration);
			this.currentExperimentID = newExperimentId;
			this.reachedPercentage = this.analysisUtilities.getReachedPercentage();
			System.out.println("REACHED PERCENTAGE: "+this.reachedPercentage);
			List<String[]> windows = this.attributesManager.generateWindowsFromRules(rules);

			this.analysisUtilities.saveOrIncrementFoundAttributesSet(windows);
			this.attributesManager.addAttributesFromRules(rules, newExperimentId);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return rules;

	}







}
