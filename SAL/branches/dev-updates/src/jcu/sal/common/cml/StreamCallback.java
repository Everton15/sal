package jcu.sal.common.cml;

import java.io.IOException;

import jcu.sal.common.Response;

/**
 * This interface is implemented by by objects within the SAL agent, and SAL client stubs.
 * It must not be used by any SAL client (other than stubs). Instead, these should use the
 * {@link ClientStreamCallback} interface instead. 
 * Object implementing this interface can be used as callback objects to collect
 * streaming data from a sensor which supports it. 
 * @author gilles
 *
 */
public interface StreamCallback {
	/**
	 * This method is called by a Protocol when streaming data from a sensor is available. 
	 * @param r the streaming data to be collected
	 * @throws IOException if the callback object wont accept calls anymore and the stream must stop now. This exception is thrown either 
	 * <ul>
	 * <li>by the RMI SAL agent to tell the agent to stop streaming data, as the callback object 
	 * is not available anymore to collect data.</li>
	 * <li>by the RMI SAL client stub to tell the agent to stop the stream.
	 */
	public void collect(Response r) throws IOException;
}
