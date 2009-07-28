package jcu.sal.client.gui.view;

import java.util.Hashtable;

import jcu.sal.client.gui.ClientController;
import jcu.sal.common.StreamID;
import jcu.sal.common.agents.SALAgent;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.CMLDescription;

public interface ClientView {
	
	/**
	 * This method returns the {@link ClientController} used by this view
	 * @return the {@link ClientController} used by this view
	 */
	public ClientController getController();
	
	/**
	 * This method sends a command, given the arguments' values, its {@link CMLDescription}
	 * and the sensor ID
	 * @param values the values for each argument of the command, except {@link CMLConstants#ARG_TYPE_CALLBACK} 
	 * callback arguments.
	 * @param cml the {@link CMLDescription} of the command to be sent
	 * @param c the context of the selected sensor
	 */
	public void sendCommand(Hashtable<String,String> values, CMLDescription cml, Context c);
	
	/**
	 * This method is invoked by {@link ResponseHandler}s when a stream must be closed
	 * @param a the sal agent
	 * @param s te stream id to close
	 */
	public void terminateStream(SALAgent a, StreamID s);
	
	/**
	 * This method is called by {@link SensorTree} when a component (sensor, protocol, agent)
	 * to notify the view that it should update other parts.
	 * @param label the {@link Context} of the selected object
	 */
	public void componentSelected(Context label);
	
	/**
	 * This method adds an entry to the log area
	 * @param l the log entry
	 */
	public void addLog(String l);
}
