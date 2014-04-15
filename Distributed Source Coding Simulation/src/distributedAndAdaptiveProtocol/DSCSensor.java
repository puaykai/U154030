package distributedAndAdaptiveProtocol;

import java.util.*;

import utilities.*;

public class DSCSensor implements stimulator.Sensor{
	
	ToolBox tools = new ToolBox();
	
	Random rand = new Random();
	CodeBook code_book = new CodeBook();
	
	int number_of_bits_requested;
	int maximum_number_of_bits_askable = 63;
	int number_of_bits_of_maximum_askable = 6;
	
	double current_reading;
	
	/**
	 * The internal data gathering function, gets the data and truncates according to the d value in the codebook
	 * */
	private double getNextDataPoint(){
		
		//current_reading = Math.abs(rand.nextDouble())+20;
		
		return  current_reading;
	}
	
	/**
	 * <br>The bits are read from left to right with the left most representing 2^0, right most 2^4.</br>
	 * <br>The length of the request are expected to be 5 bits long.</br>
	 * 
	 * <br>number of bits requested will not be 0, so</br>
	 *  <br>10000  - represents 1 and so on</br>
	 *  <br>If sensor receives all zeros, then it does not encode its reading</br>
	 * @returns false; if request is not exactly 5 bits long
	 * */
	@Override
	public boolean fetchRequest(String request) {
		
		if(request.length() != number_of_bits_of_maximum_askable) return false;
		
		number_of_bits_requested=0;
		
		for( int i=0; i<number_of_bits_of_maximum_askable;i++){
			
			number_of_bits_requested += (int) (Integer.parseInt(""+request.charAt(i))*Math.pow(2, i));
			
		}
		
		return true;
	}

	/**
	 * <br> Sends the bit string back to server</br>
	 * @return the bitstring (String) to be decoded
	 * */
	@Override
	public String sendData() {
		
		double next_reading = getNextDataPoint();
		
		if(number_of_bits_requested==0) return "";
		
		//Check if the server wants uncoded readings, then send the IEEE double bit string
		if(number_of_bits_requested == maximum_number_of_bits_askable){
			
			return tools.doubleToBinaryString(next_reading);
		}
		
		int modded_index = code_book.getIndex(next_reading) % ((int) Math.pow(2, number_of_bits_requested));
		
		return code_book.integerToBinaryString(modded_index, number_of_bits_requested);
	}

	@Override
	public double timeToInitialize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double timeToSend() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public double getReadingSentBySensor() {
		
		return current_reading;
	}

	@Override
	public void takeReadingToSend(double reading) {
		
		this.current_reading = reading;
	}
}
