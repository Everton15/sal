package jcu.sal.plugins.protocols.sunspot;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import jcu.sal.common.Slog;
import jcu.sal.plugins.protocols.sunspot.StreamDataDispatcher.QueueElement;

import org.apache.log4j.Logger;


public class SALSpot implements SpotResponseHandler, SpotCommandSender{
	private static Logger logger = Logger.getLogger(SALSpot.class);
	static {Slog.setupLogger(logger);}
	
	private static final String KEY_DELIM=":";
	private String address;
	private ISALSpotConnection conn;
	private Hashtable<String, StreamDataHandler> streams;
	private List<String> singleCmd;

	
	/**
	 * Creates a new SALSpot with the given connection and address
	 * @param c the connection
	 * @param a our address
	 * @throws IOException if the connection to the sal spot can not be opened
	 */
	public SALSpot(ISALSpotConnection c, String ourAddress) throws IOException{
		conn = c;
		address = ourAddress;
		streams = new Hashtable<String, StreamDataHandler>();
		singleCmd = new Vector<String>();
		c.open(this);
	}

	/**
	 * This method returns the address of this sal spot.
	 * @return the address of this sal spot
	 */
	public String getAddress(){
		return address;
	}
	
	/**
	 * This method returns the port used by the connection to this sal spot.
	 * @return the port used by the connection to this sal spot.
	 */
	public int getPort(){
		return conn.getPort();
	}
	
	/**
	 * this method sends a DO command to the sal spot. This command starts a new
	 * stream of data
	 * @param sensor the sensor
	 * @param cmd the command
	 * @param freq the sampling frequency
	 * @param h the {@link StreamDataHandler} object that will receive the 
	 * stream data.
	 * @throws StreamException if a stream for this sensor already 
	 * exists
	 * @throws IOException if there is an error sending the command 
	 */
	public void sendDoCommand(String sensor, String cmd, int freq, StreamDataHandler h)
		throws StreamException, IOException{
		String key = toKey(sensor, cmd);
		synchronized(streams){
			if(!streams.containsKey(key)){
				try {
					conn.sendDoCommand(sensor, cmd, freq);
					streams.put(key, h);
					if(freq==SensorConstants.SINGLE_COMMAND){
						logger.debug("Adding single shot stream "+key);
						singleCmd.add(key);
					} else
						logger.debug("Adding stream "+key);
					return;
				} catch (IOException e) {
					logger.error("Connection error when sending DO cmd");
					throw e;
				}
			}
		}
		throw new StreamException("this sensor is already streaming");
	}
	
	/**
	 * this method sends a STOP command to the sal spot. This command stops an 
	 * existing stream of data. If the stream does not exist, this method does 
	 * nothing.
	 * @param sensor the sensor
	 * @param cmd the command
	 * @throws IOException if there is an error sending the command
	 */
	public void sendStopCommand(String sensor, String cmd)
		throws IOException{
		String key = toKey(sensor, cmd);
		synchronized(streams){
			if(streams.containsKey(key) && !singleCmd.contains(key)){
				try {
					conn.sendStopCommand(sensor, cmd);
					dispatchStreamClosedException(
							"Stream closed", 
							streams.get(key)
					);
					streams.remove(key);
					logger.debug("Removed stream "+key);
				} catch (IOException e) {
					logger.error("Connection error when sending STOP cmd");
				}
			}
		}
	}
	
	/**
	 * This method returns a list of known sensors on this sal spot.
	 * @return a list of known sensors on this sal spot.
	 */
	public List<String> listSensors(){
		return SensorConstants.getSensorTypeList();
	}
	
	/**
	 * This method forces the connection with the SAL spot to close
	 */
	public void close(){
		conn.terminate();
	}
	
	/**
	 * This method is called by the {@link ISALSpotConnection} object to notify
	 * us that the low-level connection has been closed
	 */
	@Override
	public void connectionClosed(){
		BaseStation.removeSpot(this);
		terminateStreams();
	}
	
