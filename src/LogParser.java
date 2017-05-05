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
		pricePredictorError();
	}
	
	public static void pricePredictorError(){
		
		PricePredictor pricePredictor = new PricePredictor("ZI-low-demand-2nd-iteration.model");
		
		String fileName = "low_demand_log.csv";
		File gFile = new File(fileName);
        if(!gFile.exists()){
            System.out.println("Load file doesn't exist");
        	return;
        }
        
        CSVParser parser;
		try {
			parser = CSVParser.parse(gFile, StandardCharsets.US_ASCII, CSVFormat.DEFAULT);
			
			FileWriter fwOutput2 = new FileWriter(fileName+".PricePredictorErrorUP.csv", true);
			PrintWriter pwOutput2 = new PrintWriter(new BufferedWriter(fwOutput2));
			pwOutput2.println("TS,HA,Error,");
			
			for (CSVRecord csvRecord : parser) {
	            Iterator<String> itr = csvRecord.iterator();
	            /*
	    		@ATTRIBUTE currentTimeSlot NUMERIC
	    		@ATTRIBUTE hour NUMERIC
	    		@ATTRIBUTE hourAhead NUMERIC
	    		@ATTRIBUTE date NUMERIC
	    		@ATTRIBUTE month NUMERIC
	    		@ATTRIBUTE year NUMERIC
	    		@ATTRIBUTE NUMBER_OF_BROKERS NUMERIC
	    		@ATTRIBUTE NUMBER_OF_PRODUCERS NUMERIC
	    		@ATTRIBUTE PrevDayHAMarketClearingPrice NUMERIC
	    		@ATTRIBUTE PrevHAMarketClearingPrice NUMERIC
	    		@ATTRIBUTE MarketClearingPrice NUMERIC
	    		*/
	            // Time Stamp	
	            double currentTimeSlot = Double.parseDouble(itr.next());
	            double hour = Double.parseDouble(itr.next());
	            double hourAhead = Double.parseDouble(itr.next());
	            double date = Double.parseDouble(itr.next());
	            double month = Double.parseDouble(itr.next());
	            double year = Double.parseDouble(itr.next());
	            double NUMBER_OF_BROKERS = Double.parseDouble(itr.next());
	            double NUMBER_OF_PRODUCERS = Double.parseDouble(itr.next());
	            double PrevDayHAMarketClearingPrice = Double.parseDouble(itr.next());
	            double PrevHAMarketClearingPrice = Double.parseDouble(itr.next());
	            double realClearingPrice = Double.parseDouble(itr.next());
	            
				double [] param = new double[11];
				param[0] = currentTimeSlot;
				param[1] = hour;
				param[2] = hourAhead;
				param[3] = date;
				param[4] = month;
				param[5] = year;
				param[6] = NUMBER_OF_BROKERS;
				param[7] = NUMBER_OF_PRODUCERS;
				param[8] = PrevDayHAMarketClearingPrice;
				param[9] = PrevHAMarketClearingPrice;
				
				double predictedPrice = pricePredictor.getLimitPrice(param);
				
				pwOutput2.println(currentTimeSlot + "," + hourAhead + "," + (realClearingPrice-predictedPrice)+",");
				
			}
			
			parser.close();
			pwOutput2.close();
			fwOutput2.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	
	
	public static void hourAheadErrorParser(){
		double [][] errorValue = new double [7][10];
		double [][] errorCounter = new double [7][10];
		String fileName = "low_demand_mcts_HA_prediction_error.csv";
		File gFile = new File(fileName);
        if(!gFile.exists()){
            System.out.println("Load file doesn't exist");
        	return;
        }
        int mctsITER = 0;
        CSVParser parser;
		try {
			boolean print = false;
			parser = CSVParser.parse(gFile, StandardCharsets.US_ASCII, CSVFormat.DEFAULT);
			
			FileWriter fwOutput2 = new FileWriter(fileName+".50KScattered.csv", true);
			PrintWriter pwOutput2 = new PrintWriter(new BufferedWriter(fwOutput2));
			pwOutput2.println("HA,Error,");
			
			

			for (CSVRecord csvRecord : parser) {
	            Iterator<String> itr = csvRecord.iterator();
	            // Time Stamp	
	            String strMCTS = itr.next();
	            if(strMCTS.equalsIgnoreCase("MCTS_SIM")){
	            	// Skip
	            }
	            else{
	            	// Check MCTS iteration
	            	String strDay = itr.next();
	            	String strHour = itr.next();
	            	String strHA = itr.next();
	            	int HA = Integer.parseInt(strHA);
	            	String strError = itr.next();
	            	int mcts;
	            	if(strMCTS.equalsIgnoreCase("100"))
	            		mcts = 0;
	            	else if(strMCTS.equalsIgnoreCase("1000"))
	            		mcts = 1;
	            	else if(strMCTS.equalsIgnoreCase("5000"))
	            		mcts = 2;
            		else if(strMCTS.equalsIgnoreCase("10000"))
	            		mcts = 3;
        			else if(strMCTS.equalsIgnoreCase("15000"))
	            		mcts = 4;
    				else if(strMCTS.equalsIgnoreCase("25000"))
	            		mcts = 5;
    				else if(strMCTS.equalsIgnoreCase("50000")){
	            		mcts = 6;
	            		print = true;
    				}
    				else{
    					System.out.println("Missmatch!");
    					return;
    				}
	            	
	            	if(print){
	            		pwOutput2.println(strHA + "," + strError+",");
	            		print = false;
	            	}
	            	
            		errorValue[mcts][HA] += Double.parseDouble(strError);
            		errorCounter[mcts][HA] += 1;
	            
	            }
			}
			
			parser.close();
			pwOutput2.close();
			fwOutput2.close();
			
			for(int j = 0; j<7; j++){
				for(int val =0;val<10;val++){
					errorValue[j][val] /= errorCounter[j][val];
				}
			}
			
			
			FileWriter fwOutput = new FileWriter(fileName+".AvgParsed.csv", true);
			PrintWriter pwOutput = new PrintWriter(new BufferedWriter(fwOutput));
						
			pwOutput.println("MCTS,0,1,2,3,4,5,6,7,8,9");
			for(int mcts = 0; mcts < 7; mcts++){
				pwOutput.print(mcts+",");
				for(int agent = 0; agent < 10; agent++){
					pwOutput.print(errorValue[mcts][agent]+",");
				}
				pwOutput.println();
			}
			
			pwOutput.close();
			fwOutput.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void costValueParser(){
		
		double [][][] arrAgents = new double[5][7][10];
		String fileName = "low_demand_Results_Price.csv";
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
