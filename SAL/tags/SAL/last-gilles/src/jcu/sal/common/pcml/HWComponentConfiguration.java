package jcu.sal.common.pcml;

import jcu.sal.common.Parameters;

public interface HWComponentConfiguration {
	
	/**
	 * This method returns the ID associated with this component
	 * @return the ID associated with this sensor
	 */
	public String getID();
	
	/**
	 * This method returns the parameters associated with this component
	 * @return the parameters associated with this sensor
	 */
	public String getType();
	
	/**
	 * This method returns the parameters associated with this component
	 * @return the parameters associated with this component
	 */
	public Parameters getParameters();
	


}
