package jcu.sal.common.cml;

import java.io.IOException;

import jcu.sal.common.Response;

/**
 * This interface is implemented by objects which will receive the result of a command.
 * @author gilles
 *
 */
public interface StreamCallback {
	/**
	 * This method is called by a SAL agent whenever the result of a command
	 * (the {@link Response}) must be sent to a client. 
	 * @param r the command response to be collected
	 * @throws IOException if the callback object wont accept calls anymore and the 
	 * stream must stop now. This exception is thrown either 
	 */
	public void collect(Response r) throws IOException;
}
