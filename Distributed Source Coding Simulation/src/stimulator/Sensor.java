package stimulator;

public interface Sensor {

	/**
	 * @return true if successful receives the request
	 * @return false if unsuccessful
	 * */
	public boolean fetchRequest(String request);
	
	public String sendData();
	
	public double timeToInitialize();
	
	public double timeToSend();
	
	public double getReadingSentBySensor();
	
	public void takeReadingToSend(double reading);
}
