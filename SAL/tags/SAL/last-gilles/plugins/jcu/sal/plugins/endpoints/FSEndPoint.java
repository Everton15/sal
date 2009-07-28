/**
 * 
 */
package jcu.sal.plugins.endpoints;

import jcu.sal.common.Slog;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.pcml.EndPointConfiguration;
import jcu.sal.components.EndPoints.EndPoint;
import jcu.sal.components.EndPoints.EndPointID;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class FSEndPoint extends EndPoint {

	private static Logger logger = Logger.getLogger(FSEndPoint.class);
	static {Slog.setupLogger(logger);}
	public static final String ENDPOINT_TYPE = "fs";
	
	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public FSEndPoint(EndPointID i, EndPointConfiguration c) throws ConfigurationException {
		super(i,ENDPOINT_TYPE, c);
		parseConfig();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#parseConfig()
	 */
	@Override
	public void parseConfig() throws ConfigurationException {
		// Not much to do here 
		//logger.debug("Found filesystem");
		configured = true;
	}
}
