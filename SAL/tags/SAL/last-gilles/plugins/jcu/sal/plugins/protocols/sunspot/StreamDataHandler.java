package jcu.sal.plugins.protocols.sunspot;

import java.io.IOException;

/**
 * Objects implementing this interface are passed a stream of 
 * {@link StreamData} objects, representing individual results of a command. 
 * @author gilles
 *
 */
public interface StreamDataHandler {
	/**
	 * This method is invoked whenever a new {@link StreamData} object is 
	 * available and must be handled.
	 * @param d the {@link StreamData} object
	 * @throws IOException if the stream must be closed
	 */
	public void handle(StreamData d) throws IOException;

}
