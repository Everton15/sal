/**
 * 
 */
package jcu.sal.plugins.endpoints;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

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
public class SerialEndPoint extends EndPoint {

	public static final String NOSETUP_TAG = "NoSetup";
	public static final String PORTDEVICEATTRIBUTE_TAG = "PortDeviceFile";
	public static final String PORTSPEEDATTRIBUTE_TAG = "PortSpeed";
	public static final String DATABITSATTRIBUTE_TAG = "DataBits";
	public static final String PARITYATTRIBUTE_TAG = "Parity";
	public static final String STOPBITATTRIBUTE_TAG = "StopBit";
	
	public static final String ENDPOINT_TYPE = "serial";
	
	private static Logger logger = Logger.getLogger(SerialEndPoint.class);
	static {Slog.setupLogger(logger);}
	
	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public SerialEndPoint(EndPointID i,  EndPointConfiguration c) throws ConfigurationException {
		super(i,ENDPOINT_TYPE,c);
		parseConfig();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#parseConfig()
	 */
	@Override
	public void parseConfig() throws ConfigurationException {
		// Check if we have this serial port on this platform
		CommPortIdentifier id;
		String device = null;
		try { device = getParameter(PORTDEVICEATTRIBUTE_TAG); } catch (NotFoundException e1) {
			logger.error("Cant find the serial port device file in the endpoint config");
			throw new ConfigurationException("Cant find the serial port device file in the EndPoint configuration", e1);
		}
		
		try { 
			getParameter(NOSETUP_TAG);
			//if we re here, the tag is present, so skip the rest of this method
			return;
		} catch (Exception e1) {
			//parameter is optional so keep going if not found
		}
		
		int speed;
		try { speed = Integer.valueOf(getParameter(PORTSPEEDATTRIBUTE_TAG)); } catch (Exception e1) {
			logger.error("Cant find the serial port speed in the endpoint config");
			throw new ConfigurationException("Cant find the serial port speed in the EndPoint configuration", e1);
		}
		
		int dataBits;
		try { dataBits = Integer.valueOf(getParameter(DATABITSATTRIBUTE_TAG));	} catch (Exception e1) {
			logger.error("Cant find the serial port data bits in the endpoint config");
			throw new ConfigurationException("Cant find the serial port data bits in the EndPoint configuration", e1);
		}
		
		int stopBit;
		try { stopBit = Integer.valueOf(getParameter(STOPBITATTRIBUTE_TAG));} catch (Exception e1) {
			logger.error("Cant find the serial port stop bit in the endpoint config");
			throw new ConfigurationException("Cant find the serial port stop bit in the EndPoint configuration", e1);
		} 
		
		int parity;
		try { parity = Integer.valueOf(getParameter(PARITYATTRIBUTE_TAG));	} catch (Exception e1) {
			logger.error("Cant find the serial port parity in the endpoint config");
			throw new ConfigurationException("Cant find the serial port parity in the EndPoint configuration", e1);		
		}
		//logger.debug("check if we can setup the serial port");
		try {
			id = CommPortIdentifier.getPortIdentifier(device);
			if(id.getPortType()!=CommPortIdentifier.PORT_SERIAL) {
				logger.error("The supplied device file is NOT a serial port");
				throw new ConfigurationException("Could not setup the serial port");
			}

			//logger.debug("The serial port name is " + id.getName());
			SerialPort p = (SerialPort) id.open("SALv1", 20);
			p.setSerialPortParams(speed, dataBits, stopBit,	parity);
			p.close();
			configured = true;
			//logger.debug("The serial port was configured successfully");
		} catch (PortInUseException e) {
			logger.error("The serial port cannot be opened and is currently in use ...");
			e.printStackTrace();
			throw new ConfigurationException("Could not setup the serial port");
		} catch (NoSuchPortException e) {
			e.printStackTrace();
			throw new ConfigurationException("Could not setup the serial port");
		} catch (UnsupportedCommOperationException e) {
			logger.error("The serial port cannot be setup");
			e.printStackTrace();
			throw new ConfigurationException("Could not setup the serial port");
		}
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		/* Lists javax.comm recognised-serial ports */
		CommPortIdentifier portId;
		Enumeration e = CommPortIdentifier.getPortIdentifiers();
		System.out.println("List of serial ports reported by javax.comm:");
		while(e.hasMoreElements()) {
			portId= (CommPortIdentifier) e.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				System.out.println("Port: " +portId.getName() + " - Currently used? " + portId.isCurrentlyOwned());
			} 
		}
	}
}
