import java.util.*;

import leaderSelectionNeuralNetProtocol.*;

import DistLib.*;

import neuralNetDistributedAndAdaptiveProtocol.*;
import distributedAndAdaptiveProtocol.*;
import stimulator.*;
import utilities.*;


public class Testor {
	
	public static void main(String[] args){
		
		boolean use_random_data = false;
		
		String file_path = "C:\\Users\\puaykai\\Desktop\\UROP\\stbernard-meteo-arranged-filled.csv";
		
		double[][] data;
		
		int number_of_sensors = 8;
		
		int number_of_rounds = 200;
		
		int number_of_trials = 1;
		
		boolean calculate_p_values = true;
		
		boolean print_simulation_results = true;
		
		boolean print_each_step = false;
		
		if(use_random_data){
			
			data = new double[number_of_sensors][number_of_rounds];
			
		}else{
			
			ToolBox tool = new ToolBox();
			
			data = tool.csvReader(file_path);
			
			number_of_sensors = data.length;
			
			number_of_rounds = data[0].length;
			
		}

		double[][] simulation_data = new double[6][number_of_trials];
		
		for(int trial_num =0; trial_num <number_of_trials; trial_num++){
			
			if(use_random_data){
				
				double seed = 25;
				
				double change_gradient = 0.01;
				
				boolean was_up = false;
	
				Random random = new Random();
				
				for(int r=0; r<number_of_rounds; r++){
					
					for(int id =0; id<number_of_sensors; id++){
						
						seed += random.nextDouble() * (seed*change_gradient);
						if(random.nextDouble()<0.5) was_up = !was_up;
						if(was_up){
							was_up = !was_up;
							seed -= random.nextDouble() * (seed*change_gradient);
						}else{
							was_up = !was_up;
							seed += random.nextDouble() * (seed*change_gradient);
						}
						data[id][r] = seed;
						//data[id][r] =20+Math.abs(random.nextDouble());
						//data[id][r] = 20;
					}
				}
			}
			
			Server LSserver = new LSDSCServer();
			
			Sensor[] LSsensors = new Sensor[number_of_sensors];
			
			for(int i =0; i<number_of_sensors; i++){
				
				LSsensors[i] = new LSDSCSensor();
			}
			
			long start = System.currentTimeMillis();
			
			if(print_simulation_results) System.out.println("Source Coding with Neural Network AND Leader Selection \n");
			
			Simulator LSsimulator = new Simulator(number_of_sensors, number_of_rounds, LSserver, LSsensors,data, print_simulation_results, print_each_step);
			
			if(print_simulation_results) {System.out.println(""); System.out.println("Time Taken: "+(System.currentTimeMillis() - start)+" ms"); }
			
			Server NNserver = new NNDSCServer();
			
			Sensor[] NNsensors = new Sensor[number_of_sensors];
			
			for(int i =0; i<number_of_sensors; i++){
				
				NNsensors[i] = new NNDSCSensor();
			}
			
			if(print_simulation_results) System.out.println("Source Coding with Neural Network\n");
			
			start = System.currentTimeMillis();
			
			Simulator NNsimulator = new Simulator(number_of_sensors, number_of_rounds, NNserver, NNsensors,data, print_simulation_results, print_each_step);
			
			if(print_simulation_results) {System.out.println(""); System.out.println("Time taken: "+(System.currentTimeMillis() - start)+" ms");}
			
			Server DSserver = new DSCServer();
			
			Sensor[] DSsensors = new Sensor[number_of_sensors];
			
			for(int i =0; i<number_of_sensors; i++){
				
				DSsensors[i] = new DSCSensor();
			}
			
			if(print_simulation_results) System.out.println("Original Source Coding\n");
			
			start = System.currentTimeMillis();
			
			Simulator DSsimulator = new Simulator(number_of_sensors, number_of_rounds, DSserver, DSsensors,data, print_simulation_results, print_each_step);
			
			if(print_simulation_results){System.out.println("Time Taken: "+(System.currentTimeMillis() - start)+" ms");}
			
			if(calculate_p_values){
				
				simulation_data[0][trial_num] = DSsimulator.getTotalBits();
				simulation_data[1][trial_num] = DSsimulator.getTotalError();
				simulation_data[2][trial_num] = NNsimulator.getTotalBits();
				simulation_data[3][trial_num] = NNsimulator.getTotalError();
				simulation_data[4][trial_num] = LSsimulator.getTotalBits();
				simulation_data[5][trial_num] = LSsimulator.getTotalError();
			}
		}
		
		//System.out.println(t.quantile(0.75, 1));// According to http://en.wikipedia.org/wiki/Student's_t-distribution#Table_of_selected_values, it should return 1
		if(calculate_p_values){
			
			ToolBox tool_box  = new ToolBox();
			
			double[] DS_NN = tool_box.pairedTTest(simulation_data[0], simulation_data[1], simulation_data[2], simulation_data[3]);
			double[] NN_LS = tool_box.pairedTTest(simulation_data[2], simulation_data[3], simulation_data[4], simulation_data[5]);
			
			
			System.out.println("one tail P VALUE FOR BITS for Source Coding with Neural net against Original: "+DS_NN[0]);
			System.out.println("one tail P VALUE FOR ERROR for Source Coding with Neural net against Original: "+DS_NN[1]);
			System.out.println("Level of significance that Source Coding with Neural Net is use less bits than Original Source Coding: "+(DS_NN[0])*100+"% ");
			System.out.println("Level of significance that Source Coding with Neural net is has less error sum than Original Source Coding: "+(DS_NN[1])*100+"% in terms of error");
			

			System.out.println("one tail P VALUE FOR BITS for Source Coding with Neural net AND LS  against Source Coding with Neural net: "+NN_LS[0]);
			System.out.println("one tail P VALUE FOR ERROR for Source Coding with Neural net AND LS  against Source Coding with Neural net: "+NN_LS[1]);
			System.out.println("Level of significance that Source Coding with Neural net and LS is use less bits than Source Coding with Neural net: "+(NN_LS[0])*100+"% ");
			System.out.println("Level of significance that Source Coding with Neural net and LS is has less error sum than Source Coding with Neural net: "+(NN_LS[1])*100+"% in terms of error");
		}
		
	}

}
