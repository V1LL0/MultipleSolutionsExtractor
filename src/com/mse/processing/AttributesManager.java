package com.mse.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AttributesManager{

	/* Variables */
	private Map<Integer, List<String>> experimentId_AttributesMap;
	private List<Integer> orderList;

	private int nextExperimentId;


	/*  Getters & Setters  */
	public int getNextExperimentId(){
		return this.nextExperimentId;
	}

	/* Constructor */
	public AttributesManager(){
		this.experimentId_AttributesMap = new HashMap<Integer, List<String>>();
		this.nextExperimentId = -1;
		this.orderList = new LinkedList<Integer>();
	}

	/* Methods */

	public List<String> attributesFromRules(String rules) {
		String[] attributesTemp;
		List<String> attributes;

		attributesTemp = rules.split("=>|=|\n|and");
		attributes = new ArrayList<String>();

		for(String attribute : attributesTemp){
			if(attribute.matches("\\ *\\(.*")){
				attribute = attribute.replaceAll("\\ |\\(|<|>", "");
				attributes.add(attribute);
			}
		}

		/* STAMPA DI CONTROLLO */
		/*
		System.out.println("Attributes found: "+genes.size());
		System.out.println("Attributes:");
		int i=1;
		for(String attribute : attributes){
			System.out.println(i+" - "+attribute);
			i++;
		}
		 */
		return attributes;
	}


	private String[] cutAndCombineArrays(String[] array1, String[] array2, int firstIndex1, int lastIndex1, int firstIndex2, int lastIndex2){
		String[] toReturn = new String[(lastIndex1-firstIndex1)+(lastIndex2-firstIndex2)];
		int i=0;

		for(int k=firstIndex1; k<lastIndex1; k++){
			toReturn[i] = array1[k];
			i++;
		}

		for(int k=firstIndex2; k<lastIndex2; k++){
			toReturn[i] = array2[k];
			i++;
		}

		return toReturn;
	}




	private List<String[]> generateAttributesWindows(List<String> attributes, int windowSize) {	
		List<String[]> toCheck = new ArrayList<String[]>();

		int numberOfWindows=1;
		int factorial=windowSize;

		/* We need to calculate the number of windows that the method will return
		 * The calculus is all the possibilities in the window / all the permutations
		 **/
		for (int i=0; i<windowSize; i++){
			numberOfWindows*=attributes.size()-i;
		}
		for (int toMul=windowSize-1; toMul>=1; toMul--){
			factorial *= toMul;
		}
		numberOfWindows = numberOfWindows/factorial;
		/***** END OF CALCULUS *****/


		for(int i=0; i<attributes.size(); i++){
			String[] newWindow = new String[windowSize];
			int index = 0;
			for(int y=i; y<i+windowSize; y++){
				newWindow[index]=attributes.get(y%attributes.size());
				index++;
			}
			toCheck.add(newWindow);
		}

		int semiWindow = windowSize-1;
		List<String[]> otherToCheck = new ArrayList<String[]>();

		//if k doesn't reached numberOfWindows, we generate other windows with a particular technique
		//we can imagine the found windows in a column, then we hold stationary first index of each window,
		//we translate up, remaining indexes
		//	1 2 3
		//	4 5 6
		//  7 8 9
		//
		//	Will Become
		//
		//	1 5 6
		//	4 8 9
		//	7 2 3
		while(semiWindow>0){
			for(int j=1; j<attributes.size()/*-windowSize*/; j++){
				for(int i=0; i<toCheck.size(); i++){	
					String[] newWindow = cutAndCombineArrays(toCheck.get(i), toCheck.get((i+j)%toCheck.size()), 0, windowSize-semiWindow, windowSize-semiWindow, windowSize);
					otherToCheck.add(newWindow);
				}
			}


			toCheck.addAll(otherToCheck);

			otherToCheck.clear();
			semiWindow--;
		}


		//Now we need to delete permutations
		List<Integer> toRemove = new ArrayList<Integer>();
		for (int index1=0; index1<toCheck.size(); index1++){
			for (int index2=index1; index2<toCheck.size(); index2++){

				String[] first = toCheck.get(index1);
				String[] second = toCheck.get(index2);

				Arrays.sort(first);
				Arrays.sort(second);

				if(index1 != index2 && Arrays.equals(first, second) && !toRemove.contains(index1))
					toRemove.add(index2);
			}
		}

		//We can check if there are intern duplicates using a set.
		//So, if the set has a size lower than the first
		//we could conclude that there are intern duplicates.
		for (int i=0; i<toCheck.size(); i++){
			String[] winToCheck = toCheck.get(i);

			Set<String> attributesSet = new HashSet<String>();

			for(String g : winToCheck)
				attributesSet.add(g);

			if(attributesSet.size() < winToCheck.length){
				toRemove.add(i);
			}
		}


		List<Integer> toRemoveWithoutRepetitions = new ArrayList<Integer>();
		for(Integer i : toRemove){
			if(!toRemoveWithoutRepetitions.contains(i)){
				toRemoveWithoutRepetitions.add(i);
			}
		}

		Collections.sort(toRemoveWithoutRepetitions);
		Collections.reverse(toRemoveWithoutRepetitions);

		for (int i=0; i<toRemoveWithoutRepetitions.size(); i++){
			int indexToRemove = toRemoveWithoutRepetitions.get(i);
			toCheck.remove(indexToRemove);
		}
		return toCheck;

	}

	//	private List<String[]> generateGenesWindows(List<String> genes, int windowSize) {
	//		List<String> toCheck = new ArrayList<String>();
	//		List<String[]> toReturn = new LinkedList<String[]>();
	//
	//		int numberOfWindows=1;
	//		int factorial=windowSize;
	//
	//		/* We need to calculate the number of windows that the method will return
	//		 * The calculus is all the possibilities in the window / all the permutations
	//		 **/
	//		for (int i=0; i<windowSize; i++){
	//			numberOfWindows*=genes.size()-i;
	//		}
	//		for (int toMul=windowSize-1; toMul>=1; toMul--){
	//			factorial *= toMul;
	//		}
	//		numberOfWindows = numberOfWindows/factorial;
	//		/***** END OF CALCULUS *****/
	//
	//
	//		for(int i=0; i<genes.size(); i++){
	//			String newWindow = "";
	//			for(int y=i; y<i+windowSize; y++)
	//				newWindow+=(y%genes.size())+"";
	//			toCheck.add(newWindow);
	//		}
	//
	//		int semiWindow = windowSize-1;
	//		List<String> otherToCheck = new ArrayList<String>();
	//
	//
	//		//if k doesn't reached numberOfWindows, we generate other windows with a particular tecnique
	//		//we can immagine the found windows in a column, then we hold stationary first index of each window,
	//		//we translate up, remaining indexes
	//		//	1 2 3
	//		//	4 5 6
	//		//  7 8 9
	//		//
	//		//	Will Become
	//		//
	//		//	1 5 6
	//		//	4 8 9
	//		//	7 2 3
	//		while(semiWindow>0){
	//			for(int j=1; j<genes.size()-windowSize; j++){
	//				for(int i=0; i<toCheck.size(); i++){
	//					otherToCheck.add(toCheck.get(i).substring(0, windowSize-semiWindow)+toCheck.get((i+j)%toCheck.size()).substring(windowSize-semiWindow, windowSize));
	//				}
	//			}
	//			toCheck.addAll(otherToCheck);
	//			otherToCheck.clear();
	//			semiWindow--;
	//		}
	//
	//
	//		//Now we need to delete permutations
	//		//scroll the list toCheck and compare all couples of strings
	//		//if a couple contains same characters, is a permutation, and we need to remove it
	//		List<String> toRemove = new ArrayList<String>();
	//		for (String index1 : toCheck){
	//			for (String index2 : toCheck){
	//				char[] first = index1.toCharArray();
	//				char[] second = index2.toCharArray();
	//				Arrays.sort(first);
	//				Arrays.sort(second);
	//
	//				if(index1 != index2 && Arrays.equals(first, second) && !toRemove.contains(index1))// && !toRemove.contains(index2))
	//					toRemove.add(index2);
	//			}
	//		}
	//		for (String index : toCheck){
	//			char[] toCheckIfInternDuplicates = index.toCharArray();
	//
	//			int dimension1 = toCheckIfInternDuplicates.length;
	//			Set<Character> charList = new HashSet<Character>();
	//			for(char c : toCheckIfInternDuplicates)
	//				charList.add(c);
	//			int dimension2 = charList.size();
	//
	//			if(dimension2 < dimension1)
	//				toRemove.add(index);
	//		
	//
	//		}
	//
	//		for (String indexToRemove : toRemove){
	//			//System.out.println(indexToRemove);
	//			toCheck.remove(indexToRemove);
	//		}
	//
	//
	//		//Transform numbers in real genes windows and return the windows!
	//		for(String win : toCheck){
	//			int[] indexesOfWindowsElements = new int[win.length()];
	//			for(int i=0; i<win.length(); i++)
	//				indexesOfWindowsElements[i] = Integer.parseInt(win.charAt(i)+"");
	//
	//			String[] newWindow = new String[windowSize];
	//			for(int i=0; i<indexesOfWindowsElements.length; i++)
	//				newWindow[i] = genes.get(indexesOfWindowsElements[i]);
	//
	//			toReturn.add(newWindow);
	//
	//		}
	//
	//		return toReturn;
	//
	//	}


	private List<String[]> generateAllAttributesWindows(List<String> attributes){
		List<String[]> allWindows = new LinkedList<String[]>();
		for (int i=1; i<=attributes.size(); i++){
			allWindows.addAll(generateAttributesWindows(attributes, i));
		}
		return allWindows;
	}


	public List<String[]> generateWindowsFromRules(String rules){
		return generateAllAttributesWindows(attributesFromRules(rules));
	}


	public void addAttributesFromRules(String rules, int newExperimentId) {
		this.experimentId_AttributesMap.put(new Integer(newExperimentId), attributesFromRules(rules));
		this.orderList.add(new Integer(newExperimentId));

		//		System.out.println("STAMPIAMO TUTTA LA MAPPA: ");
		//		for(Integer expID : this.experimentId_WindowsMap.keySet()){
		//			System.out.println("EXP ID: "+expID);
		//			for(String[] geneWin : this.experimentId_WindowsMap.get(expID)){
		//				for(String s : geneWin)
		//					System.out.print(s+" $$ ");
		//				System.out.println();
		//			}
		//		}
		//		System.out.println("STAMPIAMO TUTTA LA LISTA: ");
		//		for(Integer i : orderList)
		//			System.out.println("Integer: "+i);

	}


	public List<String[]> getNextWindowsList(){
		List<String[]> toReturn;
		try{
			//			System.out.println("IL NEXT EXPERIMENT ID, PRIMA ERA: "+this.nextExperimentId);
			this.nextExperimentId = this.orderList.remove(0);
			//			System.out.println("IL NEXT EXPERIMENT ID, ORA È: "+this.nextExperimentId);
			toReturn =  generateAllAttributesWindows(this.experimentId_AttributesMap.remove(this.nextExperimentId));

			//			System.out.println("la lista TORETURN È: ");
			//			for(String[] win : toReturn){
			//				for(String s : win)
			//					System.out.print(s+" $ ");
			//				System.out.println();
			//			}

			if(toReturn == null)
				toReturn = new LinkedList<String[]>();

		}catch(Exception e){
			toReturn = new LinkedList<String[]>();
		}
		return toReturn;
	}


	public List<String> getNextAttributesList(){
		
		List<String> toReturn = null;
		try{
			//			System.out.println("IL NEXT EXPERIMENT ID, PRIMA ERA: "+this.nextExperimentId);
			this.nextExperimentId = this.orderList.remove(0);
			//			System.out.println("IL NEXT EXPERIMENT ID, ORA È: "+this.nextExperimentId);
			toReturn = this.experimentId_AttributesMap.remove(this.nextExperimentId);

			//			System.out.println("la lista TORETURN È: ");
			//			for(String[] win : toReturn){
			//				for(String s : win)
			//					System.out.print(s+" $ ");
			//				System.out.println();
			//			}

			if(toReturn == null)
				toReturn = null;

		}catch(Exception e){
			toReturn = null;
		}
		return toReturn;
	}



}
