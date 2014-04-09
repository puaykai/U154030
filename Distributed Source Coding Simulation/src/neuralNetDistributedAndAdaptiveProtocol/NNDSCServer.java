package neuralNetDistributedAndAdaptiveProtocol;

import java.util.*;

import org.encog.engine.network.activation.*;
import org.encog.ml.data.*;
import org.encog.neural.data.basic.*;
import org.encog.neural.networks.*;
import org.encog.neural.networks.layers.*;
import org.encog.neural.networks.training.*;
import org.encog.neural.networks.training.propagation.resilient.*;

import utilities.*;
import Jama.*;
import distributedAndAdaptiveProtocol.*;

public class NNDSCServer implements stimulator.Server{


	ToolBox tools = new ToolBox();
	
	/**
	 * Number of past readings to remember
	 * */
	int M = 10;
	
	/**
	 * <br>In the paper, this one is the number of rounds used for gathering raw data.</br>
	 * <br> This has to be at least M</br>
	 * */
	int K = M + 40;
	/**
	 * <br>Probability of error given in the paper</br>
	 * */
	double P_e = 0.01;
	
	/**
	 * <br>gamma, forgetting factor</br>
	 * */
	double gamma = 0.5;
	
	private int number_of_sensors;
	
	/**
	 * This will check if it is the initialization round
	 * */
	boolean[] is_first_round;
	
	double[] current_sensor_readings;
	double[][] past_sensor_readings;
	double[][] predicted_sensor_readings;
	int maximum_number_of_bits_askable = 63;
	int bits_needed_to_represent_maximum_askable = 6;
	double[] diff;
	
	/**
	 * <br>Keeps track of the number of rounds, so that we will collect M rounds of data</br>
	 * */
	int round_number;
	
	/**
	 * <br>Variance of prediction, sigma</br>
	 * */
	double[] sigma;
	
	/**
	 * <br>Keeps the requested number of bits</br>
	 * */
	int[] requested_bits;
	
	boolean[] obtained_sensor_reading;
	
	CodeBook code_book = new CodeBook();
	
	/***
	 * <br>Each sensor has its own neural designated neural net in the server to make predictions</br>
	 * */
	BasicNetwork[] array_of_neural_nets;
	int number_of_nodes_in_middle_layer = 32;
	int amount_of_neural_net_input_to_keep = 32;
	OrderedDataSet[] input_datas;
	double error_allowed = 0.001;
	int max_epoch = 1024;
	
	@Override
	public void initialize(int number_of_sensors) {
		
		this.number_of_sensors = number_of_sensors;
		this.is_first_round = new boolean[this.number_of_sensors];
		Arrays.fill(is_first_round, true);
		
		current_sensor_readings = new double[this.number_of_sensors];
		obtained_sensor_reading = new boolean[this.number_of_sensors];
		past_sensor_readings = new double[this.number_of_sensors][this.M];
		
		//neural_net_input = new double[this.number_of_sensors][this.amount_of_neural_net_input_to_keep][];
		

		//**********************Neural Network Initialization
		
		array_of_neural_nets = new BasicNetwork[this.number_of_sensors];
		
		input_datas = new OrderedDataSet[this.number_of_sensors];
		
		for(int i=0; i<this.number_of_sensors; i++){
			
			array_of_neural_nets[i] = new BasicNetwork();
			
			array_of_neural_nets[i].addLayer(new BasicLayer(new ActivationSigmoid(),true,this.M+i));//input layer corresponds to number of features (amount of past readings to remember + amount of sensors before it)
			
			array_of_neural_nets[i].addLayer(new BasicLayer(new ActivationSigmoid(),true,number_of_nodes_in_middle_layer));
			
			array_of_neural_nets[i].addLayer(new BasicLayer(new ActivationSigmoid(),true,1));//the output layer, i.e. prediction of the sensor reading
			
			array_of_neural_nets[i].getStructure().finalizeStructure();
			
			array_of_neural_nets[i].reset();
			
			input_datas[i] = new OrderedDataSet(amount_of_neural_net_input_to_keep);
		}
		//**********************Neural Network Initialization
		
		predicted_sensor_readings = new double[this.number_of_sensors][1];
		sigma = new double[this.number_of_sensors];
		requested_bits = new int[number_of_sensors];
		
		diff = new double[number_of_sensors];
	}
	
	/***
	 * <br>Updates the input data to the neural networks</br>
	 * */
	private void updateInputData(int id){
		
		input_datas[id].addDataPoint(this.getFeature(id), this.current_sensor_readings[id]);
		//System.out.println("UPDATE INPUT DATA::::");
		//printArray(this.getFeature(id));
		//System.out.println(id+", Input data size: "+input_datas[id].size());
	}
	
