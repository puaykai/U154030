package stimulator;

public interface Server {
	
	public void initialize(int number_of_sensors);

	//public String decodeData(String[] data);
	
	public String sendRequest(int sensor_id);
	
	public double timeToInitialize();
	
	public boolean receiveData(String data, int id);
	
	public double timeToLink();
	
	/**
	 * <br>Gets the data received by the server</br>
	 * <br>This is to check for the stimulator to check for error in transmission</br>
	 * **/
	public double[] getReadingReceivedByServer();
}
