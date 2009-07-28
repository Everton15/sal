/**
 * 
 */
package jcu.sal.plugins.endpoints;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import jcu.sal.common.Slog;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.pcml.EndPointConfiguration;
import jcu.sal.components.EndPoints.EndPoint;
import jcu.sal.components.EndPoints.EndPointID;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class EthernetEndPoint extends EndPoint {

	public static final String ETHDEVICEATTRIBUTE_TAG="EthernetDevice";
	public static final String IPADDRESSATTRIBUTE_TAG="IPAddress";
	public static final String ENDPOINT_TYPE="ethernet";
	
	private static Logger logger = Logger.getLogger(EthernetEndPoint.class);
	static {Slog.setupLogger(logger);}
	
	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public EthernetEndPoint(EndPointID i, EndPointConfiguration c) throws ConfigurationException {
		super(i, ENDPOINT_TYPE, c);
		parseConfig();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#updateConfig()
	 */
	@Override
	public void parseConfig() throws ConfigurationException {
		// Check if we have this Ethernet device on this platform
		NetworkInterface n = null;
		String intName = null, ipAddress = null;
		boolean found = false;
		
		//logger.debug("check if we have the ethernet port.");
		try { intName = getParameter(ETHDEVICEATTRIBUTE_TAG); } catch (NotFoundException e1) {}
		
		//IP address is not mandatory
		try {ipAddress = getParameter(IPADDRESSATTRIBUTE_TAG);} catch(NotFoundException e) {}
		
		if (intName==null && ipAddress==null){
			logger.error("Either the name or the ip address of an ethernet interface must be specified, none found");
			throw new ConfigurationException("Either the name or the ip address of an ethernet interface must be specified, none found");
		}
		
		try {
			/* Look for the ethernet if either by name or by IP address*/
			if (ipAddress == null) {
				/* by name */
				n = NetworkInterface.getByName(intName);
				found = true;
			} else {
				/* by IP address*/
				InetAddress i;
				Enumeration<NetworkInterface> e;
	
				e = NetworkInterface.getNetworkInterfaces();
				while(e.hasMoreElements() && !found) {
					n = e.nextElement();
					if(n.getDisplayName().equals(intName)) {
						Enumeration<InetAddress> ee = n.getInetAddresses();
						while(ee.hasMoreElements() && !found) {
							i = ee.nextElement();
							if(i.getHostAddress().equals(ipAddress)) {
								found = true;
								break;
							}
						}						
					}
				}	
			}
			
			if(found) {
				//logger.debug("Found ethernet port: " + n.getDisplayName());
				if (!n.isUp()) {
					logger.error("The ethernet interface '"+intName+"' is down");
					throw new ConfigurationException("The ethernet interface '"+intName+"' is down");
				}
				configured = true;
				//logger.debug("The ethernet port was successfully configured");
			} else {
				logger.error("The ethernet interface '"+intName+"' could not be found");
				throw new ConfigurationException("The ethernet interface '"+intName+"' could not be found");
			}
		} catch (SocketException e1) {
			logger.error("Error enumerating the network interfaces");
			throw new ConfigurationException("The ethernet interface '"+intName+"' could not be found", e1);
		}
	}
}
