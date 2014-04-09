package neuralNetDistributedAndAdaptiveProtocol;

import java.util.*;

import org.encog.ml.data.*;
import org.encog.ml.data.basic.*;
import org.encog.neural.data.*;
import org.encog.neural.data.basic.*;

import utilities.*;

/**
 * <br>The is a list of labeled data set</br>
 * <br>It acts as an window that keeps a fixed size list of data</br>
 * <br> Once the limit reaches, the <b>oldest</b> data point is discarded</br>
 * **/
public class OrderedDataSet {
	
	private ArrayList<double[]> input_data = new ArrayList<double[]>();
	private ArrayList<double[]> ideal_data = new ArrayList<double[]>();
	
	private int maximum_size;
	
	public int size(){
		
		return input_data.size();
	}
	
	private double average(double[] values){
		
		double total = 0.0;
		
		for(int i=0; i<values.length; i++){
			
			total+=values[i];
			
			//System.out.println(i+". total: "+total+" values: "+values[i] );
		}
		
		return total/values.length;
	}
	
	private double std(double[] values,double average){
		
		double sum_squares = 0.0;
		
		for(int i=0; i<values.length; i++){
			
			sum_squares += (values[i] - average) *(values[i] - average) ;
		}
		
		if(sum_squares == 0){
			//System.out.println("Returning");
			return 1.0;
		}
		
		return Math.sqrt(sum_squares);
	}
	
	/**
	 * <br>The first coordinate is <b>AVERAGE</b></br>
	 * <br>The second coordinate is <b>STANDARD DEVIATION</b></br>
	 * 
	 * <br>The first object in the Arraylist is the <b>input</b></br>
	 * <br>The second object in the Arraylist is the <b>ideal</b></br>
	 * */
	public double[] getAverageAndStD(double[] values){
		
		double average = average(values);
		
		//System.out.println("AVERAGE : "+average);
		
		return new double[] {average, std(values, average)};
	}
	
	private void calculateStats(double[][] input, double[][] ideal, int j){
		
			double[] temp = new double[input.length];
			
			if(j<input[0].length){
				
				for(int i=0; i<input.length; i++){
					
					temp[i] = input[i][j];
					
					//System.out.println("< Before: "+temp[i]);
				}
				
			}else{
				
				for(int i=0; i<ideal.length; i++){
					
					temp[i] = ideal[i][j-input[0].length];
					
					//System.out.println("Before: "+temp[i]);
				}
			}
			
			//for(int i=0; i<temp.length; i++) System.out.println("Temp: "+temp[i]);
			
			stats[j] = getAverageAndStD(temp);
	}
	
	public void printStats(){
		
		System.out.println("THIS IS STATS***************************");
		for(int i=0; i<stats.length; i++){
			for(int j=0; j<stats[i].length; j++){
				
				System.out.print(stats[i][j]+", ");
			}
			System.out.println("");
		}
		System.out.println("THIS IS STATS***************************");
	}
	

	double[][] stats ;
	public ArrayList<double[][]> normalize(double[][] input, double[][] ideal, boolean reCalculateStats){
		
		if(reCalculateStats) stats = new double[input[0].length+ideal[0].length][2];
		
		for(int j=0; j<input[0].length+ideal[0].length; j++){
			
			if(reCalculateStats) calculateStats(input, ideal,j);
			
			if(j<input[0].length){
				
				for(int i=0; i<input.length; i++){
					
					input[i][j] = ( (input[i][j] - stats[j][0]) / stats[j][1] ) + 0.5 ;
					
					//System.out.println(j+". Input: "+input[i][j]+" average : "+stats[j][0]+" std: "+stats[j][1]);
				}
				
			}else{
				
				for(int i=0; i<input.length; i++){
					
					ideal[i][j-input[0].length] = ( (ideal[i][j-input[0].length] - stats[j][0]) / stats[j][1] ) +0.5;
					
					//System.out.println("Ideal: "+ideal[i][j-input[0].length]+" average : "+stats[j][0]+" std: "+stats[j][1]);
				}
				
			}
			
		}
		
		ArrayList<double[][]> stuff = new ArrayList<double[][]>();
		/*
		System.out.println("INPUT "+reCalculateStats);
		for(int i=0; i<input.length; i++){
			for(int j=0; j<input[0].length; j++){
				
				System.out.print(input[i][j]+", ");
			}
			System.out.println("");
		}
		*/
		stuff.add(input);
		
		stuff.add(ideal);
		
		return stuff;
	}
	
	public double unnormalize(double normalized_value, int feature_number){
		
		return ( (normalized_value-.5) * stats[feature_number][1]) + stats[feature_number][0];
	}
	
	
	public OrderedDataSet(int maximum_size){
		
		this.maximum_size = maximum_size;
	}
	
	public void addDataPoint(double[] input, double ideal ){
		
		if(input_data.size()>maximum_size){
			
			input_data.remove(0);
			ideal_data.remove(0);
		}
		
		double[] ideal_0 = new double[]{ideal};
		
		//data.add(new BasicMLDataPair(new BasicMLData(input),new BasicMLData(ideal_0)));
		
		input_data.add(input);
		ideal_data.add(ideal_0);
	}
	
	public BasicNeuralDataSet getTrainingSet(){
		//System.out.println("Getting TrainingSet, In ordered data set, Before normalization");
		
		ToolBox tools = new ToolBox();
		
		double[][] input_array = tools.makeCopy(input_data.toArray(new double[input_data.size()][]));//TODO these should be made into another copy
		double[][] ideal_array = tools.makeCopy(ideal_data.toArray(new double[ideal_data.size()][]));
		/*
		for(int i=0; i<input_array.length; i++){
			
			System.out.print("Adding input: ");
			printArray(input_array[i]);
			System.out.print(" : ");
			printArray(ideal_array[i]);
			System.out.println("");
		}
		
		System.out.println("NORMALIZE");
		*/
		ArrayList<double[][]> normalized_data = normalize(input_array,ideal_array ,true);//TODO this function normalizes the ORIGINAL data in <input_data> and <ideal_input>
		
		double[][] input = normalized_data.get(0);
		double[][] ideal = normalized_data.get(1);
		
		BasicNeuralDataSet dataset = new BasicNeuralDataSet();
		
		for(int i=0; i<input.length; i++){
			/*
			System.out.print("Adding input: ");
			printArray(input[i]);
			System.out.print(" : ");
			printArray(ideal[i]);
			System.out.println("");
			*/
			dataset.add(new BasicMLData(input[i]), new BasicMLData(ideal[i]));
		}
		
		return dataset;
	}
	
	private void printArray(double[] a){
		for(int i=0; i<a.length; i++){
			
			System.out.print(a[i]+", ");
		}
		System.out.println("");
	}
}
