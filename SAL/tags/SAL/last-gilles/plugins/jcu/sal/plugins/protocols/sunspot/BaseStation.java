package jcu.sal.plugins.protocols.sunspot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;

import jcu.sal.common.Slog;

import org.apache.log4j.Logger;


public class BaseStation {
	private static Logger logger = Logger.getLogger(BaseStation.class);
	static {Slog.setupLogger(logger);}	
	
	private static List<SALSpot> spots = new Vector<SALSpot>();
	private static SALSpotFinder finder = new SALSpotFinder();
	
	/**
	 * This method starts the SAL base station. It starts the SAL spot finder
	 * and the stream data dispatcher thread. 
	 */
	public synchronized static void start(){
		StreamDataDispatcher.start();
		try {
			finder.start();
		} catch (IOException e) {
			logger.error("Error starting the SAL spot discovery thread");
			e.printStackTrace();
		}
	}
	
	/**
	 * This method stops the SAL base station. It stops the SAL spot finder
	 * and the stream data dispatcher thread.
	 */
	public synchronized static void stop(){
		//must be before removeAllSpots() obviously
		finder.stop();
		//must be before StreamDataDispatcher.stop() as it will
		//send StreamClosedException to open streams which must be dispatched
		removeAllSpots();
		StreamDataDispatcher.stop();
	}

	
	/**
	 * This method is called by the {@link SALSpotFinder} thread when a new 
	 * SAL spot is discovered
	 * @param s the new SAL spot
	 */
	static void addSpot(SALSpot s){
		synchronized(spots){
			SALSpot prev = getSpot(s.getAddress());
			if(prev!=null)
				removeSpot(prev);
			
			logger.debug("Adding new spot: "
					+s.getAddress()+" "+s.getPort());
			spots.add(s);
		}
	}
	
	/**
	 * This method is called by a {@link SALSpot} when a spot is disconnected.
	 * It is also called by {@link #removeAllSpots()}. 
	 * @param s the {@link SALSpot} to be removed
	 */
	static void removeSpot(SALSpot s){
		synchronized(spots){
			if(spots.remove(s)){
				logger.debug("Removing spot: "
						+s.getAddress()+" "+s.getPort());
				s.close();
			}
		}
	}
	
	/**
	 * This method removes all spots
	 */
	private static void removeAllSpots(){
		synchronized(spots){
			Vector<SALSpot> v = new Vector<SALSpot>(spots);
			for(SALSpot s: v)
				removeSpot(s);
			
		}
	}
	
	/**
	 * This method returns a {@link SALSpot} object given its address
	 * @param address the address of the spot
	 * @return a {@link SALSpot} object given its address, or <code>null</code>
	 * if not SAL spot has the given address
	 */
	public static SALSpot getSpot(String address){
		SALSpot s = null;
		synchronized(spots){
			for(SALSpot spot: spots)
				if(spot.getAddress().equals(address)){
					s = spot;
					break;
				}
		}
		return s;
	}
	
	/**
	 * This method returns a list of all {@link SALSpot}s currently connected.
	 * @return a list of all {@link SALSpot}s currently connected
	 */
	public static List<SALSpot> getSpots(){
		Vector<SALSpot> v;
		synchronized(spots){
			v = new Vector<SALSpot>(spots);
		}
		return v;
	}
	

	public static void main(String[] args) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String s;
		SALSpot spot;
		String[] tokens;
		StreamDataHandler h = new StreamDataHandler(){

			@Override
			public void handle(StreamData d) throws IOException {
				try{
					System.out.println("Stream handler: handled data: "+d.getData());
				}catch (StreamClosedException e){
					System.out.println("Stream handler: Stream closed");
				} catch(StreamException e){
					System.out.println("Stream handler: received exception: "+e.getMessage());
				}
			}
			
		};
		
		System.out.println("Starting");
		System.setProperty("SERIAL_PORT", args[0]);
		start();
		
		System.out.println("Enter a (SPOT addr CMD_TYPE SENSOR_TYPE CMD INTERVAL) or -1 to exit");
		s = br.readLine();
		while(!s.equals("-1")){
			tokens = SALSpotConnectionImpl.split(s, " ");
			spot = BaseStation.getSpot(tokens[0]);
			if(spot!=null){
				if(tokens[1].equals("1")){
					//send DO cmd
					try {
						spot.sendDoCommand(tokens[2], tokens[3], Integer.parseInt(tokens[4]), h);
					} catch (NumberFormatException e) {
						System.out.println("Wrong smapling frequency value");
					} catch (StreamException e) {
						System.out.println("Error setting up the stream");
					}
				} else if(tokens[1].equals("2")){
					//send STOP cmd
					spot.sendStopCommand(tokens[2], tokens[3]);
				} else
					System.out.println("Wrong command type (must be 1 or 2");
			} else
				System.out.println("Wrong spot address");
			
			System.out.println("Enter a (SPOT addr CMD_TYPE(1: do, 2:STOP) SENSOR_TYPE CMD INTERVAL) or -1 to exit");
			s = br.readLine();			
		}
		
		stop();
		removeAllSpots();
		System.out.println("Main exiting");
        System.exit(0);
	}
}
