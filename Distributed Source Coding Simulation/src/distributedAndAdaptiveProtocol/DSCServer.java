package distributedAndAdaptiveProtocol;

import java.lang.reflect.*;
import java.util.*;

import utilities.*;

import Jama.*;

public class DSCServer implements stimulator.Server{
	
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
	 * <br>Coefficient matrix </br>
	 * 
	 * <br> Rows(first coordinate) are the number of sensors (coefficient)</br>
	 * <br> Columns(second coordinate) are past data (coefficient) + other sensors (coefficient)</br>
	 * <br> We add one more entry denoting the its own reading, but it will be 0-ed in multiplication</br>
	 * */
	Matrix heta;
	
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
	 * <br>meu in the paper is the learning rate</br>
	 * */
	double meu = 0.0001;
	
	/**
	 * <br>Keeps the requested number of bits</br>
	 * */
	int[] requested_bits;
	
	boolean[] obtained_sensor_reading;
	
	CodeBook code_book = new CodeBook();
	
	@Override
	public void initialize(int number_of_sensors) {
		
		this.number_of_sensors = number_of_sensors;
		this.is_first_round = new boolean[this.number_of_sensors];
		Arrays.fill(is_first_round, true);
		
		current_sensor_readings = new double[this.number_of_sensors];
		obtained_sensor_reading = new boolean[this.number_of_sensors];
		past_sensor_readings = new double[this.number_of_sensors][this.M];
		
		heta = Matrix.random(number_of_sensors, M + number_of_sensors);
		
		predicted_sensor_readings = new double[this.number_of_sensors][1];
		sigma = new double[this.number_of_sensors];
		requested_bits = new int[number_of_sensors];
		
		diff = new double[number_of_sensors];
	}
	
	/**
	 * <br>Updates coefficients, heta</br>
	 * */
	private void updateWeights(){
		
		double[][] change_matrix = new double[number_of_sensors][M+number_of_sensors];
		
		for(int sensor_id=0; sensor_id<number_of_sensors; sensor_id++){
			
			double N = current_sensor_readings[sensor_id] - predicted_sensor_readings[sensor_id][0];
			for(int t=0; t<M;t++){
				
				change_matrix[sensor_id][t] = meu * N * past_sensor_readings[sensor_id][t];
			}
			for(int i=0; i<number_of_sensors; i++){
				
				change_matrix[sensor_id][i+M] = meu * N * current_sensor_readings[i];
			}
		}
		
		heta.plusEquals(new Matrix(change_matrix));
	}
	
	/**
	 * <br>Updates sigma of sensor j, variance of prediction</br>
	 * <br>Assumes that the prediction was updated</br>
	 * */
	private void updateVarianceOfPrediction(int sensor_id){
		
		double N = current_sensor_readings[sensor_id] - predicted_sensor_readings[sensor_id][0];
		if(round_number==0){
			
			sigma[sensor_id] = N * N;
			
		}else if(round_number<=K){
			
			sigma[sensor_id] = (((round_number-1)*sigma[sensor_id]) + (N * N))/(round_number);
			
		}else{
			
			sigma[sensor_id] = (1-gamma)*sigma[sensor_id] + gamma * N * N;
		}
	}
	
	/**
	 * <br>These are the Y's in the paper, updates the prediction</br>
	 * */
	private void calculatePrediction(int sensor_id){
		
		predicted_sensor_readings[sensor_id][0] = (heta.getMatrix(sensor_id, sensor_id,0,M + number_of_sensors-1).times(getFeatureMatrix(sensor_id))).get(0, 0);
		
	}
	
	/**
	 * <br>Get feature matrix, this is the Z described in the paper</br>
	 * */
	private Matrix getFeatureMatrix(int sensor_id){
		
		double[][] matrix_shell = new double[this.M + this.number_of_sensors][1];
		
		for(int i=0; i<this.M; i++){
			
			double temp_value = past_sensor_readings[sensor_id][i];
			
			matrix_shell[i][0] = temp_value;

		}
		
		for(int i=0; i<sensor_id; i++){
			
			double temp_value = current_sensor_readings[i];
			
			matrix_shell[i+this.M][0] = temp_value;
		}
		
		return new Matrix(matrix_shell);
	}
	
	/**
	 * <br>Records the current </br>
	 * */
	public void updatePastReadings(double new_reading, int id){
		
		for(int i=(M-1-1); i>=0;i--){
			
			past_sensor_readings[id][i+1] = past_sensor_readings[id][i] ;
		}
		
		past_sensor_readings[id][0] = new_reading;
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
		
		if(is_first_round[sensor_id] || sensor_id ==0 || round_number<M){
			
			requested_bits[sensor_id] = maximum_number_of_bits_askable;
			
			return "111111";
		}
		
		double delta = code_book.getD();
		
		int i = (int) Math.ceil( ( 0.5 * ( Math.log( (sigma[sensor_id] * sigma[sensor_id] )/ ( delta * delta * P_e) ) / Math.log(2.0) ) ) +1 );
		i = Math.min(i, maximum_number_of_bits_askable); //request not more that 63 bits
		i = Math.max(0,  i);//request no less than 0 bits
		
		requested_bits[sensor_id] = i;
		
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
		
		
		if(id!=0 &&round_number>=K && data.length() != requested_bits[id]+1  && data.length() !=0) return false;
		
		if(!is_first_round[id] && id!=0 && ! obtained_sensor_reading[0]) return false;
		
		//first_reading / first sensor everybody is un-coded, and is assumed to be correct
		if(round_number<M||is_first_round[id] || id==0 || data.length()>=this.maximum_number_of_bits_askable){
			
			is_first_round[id] = false;
			
			current_sensor_readings[id] = tools.binaryToDouble(data);
			
			obtained_sensor_reading[id] = true;
			
		}else{
			
			current_sensor_readings[id] = code_book.getDecodedValue(current_sensor_readings[0], data);
			obtained_sensor_reading[id] = true;
		}
		
		//calculate Y, prediction values, only after M readings
		if(round_number>=M){
			
			double temp = current_sensor_readings[id];
			updatePastReadings(temp, id);
			
			
		}else{
			
			double temp_value = current_sensor_readings[id];
			
			past_sensor_readings[id][M-round_number-1] = temp_value;
		}
		
		this.calculatePrediction(id);
		this.updateVarianceOfPrediction(id);
		diff[id] = current_sensor_readings[id]- predicted_sensor_readings[id][0];
		boolean all_received = true;
		for(int i=0; i<number_of_sensors; i++){
			
			if( ! obtained_sensor_reading[i]){
				
				all_received = false; break;
			}
		}
		if(all_received){
			Arrays.fill(obtained_sensor_reading, false);
			
			if(round_number<=K) this.updateWeights();
			
			round_number++;
			
			
			double average=0.0;
			for(int i=0; i<number_of_sensors; i++){
				
				average += Math.abs(diff[i]);
			}
			
		}
		
		return true;//received the data
	}

	@Override
	public double timeToLink() {
		// TODO Auto-generated method stub
		return 0;
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
