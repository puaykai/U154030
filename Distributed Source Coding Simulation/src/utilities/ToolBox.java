package utilities;

import java.io.*;
import java.util.*;

import DistLib.*;

public class ToolBox {
	
	/******
	 * <br>First item is PVALUE ON BIT</br>
	 * <br>Second item is PVALUE ON ERROR</br>
	 * ****/
	public double[] pairedTTest(double[] total_bit_data_1, double[] total_error_data_1, double[] total_bit_data_2, double[] total_error_data_2){
		
		int number_of_trials = total_bit_data_1.length;
		

		double mean_total_bit_DS=0.0;
		double mean_total_bit_NN=0.0;
		double mean_total_error_DS = 0.0;
		double mean_total_error_NN = 0.0;
		
		for(int trial_num = 0; trial_num<number_of_trials; trial_num++){
			
			mean_total_bit_DS += total_bit_data_1[trial_num];
			mean_total_error_DS +=total_error_data_1[trial_num];
			mean_total_bit_NN +=total_bit_data_2[trial_num];
			mean_total_error_NN +=total_error_data_2[trial_num];
		}
		
		mean_total_bit_DS /= number_of_trials;
		mean_total_error_DS /= number_of_trials;
		mean_total_bit_NN /= number_of_trials;
		mean_total_error_NN /= number_of_trials;
		
		double variance_total_bit_DS=0;
		double variance_total_error_DS=0;
		double variance_total_bit_NN =0;
		double variance_total_error_NN =0;
		
		for(int trial_num =0; trial_num<number_of_trials; trial_num++){
			
			variance_total_bit_DS += (total_bit_data_1[trial_num] - mean_total_bit_DS) * (total_bit_data_1[trial_num] - mean_total_bit_DS);
			variance_total_error_DS += (total_error_data_1[trial_num] - mean_total_error_DS) * (total_error_data_1[trial_num] - mean_total_error_DS);
			variance_total_bit_NN +=(total_bit_data_2[trial_num] - mean_total_bit_NN) * (total_bit_data_2[trial_num] - mean_total_bit_NN);
			variance_total_error_NN += (total_error_data_2[trial_num] - mean_total_error_NN) * (total_error_data_2[trial_num] - mean_total_error_NN);
		}
		
		variance_total_bit_DS /= number_of_trials;
		variance_total_error_DS /= number_of_trials;
		variance_total_bit_NN /= number_of_trials;
		variance_total_error_NN /= number_of_trials;
		
		double difference_variance_total_bits = variance_total_bit_DS/number_of_trials + variance_total_bit_NN/number_of_trials;
		double difference_variance_total_error = variance_total_error_DS/number_of_trials + variance_total_error_NN/number_of_trials;
		
		double degree_of_freedom_total_bits = difference_variance_total_bits / ( ( (variance_total_bit_DS/(number_of_trials)) * (variance_total_bit_DS/(number_of_trials)) ) / (number_of_trials-1) +  ( (variance_total_bit_NN/(number_of_trials)) * (variance_total_bit_NN/(number_of_trials)) ) / (number_of_trials-1) );
		double degree_of_freedom_total_error = difference_variance_total_error / ( ( (variance_total_error_DS/(number_of_trials)) * (variance_total_error_DS/(number_of_trials)) ) / (number_of_trials-1) +  ( (variance_total_error_NN/(number_of_trials)) * (variance_total_error_NN/(number_of_trials)) ) / (number_of_trials-1) );
		
		double t_statistic_total_bits = (mean_total_bit_DS - mean_total_bit_NN)/Math.sqrt(difference_variance_total_bits);
		double t_statistic_total_error = (mean_total_error_DS - mean_total_error_NN) / Math.sqrt(difference_variance_total_error);
		
		double p_value_total_bits = t.density(t_statistic_total_bits, degree_of_freedom_total_bits);
		double p_value_total_error = t.density(t_statistic_total_error, degree_of_freedom_total_error);
		
		return new double[]{p_value_total_bits, p_value_total_error};
	}
	
	public double[] makeCopy(double[] array){
		
		double[] copy = new double[array.length];
		for(int i=0; i<array.length; i++){
			
			copy[i] = array[i];
		}
		return copy;
	}
	
	public double[][] makeCopy(double[][] array){
		
		double[][] copy = new double[array.length][array[0].length];
		for(int i=0; i<array.length; i++)
			for(int j=0; j<array[0].length; j++)
				copy[i][j] = array[i][j];
		
		return copy;
	}
	
	public String reverse(String s){
		
		return new StringBuffer(s).reverse().toString();
	}
	
	public String pad0ToFront(String s, int expected_length){
		
		int current_length = s.length();
		
		for(int i=0; i<(expected_length-current_length); i++){
			
			s = "0" + s;
			
			
		}
		
		return s;
	}
	
	public double[][] csvReader(String file_path){
		

		String csvFile = file_path;
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		
		ArrayList<double[]> temporary = new ArrayList<double[]>();
	 
		try {
	 
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
	 
			        // use comma as separator
				String[] readings = line.split(cvsSplitBy);
				
				double[] row = new double[readings.length];
	 
				for(int i=0; i<readings.length; i++){
					
					//System.out.print(readings[i]+",");
					
					row[i] = Double.parseDouble(readings[i]);
					
					temporary.add(row);
				}
				//System.out.println(" ["+readings.length+"]");
	 
			}
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	 
		System.out.println("Done reading file");
		
		double[][] temporary_2 = new double[temporary.size()][temporary.get(0).length];
		
		double[][] to_return = new double[temporary.get(0).length][temporary.size()];
		
		for(int i=0; i<temporary.size(); i++){
			
			temporary_2[i] = temporary.get(i);
		}
		
		for(int i=0; i<to_return.length; i++){
			
			for(int j=0; j<to_return[0].length; j++){
				
				to_return[i][j] = temporary_2[j][i];
			}
		}
		
		return to_return;
	}
	
	
	public String doubleToBinaryString(double reading){
		
		return pad0ToFront(Long.toBinaryString(Double.doubleToRawLongBits(reading)),64);
	}
	
	public double binaryToDouble(String binary){
		
		if(binary.startsWith("1")){
			
			binary = binary.substring(1);
			
			if(binary.contains("1"))binary = binary.substring(binary.indexOf("1"));
			
			return -Double.longBitsToDouble(Long.parseLong(binary,2));
			
		}else{
			
			if(binary.contains("1"))binary = binary.substring(binary.indexOf("1"));
			
			return Double.longBitsToDouble(Long.parseLong(binary,2));
		}
	}
	public static void main(String[] args){
		
		ToolBox tools = new ToolBox();
		
		double reading = 1;
		
		String obinary = Long.toBinaryString(Double.doubleToRawLongBits(reading));
		
		System.out.println("obinary:"+obinary);
		
		
		String binary = tools.doubleToBinaryString(reading);
		
		System.out.println(binary);
		
		System.out.println("Length of binary: "+binary.length());
		
		System.out.println(tools.binaryToDouble(binary));
		double readinga = -83745.9743;
		
		
		
		String binarya = tools.doubleToBinaryString(readinga);
		
		
		
		System.out.println(binarya);
		
		System.out.println("Length of binary: "+binarya.length());

		System.out.println(tools.binaryToDouble(binarya));
		//System.out.println(Double.longBitsToDouble(Long.parseLong(binary, 2)));
	}

	
}
