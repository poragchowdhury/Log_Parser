import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class LogParser {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		double [][][] arrAgents = new double[5][7][10];
		String fileName = "equal_demand_Results_Price.csv";
		File gFile = new File(fileName);
        if(!gFile.exists()){
            System.out.println("Load file doesn't exist");
        	return;
        }
        
        int mctsCounter = -1;
        CSVParser parser;
		try {
			parser = CSVParser.parse(gFile, StandardCharsets.US_ASCII, CSVFormat.DEFAULT);
		
		
			for (CSVRecord csvRecord : parser) {
	            Iterator<String> itr = csvRecord.iterator();
	            // Time Stamp	
	            String strSeed = itr.next();
	            
	            if(strSeed.equalsIgnoreCase("Seed"))
	            {
	            	mctsCounter++;
	            }
	            else{
	            	String strLBMP = itr.next();
	            	strLBMP = itr.next();
	            	strLBMP = itr.next();
	            	strLBMP = itr.next();
	            	strLBMP = itr.next();
	            	// add all the values 
	            	for(int agentCounter = 0; agentCounter < 5; agentCounter++){
		            	for(int valueCounter = 0; valueCounter < 10; valueCounter++){
		            		strLBMP = itr.next();
		            		arrAgents[agentCounter][mctsCounter][valueCounter] += Double.parseDouble(strLBMP); 
		            	}
		            	strLBMP = itr.next();
		            	if(strLBMP.equalsIgnoreCase("Mean Clearing Price"))
		            		break;
	            	}
	            }
	        }
		
			parser.close();
		
			// Do Average
			for(int i = 0; i < 5; i++){
				for(int j = 0; j<7; j++){
					for(int val =0;val<10;val++){
						arrAgents[i][j][val] /= 30.00;
					}
				}
			}
			
			FileWriter fwOutput = new FileWriter(fileName+".parsed.csv", true);
			PrintWriter pwOutput = new PrintWriter(new BufferedWriter(fwOutput));
			for(int val = 0; val < 10; val++){
				pwOutput.println("Graph " + val);
				pwOutput.println("MCTS,ZI,ZIP,SPOT,MCTSX,MCTS10K");
				for(int mcts = 0; mcts < 7; mcts++){
					pwOutput.print(mcts+",");
					for(int agent = 0; agent < 5; agent++){
						pwOutput.print(arrAgents[agent][mcts][val]+",");
					}
					pwOutput.println();
				}
			}
			pwOutput.close();
			fwOutput.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
