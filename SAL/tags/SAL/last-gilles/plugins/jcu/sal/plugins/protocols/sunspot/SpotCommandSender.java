package jcu.sal.plugins.protocols.sunspot;

import java.io.IOException;

/**
 * Object implementing this interface can send specific commands to a SAL spot
 * @author gilles
 *
 */
public interface SpotCommandSender {

	/**
	 * This command sends a DO command do a SAL sunspot. This command instructs
	 * the sal spot to start streaming the result of the command at the 
	 * specified sampling frequency.
	 * @param sensor the sensor to which the command is directed
	 * @param cmd the command 
	 * @param freq the sampling frequency
	 * @param h the {@link StreamDataHandler} which will receive the data stream
	 * @throws StreamException if there is an error setting up the stream
	 * @throws IOException if the command cannot be sent
	 */
	public void sendDoCommand(String sensor, String cmd, int freq,
			StreamDataHandler h) throws StreamException, IOException;
	
	/**
	 * this method sends a STOP command to a sal sunspot. The sal spot will 
	 * terminate the existing stream of data.
	 * @param sensor the streaming sensor 
	 * @param cmd the command used to produce the stream
	 * @throws IOException if the STOP command can not be sent
	 */
	public void sendStopCommand(String sensor, String cmd) throws IOException;
}
