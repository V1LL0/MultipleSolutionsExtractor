package com.mse.processing;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class DataElaborator {


	public Instances initializeData(DataSource source) {
		Instances data = null;

		try{
			data = source.getDataSet();

			// setting class attribute if the data format does not provide this information
			// For example, the XRFF format saves the class attribute information as well
			if (data.classIndex() == -1)
				data.setClassIndex(data.numAttributes() - 1);

			data = removeFirstAttribute(data);
		}catch(Exception e){
			System.out.println("Data initialization failed");
			System.out.println("Date and Time: "+new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
			e.printStackTrace();
		}

		return data;

	}


	private Instances removeFirstAttribute(Instances data) {

		try {

			String[] options = new String[2];
			options[0] = "-R";						// "range"
			options[1] = "1";						// first attribute
			Remove remove = new Remove();			// new instance of filter
			remove.setOptions(options);
			remove.setInputFormat(data);			// inform filter about dataset **AFTER** setting options
			data = Filter.useFilter(data, remove);	// apply filter

		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}




	public Instances removeAttributeByName(Instances data, String attribute) {
		System.out.println("Attribute That i'm going to remove: "+attribute);
		try {

			Remove filterRemover = new Remove();
			filterRemover.setInputFormat(data);

			String attributeRegExpression =  attribute.replaceAll("\\|", "\\\\|"); 
			attributeRegExpression = attributeRegExpression.replaceAll("\\?", "\\\\?");
			attributeRegExpression = attributeRegExpression.replaceAll("_", "\\\\_");

			int indexOfAttributeToRemove = findIndex(data, attributeRegExpression);

			System.out.println("Attribute Reg Expression: "+attributeRegExpression);
			System.out.println("Index Of Attribute to remove: "+indexOfAttributeToRemove);
			//If name of the attribute doesn't exists into the dataset, return original dataset
			if (indexOfAttributeToRemove<0)
				return data;

			//We need to add 1 becuase the filter wants to know the position (first, second, ecc...)
			//and not the index
			filterRemover.setOptions(new String[]{"-R", (indexOfAttributeToRemove+1)+""});
			System.out.println("How many attributes before removal: "+data.numAttributes());
			data = Filter.useFilter(data, filterRemover);
			System.out.println("How many attributes after removal: "+data.numAttributes());

			//			RemoveByName filterRemover = new RemoveByName();
			//			filterRemover.setInputFormat(data);
			//			
			//			String expression =  gene.replaceAll("\\|", "\\\\|"); 
			//			expression = "\\ "+expression;
			//			System.out.println("Expression: "+expression);
			//
			//			
			//			filterRemover.setExpression(expression);
			//			System.out.println("Quanti sono prima: "+data.numAttributes());
			//			data = Filter.useFilter(data, filterRemover);
			//			System.out.println("Quanti sono dopo: "+data.numAttributes());

		} catch (Exception e) {
			e.printStackTrace();
		}		
		return data;

	}



	/**
	 * 
	 * @param data, Instances loaded
	 * @param attributeRegExpression, Regular expression to match to find right attribute
	 * @return the index of the attribute, if found. -1 otherwise.
	 */
	private int findIndex(Instances data, String attributeRegExpression) {
		Enumeration<Attribute> attributes = data.enumerateAttributes();
		int index = 0;
		while(attributes.hasMoreElements()){
			String attribute = attributes.nextElement().toString();
			attribute = attribute.replaceAll("\\s*\\'\\s*", " ");
			String[] attributesSplit = attribute.split("@attribute ");
			attribute = attributesSplit[1].split(" ")[0];
			if(attribute.matches(attributeRegExpression)){
				return index;
			}
			else
				index++;
		}
		return -1;
	}



	public boolean deleteTMPFile(){
		try{
			File[] fileList = new File("/tmp").listFiles();
			List<File> filesToCheck = new LinkedList<File>();
			for(File file : fileList){
				if(file.getName().matches(".*arffOut.*\\.tmp")){
					filesToCheck.add(file);
				}
			}

			if (filesToCheck.size() == 1)
				return false;

			long maxTime = 0;
			List<File> toRemove = new LinkedList<File>();

			for(File file : filesToCheck){
				if (file.lastModified() > maxTime)
					maxTime = file.lastModified();
			}

			for(File file : filesToCheck){
				if(file.lastModified() < maxTime)
					toRemove.add(file);
			}
			boolean result = true;

			for(File file : toRemove){
				if(!file.delete())
					result = false;
				else
					System.out.println("Tmp file removed: "+file.getName());
			}
			return result;

		}catch(Exception e){
			return false;
		}

	}




}
