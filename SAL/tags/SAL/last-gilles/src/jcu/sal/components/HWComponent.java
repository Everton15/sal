/**
 * 
 */
package jcu.sal.components;

import jcu.sal.common.exceptions.ConfigurationException;

import jcu.sal.common.Parameters;
import jcu.sal.common.exceptions.NotFoundException;



/**
 * This interface specifies the basic methods required by Components 
 * (LogicalPorts, EndPoints, Sensors and Procotol)
 * @author gilles
 *
 */
public interface HWComponent {
	
	/**
	 * This method returns the type of this component
	 */
	public String getType();

	/**
	 * Changes this component's configuration and applies these changes 
	 * @param config the component's configuration
	 * 
	 */
	public Identifier getID();
	
		
	/**
	 * Retrieves this component configuration parameters
	 * @param config the component's configuration parameters
	 *
	 */
	public Parameters getParameters();
	
	/**
	 * Retrieves a single configuration directive from this component
	 * @param key the directive's name
	 * @throws NotFoundException if no directive matches the given name
	 * 
	 */
	public String getParameter(String directive) throws NotFoundException;
	
	/**
	 * Starts this component: puts the component in a state where it can be used
	 * without requiring further configuration
	 */
	public void start() throws ConfigurationException;

	/**
	 * Stops this component
	 *
	 */
	public void stop() ;
	
	/**
	 * Destroy this component 
	 *
	 */
	public void remove(componentRemovalListener c);

}