	/**
	 * <br>Updates sigma of sensor j, variance of prediction</br>
	 * <br>Assumes that the prediction was updated</br>
	 * */
	private void updateVarianceOfPrediction(int sensor_id){
		
		double N = current_sensor_readings[sensor_id] - predicted_sensor_readings[sensor_id][0];
		//System.out.println("Sensor "+sensor_id+" Prediction error: "+N +" Original Sigma: "+sigma[sensor_id]);
		if(round_number==0){
			
			sigma[sensor_id] = N * N;
			
		}else if(round_number<=K){
			
			sigma[sensor_id] = (((round_number-1)*sigma[sensor_id]) + (N * N))/(round_number);
			
		}else{
			
			sigma[sensor_id] = (1-gamma)*sigma[sensor_id] + gamma * N * N;
		}
		//System.out.println("Sensor "+sensor_id+" New Sigma: "+sigma[sensor_id]);
	}
	
	/***
	 * <br> Updates the neural networks</br>
	 * <br> Assumes that the 
	 * **/
	private void trainNeuralNets(int id){
		
		Train train = new ResilientPropagation(array_of_neural_nets[id],input_datas[id].getTrainingSet());
		
		int epoch =0;
		//System.out.println("EPOCH: "+epoch+" Error: "+train.getError());
		while(train.getError()>error_allowed && epoch<max_epoch){
			
			//System.out.println("EPOCH: "+epoch+" Error: "+train.getError());
			
			train.iteration();
			epoch++;
		}
	}
	
	/**
	 * <br>These are the Y's in the paper, updates the prediction</br>
	 * */
	private void calculatePrediction(int sensor_id){
		//
		//System.out.println("Calculating Prediction for: "+sensor_id);
		double[][] input = getFeatureMatrix(sensor_id);
		double[][] normalized_input = input_datas[sensor_id].normalize(input, new double[][]{{1}}, false).get(0);
		/*
		for(int i=0; i< normalized_input.length; i++){
			for(int j=0; j<normalized_input[i].length; j++){
				
				System.out.print(normalized_input[i][j]+", ");
			}
			System.out.println("");
		}
		*/
		double normalized_data =array_of_neural_nets[sensor_id].compute(new BasicNeuralData(normalized_input[0])).getData(0);
		//double normalized_data = array_of_neural_nets[sensor_id].compute( (new BasicNeuralDataSet(getFeatureMatrix(sensor_id),new double[][]{{0}})).get(0).getInput() ).getData(0);
		
		predicted_sensor_readings[sensor_id][0] = input_datas[sensor_id].unnormalize(normalized_data, this.M+sensor_id);
		//(heta.getMatrix(sensor_id, sensor_id,0,M + number_of_sensors-1).times(getFeatureMatrix(sensor_id))).get(0, 0);
		//System.out.println("Predicted : "+predicted_sensor_readings[sensor_id][0]);
	}
	
	/**
	 * <br>Get feature matrix, this is the Z described in the paper</br>
	 * */
	private double[][] getFeatureMatrix(int sensor_id){
		
		double[][] matrix_shell = new double[1][this.M + sensor_id];
		
		for(int i=0; i<this.M; i++){
			
			double temp_value = past_sensor_readings[sensor_id][i];
			
			//System.out.println("past temp_value: "+temp_value);
			
			matrix_shell[0][i] = temp_value;

		}
		
		for(int i=0; i<sensor_id; i++){
			
			double temp_value = current_sensor_readings[i];
			
			//System.out.println("others temp_value: "+temp_value);
			
			matrix_shell[0][i+this.M] = temp_value;
		}
		
		//System.out.println("In the getFeatureMatrix");
		//printArray(matrix_shell);
		
		return matrix_shell;
	}
	
	private double[] getFeature(int sensor_id){
		
		double[] matrix_shell = new double[this.M + sensor_id];
		
		for(int i=0; i<this.M; i++){
			
			double temp_value = past_sensor_readings[sensor_id][i];
			
			//System.out.println("past temp_value: "+temp_value);
			
			matrix_shell[i] = temp_value;

		}
		
		for(int i=0; i<sensor_id; i++){
			
			double temp_value = current_sensor_readings[i];
			
			//System.out.println("others temp_value: "+temp_value);
			
			matrix_shell[i+this.M] = temp_value;
		}
		
		//System.out.println("In the getFeatureMatrix");
		//printArray(matrix_shell);
		
		return matrix_shell;
	}
	
	/**
	 * <br>Records the current </br>
	 * */
	public void updatePastReadings(double new_reading, int id){
		
		for(int i=(M-1-1); i>=0;i--){
			
			past_sensor_readings[id][i+1] = past_sensor_readings[id][i] ;
		}
		
		past_sensor_readings[id][0] = new_reading;
		//System.out.println("UPDATING PAST SENSOR : "+new_reading+" ; "+past_sensor_readings[id][0]);
	}

