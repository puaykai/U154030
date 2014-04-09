package neuralNetDistributedAndAdaptiveProtocol;

import java.util.*;

import org.encog.engine.network.activation.*;
import org.encog.ml.data.*;
import org.encog.ml.data.basic.*;
import org.encog.neural.data.*;
import org.encog.neural.data.basic.*;
import org.encog.neural.networks.*;
import org.encog.neural.networks.layers.*;
import org.encog.neural.networks.training.*;
import org.encog.neural.networks.training.propagation.resilient.*;

public class Example {
	
	private double average(double[] values){
		
		double total = 0.0;
		
		for(int i=0; i<values.length; i++){
			
			total+=values[i];
			
			//System.out.println("total: "+total+" values: "+values[i] );
		}
		
		return total/values.length;
	}
	
	private double std(double[] values,double average){
		
		double sum_squares = 0.0;
		
		for(int i=0; i<values.length; i++){
			
			sum_squares += (values[i] - average) *(values[i] - average) ;
		}
		
		if(sum_squares == 0) return 1.0;
		
		return Math.sqrt(sum_squares);
	}
	
	double[][] stats_col;
	
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
	
	public ArrayList<double[][]> normalize_column(double[][] input, double[][] ideal){
		
		stats_col = new double[input.length][2];
		
		//assume that input and ideal are aligned row wise
		for(int i=0; i<input.length; i++){
			
			double[] values = new double[input[i].length+ideal[i].length];
			
			for(int j=0; j<input[i].length; j++){
				
				values[j] = input[i][j];
				
				//System.out.print(input[i][j]+", ");
			}
			//System.out.print(" : ");
			
			for(int j=0; j<ideal[i].length;j++){
				
				//System.out.print(ideal[i][j]+",");
				values[j+input[i].length] = ideal[i][j];
			}
			//System.out.println("");
			
			stats_col[i] = getAverageAndStD(values);
			
			for(int j=0; j<input[i].length; j++){
				
				//input[i][j] = Math.max( Math.min( (input[i][j] - stats[i][0]) / stats[i][1] , -3*stats[i][1]) , 3* stats[i][1] );
				input[i][j] = (input[i][j] - stats_col[i][0]) / stats_col[i][1];
				//System.out.print(input[i][j]+", ");
			}
			//System.out.print(" : ");
			
			for(int j=0; j<ideal[i].length;j++){
				
				//System.out.print(ideal[i][j]+",");
				//ideal[i][j] = Math.max( Math.min( (input[i][j] - stats[i][0]) / stats[i][1] , -3*stats[i][1]) , 3* stats[i][1] );
				ideal[i][j] = (ideal[i][j] - stats_col[i][0]) / stats_col[i][1];
			}
			//System.out.println("");
		}
		
		ArrayList<double[][]> stuff = new ArrayList<double[][]>();
		
		stuff.add(input);
		stuff.add(ideal);
		
		return stuff;
	}

	/**
	 * <br>Assumes that the normalize is called</br>
	 * **/
	public ArrayList<double[][]> unnormalize_column(double[][] input, double[][] ideal){
		
		for(int i=0; i<input.length; i++){
			
			for(int j=0; j<input[i].length; j++){
				
				input[i][j] = (input[i][j] * stats[i][1]) + stats[i][0];
			}
			for(int j=0; j<ideal[i].length;j++){
				
				ideal[i][j] = (ideal[i][j] * stats[i][1]) + stats[i][0];
			}
		}
		
		ArrayList<double[][]> stuff = new ArrayList<double[][]>();
		
		stuff.add(input);
		stuff.add(ideal);
		
		return stuff;
	}

	public double unnormalize_col(double normalized_value, int index){
		
		return (normalized_value * stats_col[index][1]) + stats_col[index][0];
	}
	
	double[][] stats ;
	public ArrayList<double[][]> normalize(double[][] input, double[][] ideal){
		
		stats = new double[input[0].length+ideal[0].length][2];
		
		for(int j=0; j<input[0].length+ideal[0].length; j++){
			
			double[] temp = new double[input.length];
			
			if(j<input[0].length){
				
				for(int i=0; i<input.length; i++){
					
					temp[i] = input[i][j];
					
					System.out.println("< Before: "+temp[i]);
				}
				
			}else{
				
				for(int i=0; i<ideal.length; i++){
					
					temp[i] = ideal[i][j-input[0].length];
					
					System.out.println("Before: "+temp[i]);
				}
			}
			
			//for(int i=0; i<temp.length; i++) System.out.println("Temp: "+temp[i]);
			
			stats[j] = getAverageAndStD(temp);
			
			if(j<input[0].length){
				
				for(int i=0; i<input.length; i++){
					
					input[i][j] = ( (input[i][j] - stats[j][0]) / stats[j][1] ) + 0.5 ;
				}
				
			}else{
				
				for(int i=0; i<input.length; i++){
					
					ideal[i][j-input[0].length] = ( (ideal[i][j-input[0].length] - stats[j][0]) / stats[j][1] ) +0.5;
					
					System.out.println("Ideal: "+ideal[i][j-input[0].length]+" average : "+stats[j][0]+" std: "+stats[j][1]);
				}
				
			}
			
		}
		
		ArrayList<double[][]> stuff = new ArrayList<double[][]>();
		
		stuff.add(input);
		
		stuff.add(ideal);
		
		return stuff;
	}
	
