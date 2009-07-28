/**
 * 
 */
package jcu.sal.components;

import java.util.concurrent.atomic.AtomicBoolean;

import jcu.sal.common.exceptions.ConfigurationException;

import jcu.sal.common.Parameters;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.pcml.HWComponentConfiguration;

/**
 * This class does something
 * @author gilles
 *
 */
public abstract class AbstractComponent<T extends Identifier, U extends HWComponentConfiguration> implements HWComponent {
	
	protected U config;
	protected AtomicBoolean removed;
	protected T id = null;
	
	/**
	 * This constructor creates an abstract component with the supplied configuration and ID objects
	 * @param c the supplied configuration object
	 * @param i the ID of the component
	 */
	public AbstractComponent(U c, T i) {config = c; id = i;}
	
	/**
	 * This method returns the configuration object associated with this component
	 * @return the configuration object associated with this component
	 */
	public U getConfig() { return config; }
	
	/* (non-Javadoc)
	 * @see jcu.sal.components.HWComponent#getConfig()
	 */
	@Override
	public Parameters getParameters() { return config.getParameters(); }

	/* (non-Javadoc)
	 * @see jcu.sal.components.HWComponent#getConfig(java.lang.String)
	 */
	@Override
	public String getParameter(String directive) throws NotFoundException {
		return config.getParameters().getParameter(directive).getStringValue();
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.components.HWComponent#remove()
	 */
	@Override
	public abstract void remove(componentRemovalListener c);

	/* (non-Javadoc)
	 * @see jcu.sal.components.HWComponent#start()
	 */
	@Override
	public abstract void start() throws ConfigurationException;

	/* (non-Javadoc)
	 * @see jcu.sal.components.HWComponent#stop()
	 */
	@Override
	public abstract void stop();
	
	/* (non-Javadoc)
	 * @see jcu.sal.components.HWComponent#getType()
	 */
	@Override
	public String getType() {
		return config.getType();
	}
	
	/**
	 * Parses the component's configuration directives stored in config,
	 * and applies them
	 * @throws ConfigurationException Indicates a error parsing/setting configuration. 
	 * Most likely there is a problem with the component's configuration
	 */
	public abstract void parseConfig() throws ConfigurationException;
	
	/**
	 * returns a textual representation of a component
	 * @return the textual representation of the component
	 */
	public abstract String toString();

	/**
	 * Gets the Identifier of a component
	 * @return the Identifier
	 */
			
	public T getID() {
		return this.id;
	}
}
