package stimulator;

import java.util.*;

public class Simulator {
	
	private int number_of_sensors;
	private String[] channel;
	private int[] amount_of_bits_sent;
	private Sensor[] sensors;
	private Server server;
	private double simulation_time;
	private int[] number_of_drops_sensor;
	private int number_of_drops_server;
	private double[] sum_of_error;
	double[][] data_to_be_sent;
	private int number_of_rounds;
	private double total_error;
	private double total_bits;

	public Simulator(int number_of_sensors, int number_of_rounds, Server server, Sensor[] sensors, double[][] data, boolean report_results, boolean print_each_step){
		
		initialization(number_of_sensors, number_of_rounds,server, sensors,data);
		
		stimulate(number_of_rounds, print_each_step);
		
		reportStimulationResults(report_results);
	}
	
	/**
	 * <br>Generate the data to be sent by the sensor</br>
	 * <br>Happens when data is not provided</br>
	 * <br>Should use exponential distribution as random variable as noise</br>
	 * */
	private void generateData(){
		
		Random random = new Random();
		
		for(int r =0; r<this.number_of_rounds; r++){
			
			for(int id=0; id<this.number_of_sensors; id++){
				
				data_to_be_sent[id][r] = 20 +Math.abs(random.nextDouble());
			}
		}
	}
	
	private void initialization(int number_of_sensors, int number_of_rounds,Server server, Sensor[] sensors,double[][] data){
		
		if(data !=null && data.length ==number_of_sensors && data[0].length == number_of_rounds){
			
			data_to_be_sent = data;
			
		}else{
			
			this.generateData();
		}
		
		this.number_of_rounds = number_of_rounds;
		
		amount_of_bits_sent = new int[number_of_sensors];
		
		number_of_drops_sensor = new int[number_of_sensors];
		
		sum_of_error = new double[number_of_sensors];
		
		this.number_of_sensors = number_of_sensors;
		channel = new String[this.number_of_sensors];
		
		this.server = server;
		server.initialize(number_of_sensors); 
		simulation_time += server.timeToInitialize();
		
		this.sensors = sensors;
		
		for(int i=0; i<number_of_sensors; i++){
			
			//System.out.println("Initializing sensor: "+i);
			
			simulation_time += sensors[i].timeToInitialize();
		}
	}
	
	private void stimulate(int number_of_rounds,boolean print_each_step){
		
		int last = 0;
		
		for(int r = 0; r<number_of_rounds; r++){
			//System.out.println("ROUND : "+r+"/"+number_of_rounds+"*****************************************************");
			if(print_each_step){
				if(r ==0 )System.out.println("Stimulation STARTED");
				if( (((double)r)/number_of_rounds) > last*0.05 ){System.out.println("Completed: "+((100*(double)r)/number_of_rounds)+"%");last++;} 
			}
			

			
			for(int id =0; id<number_of_sensors; id++){
				
				sensors[id]	.takeReadingToSend(data_to_be_sent[id][r]);
				
				serverSendRequest(id);//server sending
				
				channelCorrupt();
				
				sensorFetchAndSendData(id);//sensor checking and sending
				
				channelCorrupt();
				
				//keeps sending until sensor receives something and send data back and server can read
				while(channel[id]==null || !serverReadsData(channel[id], id)){
					
					System.out.println("Server drops data from sensor "+id+" channel id is null: "+(channel[id]==null));
					
					System.exit(0);
						
					serverSendRequest(id);
					
					channelCorrupt();
						
					sensorFetchAndSendData(id);
					
					channelCorrupt();
				}
			}
			
			//Calculate the errors made by server's decoding
			double[] readings_by_server = server.getReadingReceivedByServer();
			for(int i=0; i<readings_by_server.length; i++){
				
				sum_of_error[i] += Math.abs(readings_by_server[i] - sensors[i].getReadingSentBySensor());
				
				if(print_each_step)System.out.println(r+". Sensor "+i+" sent: "+sensors[i].getReadingSentBySensor()+" and server receives: "+readings_by_server[i]+". Error: "+Math.abs(readings_by_server[i] - sensors[i].getReadingSentBySensor())+"  \t "+(Math.abs(readings_by_server[i] - sensors[i].getReadingSentBySensor())>0.001)+" :: Number of bits used: "+channel[i].length());
			}
		}
			

			
	}
	
	private boolean serverReadsData(String data, int id){
		
		//System.out.println("Server receives: "+data+" from sensor "+id);
		
		if (!server.receiveData(data, id)){
			
			number_of_drops_server++;
			
			return false;
		}
			
		return true;
	}
	
	public void channelCorrupt(){}
	
	public double channelSendingTime(){return 0.01;}//fixed time for now
	
	private void serverSendRequest(int id){
		
		channel[id] = server.sendRequest(id);
		
		amount_of_bits_sent[id] +=channel[id].length();
		
		//System.out.println("Server sent : "+channel[id]+ " to "+id);
		
		simulation_time+=channelSendingTime();
	}
	
	/**
	 * Tries to read the data from the channel and replies if it understood.
	 * Otherwise message from server is ignored
	 * */
	private void sensorFetchAndSendData(int id){
		
		//System.out.println("Sensor "+id +" receives: "+channel[id]);
		
		if(sensors[id].fetchRequest(channel[id])){
			
			channel[id] = sensors[id].sendData();
			
			amount_of_bits_sent[id] += channel[id].length();
			
			simulation_time+=channelSendingTime();
		}
			
		else
			
			channel[id] = null;
		
			number_of_drops_sensor[id]++;
	}
	
	private void reportStimulationResults(boolean report){
		
		for(int i=0; i<this.number_of_sensors; i++){
			
			this.total_bits +=amount_of_bits_sent[i];
		}
		
		for(int i=0; i<this.sum_of_error.length; i++){
			
			this.total_error += sum_of_error[i];
		}
		
		if(report){
			
			System.out.println("************************************************************");
			System.out.println("STIMULATION RESULTS");
			for(int i=0; i<this.number_of_sensors; i++){
				
				System.out.println("Channel "+i+" has carried "+amount_of_bits_sent[i]+" bits");
				
			}
			System.out.println("TOTAL NUMBER OF BITS SENT: "+ total_bits);
			System.out.println("");
			System.out.println("");
			
			for(int i=0; i<this.sum_of_error.length; i++){
				
				System.out.println("Decoding bits from channel "+i+" has made a total of "+sum_of_error[i]);
				
			}
			System.out.println("TOTAL ERROR : "+total_error);
			System.out.println("************************************************************");
		}
	}
	
	public double getTotalBits(){
		
		return this.total_bits;
	}
	public double getTotalError(){
		
		return this.total_error;
	}
}
