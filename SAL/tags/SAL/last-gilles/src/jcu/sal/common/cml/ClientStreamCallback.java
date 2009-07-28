package jcu.sal.common.cml;

import java.io.IOException;

import jcu.sal.common.Response;
import jcu.sal.common.agents.SALAgent;


/**
 * Object implementing this interface can be used as callback objects to collect streaming data from a sensor which supports it. 
 * @author gilles
 *
 */
public interface ClientStreamCallback {
	/**
	 * This method is called by a Protocol when streaming data from a sensor is available. 
	 * @param r the streaming data to be collected
	 * @throws IOException if the callback object wont accept calls anymore and the stream must stop now. This exception is thrown either 
	 * <ul>
	 * <li>by the RMI SAL agent to tell the agent to stop streaming data, as the callback object 
	 * is not available anymore to collect data.</li>
	 * <li>by the RMI SAL client stub to tell the agent to stop the stream.
	 */
	public void collect(Response r, SALAgent a) throws IOException;
}