	/**
	 * <br> In the first round,</br>
	 * <br>In the paper the server is supposed to ask sensors for uncoded data.</br>
	 * <br>We sent the maximum number of bits: 63 bits = "111111"</br>
	 * <br> Sensor will encode in IEEE double format</br>
	 * <br> Calculates the i (number of requested bits), as given in the paper</br>
	 * */
	@Override
	public String sendRequest(int sensor_id) {
		
		//System.out.println("IN SERVER: sigma["+sensor_id+"] is "+sigma[sensor_id]);
		if(is_first_round[sensor_id] || sensor_id ==0 || round_number<M){
			
			requested_bits[sensor_id] = maximum_number_of_bits_askable;
			
			return "111111";
		}
		
		double delta = code_book.getD();
		
		int i = (int) Math.ceil( ( 0.5 * ( Math.log( (sigma[sensor_id] * sigma[sensor_id] )/ ( delta * delta * P_e) ) / Math.log(2.0) ) ) +1 );
		i = Math.min(i, maximum_number_of_bits_askable); //request not more that 63 bits
		i = Math.max(0,  i);//request no less than 0 bits
		
		requested_bits[sensor_id] = i;
		//System.out.println("Asking "+sensor_id +" to send "+ i+ " bits");
		
		String bit_string = tools.pad0ToFront(Integer.toBinaryString(i),bits_needed_to_represent_maximum_askable);//pad it to make length 6
		
		return tools.reverse(bit_string);
	}

	@Override
	public double timeToInitialize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean receiveData(String data, int id) {
		
		//System.out.println("requested_bits: "+requested_bits[id]);
		//System.out.println("received data length: "+data.length());
		
		if(id!=0 &&round_number>=K && data.length() != requested_bits[id]+1 && data.length() !=0){
			//System.out.println("HERE");
			 return false;
		}
		
		if(!is_first_round[id] && id!=0 && ! obtained_sensor_reading[0]){
			
			
			//System.out.println("THERE");
			 return false;
		}
		
		//first_reading / first sensor everybody is un-coded, and is assumed to be correct
		if(round_number<M||is_first_round[id] || id==0){
			
			is_first_round[id] = false;
			
			current_sensor_readings[id] = tools.binaryToDouble(data);
			
			obtained_sensor_reading[id] = true;
			
			//System.out.println("FULL decoded: "+current_sensor_readings[id]);
			
		}else{
			
			current_sensor_readings[id] = code_book.getDecodedValue(current_sensor_readings[0], data);
			//System.out.println(data);
			//System.out.println("Decoded Value: "+current_sensor_readings[id]);
			obtained_sensor_reading[id] = true;
		}
		
		//System.out.println("current_sensor_readings");
		//printArray(current_sensor_readings);
		//System.out.println("past_sensor_readings");
		//printArray(past_sensor_readings);
		
		//calculate Y, prediction values, only after M readings
		
		if(round_number>=(M-1)){
			
			double temp = current_sensor_readings[id];
			updatePastReadings(temp, id);
			
			this.updateInputData(id);
			this.trainNeuralNets(id);
			this.calculatePrediction(id);
			this.updateVarianceOfPrediction(id);
			
		}else{
			
			double temp_value = current_sensor_readings[id];
			
			past_sensor_readings[id][M-round_number-1] = temp_value;
		}
		
			
		//this.calculatePrediction(id);
		//this.updateVarianceOfPrediction(id);

		//System.out.println("Prediction "+predicted_sensor_readings[id][0]+" actual sensor reading: "+current_sensor_readings[id]+ " diff: "+ (current_sensor_readings[id]- predicted_sensor_readings[id][0]));
		diff[id] = current_sensor_readings[id]- predicted_sensor_readings[id][0];
		boolean all_received = true;
		for(int i=0; i<number_of_sensors; i++){
			
			if( ! obtained_sensor_reading[i]){
				
				//System.out.println("IN SERVER: Have not received from "+i);
				all_received = false; break;
			}
		}
		if(all_received){
			Arrays.fill(obtained_sensor_reading, false);
			
			//if(round_number<=K) this.updateWeights(); //TODO trains neural nets

			round_number++;
			
			//System.out.println("IN SERVER : ROUND NUMBER: "+round_number);
			
			double average=0.0;
			for(int i=0; i<number_of_sensors; i++){
				
				average += Math.abs(diff[i]);
			}
			//System.out.println("Total prediction error: "+average);
			
		}
		
		return true;//received the data
	}

	@Override
	public double timeToLink() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static void main(String[] args){
		double delta = 0.0001;
		//double sigma = 0.022727272726220802;
		double sigma = 0.005;
		double P_e = 0.01;
		
		int i = (int) Math.ceil( ( 0.5 * ( Math.log( (sigma * sigma )/ ( delta * delta * P_e) ) / Math.log(2.0) ) ) +1 );
		i = Math.min(i, 63); //request not more that 63 bits
		i = Math.max(0,  i);//request no less than 0 bits
		
		//System.out.println(i);
	}
	
	public static void  printArray(double[][] array){
		
		for(int row=0; row<array.length; row++){
			for(int col =0; col<array[row].length; col++){
				
				System.out.print(array[row][col]+", ");
			}
			System.out.println("");
		}
			
				
	}
	
	public static void printArray(double[] array){
		
		for(int row=0; row<array.length; row++){
			System.out.print(array[row]+", ");
		}
		System.out.println("");
	}

	@Override
	public double[] getReadingReceivedByServer() {
		
		return tools.makeCopy(current_sensor_readings);
	}
}
