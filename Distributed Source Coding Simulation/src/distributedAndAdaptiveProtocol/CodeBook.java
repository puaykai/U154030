package distributedAndAdaptiveProtocol;

import java.util.*;

import utilities.*;

public class CodeBook {
	
	private double d = 0.0001;//interval value
	
	/**
	 * @param reading (double) 
	 * @return index (int) of the reading in the codebook or the A/D converter in the paper
	 * */
	public int getIndex(double reading){
		
		return (int) Math.floor(reading/this.getD());
	}
	
	public String integerToBinaryString(int modded_index, int number_of_bits_requested){
		
		ToolBox tools = new ToolBox();
		
		boolean is_negative = modded_index<0;
		
		String binary = tools.pad0ToFront(Integer.toBinaryString(Math.abs(modded_index)), number_of_bits_requested);
		
		if(is_negative){
			
			binary = "1"+binary;
		}else{
			
			binary = "0"+binary;
		}
		
		return binary;
	}
	
	/**
	 * @param side information (double) the extra side information given
	 * @return decoded value
	 * */
	public double getDecodedValue(double side_information, String code_string){
		
		boolean start_with_1 = code_string.startsWith("1");
		
		if(code_string.length()<1) {return side_information;}
		
		code_string = code_string.substring(1);
		
		double delta = this.getD() * Math.pow(2, code_string.length());
		
		double offset = 0.0;
		
		for(int i=0 ; i<code_string.length(); i++){
			
			offset += Math.pow(2, i) * Integer.parseInt(""+code_string.charAt(code_string.length()-1-i));
		}
		
		if(start_with_1) offset *= -1;
		
		offset *= this.getD();
		
		double multiple = (side_information - offset) / delta;
		
		double fraction_part_of_integral_multiple = multiple - Math.floor(multiple);
		
		int integral_multiple;
		
		//check whether the fraction part is closer to 0 or 1,
		//i.e. distance between 1 smaller than distance between 0?
		if(Math.abs(1 - fraction_part_of_integral_multiple)<Math.abs(fraction_part_of_integral_multiple)){
			
			//closer to 1
			integral_multiple = (int) Math.ceil(multiple);
			
		}else{
			
			//closer to 0
			integral_multiple = (int) Math.floor(multiple);
			
		}
		
		return delta * integral_multiple + offset;
	}
	
	/**
	 * The raw readings are truncated by the d value, i.e. rounded off to the nearest d
	 * */
	public double quantize(double raw_reading){
		
		return this.getD()*Math.floor(raw_reading/this.getD());
	}

	public static void main(String[] args){
		//test
		
		CodeBook cb = new CodeBook();
		String data = "0100000000110100111001000101100001011101001011001100011001110111";
		System.out.println(data.length());
		System.out.println(cb.getDecodedValue(-20.8, "01"));
		
		int modded_index1 = cb.getIndex(-19.8) % ((int) Math.pow(2,16));
		int modded_index2 =  cb.getIndex(19.8) % ((int) Math.pow(2,16));
		System.out.println(modded_index1);
		System.out.println(modded_index2);
		System.out.println(Integer.toBinaryString(modded_index1).length());
		System.out.println(Integer.toBinaryString(modded_index2).length());
		System.out.println(Integer.toBinaryString(modded_index1));
		System.out.println(Integer.toBinaryString(modded_index2));
	}

	/**
	 * Gets the interval value.
	 * */
	public double getD() {
		return d;
	}

	public void setD(double d) {
		this.d = d;
	}


}