	/**
	 * This method sends a stream closed exception to all existing streams
	 */
	private void terminateStreams(){
		synchronized(streams){
			Vector<String> keys = new Vector<String>(streams.keySet());
			for(String key : keys){
				dispatchStreamClosedException(
						"Stream closed", 
						streams.get(key)
				);
				streams.remove(key);
				singleCmd.remove(key);
				logger.debug("Removed stream "+key);
			}
		}
	}
	
	/**
	 * This method returns the map key in the <code>streams</code> map
	 * from a sensor type and command
	 * @param s the sensor
	 * @param c the command
	 * @return the key in the <code>streams</code> map
	 */
	private String toKey(String s, String c){
		return s+KEY_DELIM+c;
	}
	
	@Override
	public void processReplyMessage(String[] tokens){
		StreamDataHandler handler;
		String key = toKey(tokens[1], tokens[2]);
		synchronized(streams){
			handler= streams.get(key); 
			if(handler!=null){
				dispatchStreamData(
						tokens[3],
						handler,
						tokens[1],
						tokens[2]
				);
				if(singleCmd.contains(key)){
					singleCmd.remove(key);
					streams.remove(key);
				}
			} else
				logger.error("REPLY_DATA message does not belong"+
						" to any stream");
		}
	}

	@Override
	public void processReplyErrorMessage(String[] tokens){
		StreamDataHandler handler;
		String key = toKey(tokens[1], tokens[2]);
		String e = "";
		
		//reconstruct the exception's message
		for(int i = 3; i<tokens.length;i++)
			e = e + " "+ tokens[i];
		
		synchronized(streams){
			handler= streams.get(key); 
			if(handler!=null){
				dispatchStreamException(
						e,
						handler,
						tokens[1],
						tokens[2]
				);
				streams.remove(key);
				singleCmd.remove(key);
				logger.debug("Removed stream "+key);
			} else
				logger.error("REPLY_ERROR message does not belong"+
				" to any stream");
		}
	}
	
	/**
	 * This method creates a new {@link QueueElement}, which will carry a
	 * data element <code>d</code>. The {@link QueueElement} is then inserted  
	 * in the {@link StreamDataDispatcher}'s queue for dispatching.
	 * @param d the data to be queued
	 * @param h the {@link StreamDataHandler} which will handle the 
	 * data element.
	 * @param s the sensor from where this stream data originates
	 * @param c the command used to produce the {@link StreamData} 
	 * 
	 */
	private void dispatchStreamData(String d, StreamDataHandler h, 
			String sensor, String cmd){
		StreamDataDispatcher.queueData(
				new QueueElement(new StreamData(d), h,this, sensor, cmd)
		);
	}
	
	/**
	 * This method creates a new {@link QueueElement}, which will carry a
	 * {@link StreamException} with message <code>e</code>. The 
	 * {@link QueueElement} is then inserted  
	 * in the {@link StreamDataDispatcher}'s queue for dispatching.
	 * @param e the description of the {@link StreamException}
	 * @param h the {@link StreamDataHandler} which will handle the 
	 * {@link StreamException}
	 * @param s the sensor from where this stream data originates
	 * @param c the command used to produce the {@link StreamData} 
	 * 
	 */
	private void dispatchStreamException(String e, StreamDataHandler h, 
			String sensor, String cmd){
		StreamDataDispatcher.queueData(
				new QueueElement(
						new StreamData(new StreamException(e)), 
						h,
						this,
						sensor,
						cmd)
		);
	}
	
	/**
	 * This method creates a new {@link QueueElement}, which will carry a
	 * {@link StreamClosedException} with message <code>e</code>. The 
	 * {@link QueueElement} is then inserted  
	 * in the {@link StreamDataDispatcher}'s queue for dispatching.
	 * @param e the description of the {@link StreamClosedException}
	 * @param h the {@link StreamDataHandler} which will handle the 
	 * {@link StreamClosedException}.
	 */
	private void dispatchStreamClosedException(String e, StreamDataHandler h){
		StreamDataDispatcher.queueData(
				new QueueElement(
						new StreamData(new StreamClosedException(e)),
						h
						)
		);
	}
}