	public double unnormalize(double normalized_value, int feature_number){
		
		return ( (normalized_value-.5) * stats[feature_number][1]) + stats[feature_number][0];
	}
	
	public static void main(String[] args){
		Example example = new Example();
		
		double XOR_INPUT[][] = { 
			  { 0.0, 0.0 }, 
			  { 20.0, 0.0 },
			  { 0.0, 20.0 }, 
			  { 20.0, 20.0 } };
		
		
		double XOR_IDEAL[][] = { 
			  { 0.0 }, 
			  { 20.0 }, 
			  { 20.0 }, 
			  { 0.0 } };
		
		ArrayList<double[][]> stuff = example.normalize(XOR_INPUT, XOR_IDEAL);
		
		//ArrayList<double[][]> result = example.unnormalize(stuff.get(0), stuff.get(1));
		
		//print result
		for(int i=0; i<stuff.get(0).length; i++){
			
			for(int j=0; j<stuff.get(0)[i].length;j++){
				
				System.out.print(stuff.get(0)[i][j]+", ");
			}
			
			System.out.print(" : ");
			
			for(int j=0; j<stuff.get(1)[i].length;j++){
				
				System.out.print(stuff.get(1)[i][j]+", ");
			}
			System.out.println("");
		}
		
		
		
		for(int row = 0; row<XOR_INPUT.length; row++){
			for(int col = 0; col<XOR_INPUT[row].length;col++){
				System.out.print(XOR_INPUT[row][col]+",");
			}
			System.out.println("");
		}
		
		
		
		NeuralDataSet trainingSet = new BasicNeuralDataSet(stuff.get(0), stuff.get(1));
		BasicNetwork network = new BasicNetwork();

		network.addLayer(new BasicLayer(new ActivationSigmoid(), true,2));
		network.addLayer(new BasicLayer(new ActivationSigmoid(), true,64));
		network.addLayer(new BasicLayer(new ActivationSigmoid(), true,1));
		network.getStructure().finalizeStructure();
		network.reset();
		
		Train train = new ResilientPropagation(network, trainingSet);
		
		int epoch = 1;
		int max_iter = 1024;
		do {

		  train.iteration();
		  System.out.println("Epoch #" + epoch + 
		                     " Error:" + train.getError());
		  epoch++;

		} while(train.getError() > 0.001 && epoch<max_iter);
		
		
		
		System.out.println("Neural Network Results:");
		
		for(int i=0; i<trainingSet.size(); i++){
			
			MLDataPair pair = trainingSet.get(i);
			
			final MLData output = network.compute(pair.getInput());
			
			double data_0 =  example.unnormalize(pair.getInput().getData(0), 0);
			double data_1 =  example.unnormalize(pair.getInput().getData(1), 1);
			double actual =  example.unnormalize(output.getData(0), 2);
			double ideal =  example.unnormalize(pair.getIdeal().getData(0), 2);
			
			
			/*
			  System.out.println( example.unnormalize_col(pair.getInput().getData(0),i) + 
			         "," + example.unnormalize_col(pair.getInput().getData(1),i)  + 
			         ", actual=" + example.unnormalize_col(output.getData(0),i) + 
			         ",ideal=" +example.unnormalize_col( pair.getIdeal().getData(0),i));
			  */
				System.out.println( data_0+ 
			         "," +data_1 + 
			         ", actual=" +actual + 
			         ",ideal=" +ideal);
		}
		
		/*
		for(MLDataPair pair: trainingSet ) {

		  final MLData output = network.compute(pair.getInput());
		  System.out.println(pair.getInput().getData(0) + 
		         "," + pair.getInput().getData(1)  + 
		         ", actual=" + output.getData(0) + 
		         ",ideal=" + pair.getIdeal().getData(0));

		}
		*/
		
	}
	
}
