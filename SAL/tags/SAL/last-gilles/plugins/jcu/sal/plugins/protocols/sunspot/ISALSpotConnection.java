package jcu.sal.plugins.protocols.sunspot;

import java.io.IOException;

/**
 * Objects implementing this interface are capable of handling
 * a connection to a SAL spot. They are be used to open the connection, 
 * send DO & STOP commands, received data, and terminate the connection
 * @author gilles
 *
 */
public interface ISALSpotConnection {
	
	/**
	 * This method tries to open the connection to a sal spot, and tells this 
	 * connection where to send incoming data for
	 * processing
	 * @param s the {@link SALSpot} which will handle incoming data
	 * @throws IOException if the connection to the sal spot can not be opened.
	 */
	public void open(SpotResponseHandler s)  throws IOException;
	
	/**
	 * This method returns the address of the sal spot
	 * @return the address of the sal spot
	 */
	public String getRemoteAddress();
	
	/**
	 * This method returns the port number used by this connection
	 * to the sal spot 
	 * @return the port number
	 */
	public int getPort();
	
	/**
	 * This method terminates the connection to a sal spot
	 */
	public void terminate();
	
	/**
	 * This command sends a DO command to a sensor. This command starts
	 * a stream of data from a sensor
	 * @param s the sensor
	 * @param c the command
	 * @param freq how often to run the command
	 * @throws IOException if there is an error sending the command
	 */
	void sendDoCommand(String s, String c, int freq) throws IOException;
	
	/**
	 * This command sends a STOP command to a sensor. It is used to stop
	 * an existing stream of data
	 * @param s the sensor
	 * @param c the command
	 * @throws IOException if there is an error sending the command
	 */
	void sendStopCommand(String s, String c) throws IOException;
	
	/**
	 * This command sends a PING to the {@link SALSpot}
	 * @throws IOException if there is an error sending the ping
	 */
	void sendPing() throws IOException;
}
