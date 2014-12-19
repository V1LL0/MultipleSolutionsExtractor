package com.mse;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;



public class MultipleSolutionsExtractorMain {

	public static void main(String[] args) {
		int max_iterations = Integer.parseInt(args[0]);
		double min_percentage_fmeasure = Double.parseDouble(args[1]);
		String strict_loose_double = args[2];

		BufferedWriter writer = null;

		try {
			writer = new BufferedWriter(new FileWriter("./executionInformation.txt"));

			if( !strict_loose_double.equals("strict")	&& 
					!strict_loose_double.equals("loose")	&&
					!strict_loose_double.equals("double")	)
			{	
				throw new IOException("Command Not Recognized: "+strict_loose_double);
			}

			for(int i=3; i<args.length; i++){
				try{
					String pathToFile = args[i];
					//			String outputFile = args[1];
					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm");

					long timeInMillis = System.currentTimeMillis();
					System.out.println("FILE in Examination: "+args[i]);
					System.out.println("Time: "+sdf.format(new Date(timeInMillis)));
					writer.write("FILE in Examination: "+args[i]+"\n");
					writer.write("Time: "+sdf.format(new Date(timeInMillis))+"\n");
					writer.flush();

					WekaLauncher wekaLuncher = new WekaLauncher(max_iterations, min_percentage_fmeasure);
					//			"/media/5AE6A6A5E6A680BD/Universita/Magistrale/2_anno/2_semestre/AAA_TESI/outputFiles/datamatrix.csv";
					//			"/media/5AE6A6A5E6A680BD/Universita/Magistrale/2_anno/2_semestre/AAA_TESI/wekaAnalysis/wekaResults.txt";



					if(strict_loose_double.equals("strict")){
						wekaLuncher.initialize(pathToFile, strict_loose_double);
						wekaLuncher.startStrict();
					}else if(strict_loose_double.equals("loose")){						
						wekaLuncher.initialize(pathToFile, strict_loose_double);
						wekaLuncher.startLoose();
					}else if(strict_loose_double.equals("double")){
						wekaLuncher.initialize(pathToFile, "loose");
						wekaLuncher.startLoose();
						wekaLuncher.initialize(pathToFile, "strict");
						wekaLuncher.startStrict();
					}

					wekaLuncher.finalize();


					timeInMillis = System.currentTimeMillis();
					System.out.println("Finished file: "+args[i]);
					System.out.println("Time: "+sdf.format(new Date(timeInMillis)));
					writer.write("Finished file: "+args[i]+"\n");
					writer.write("Time: "+sdf.format(new Date(timeInMillis))+"\n");
					writer.flush();
				}catch(Exception e){
					continue;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try{
				writer.flush();
				writer.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}


	}




}
