/**
 * 
 */
package jcu.sal.plugins.protocols.dummy;

import java.util.List;
import java.util.Vector;

import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.components.protocols.AbstractProtocol;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.components.sensors.Sensor;
import jcu.sal.plugins.endpoints.FSEndPoint;

/**
 * @author gilles
 *
 */
public class DummyProtocol extends AbstractProtocol {
	public static final String NB_DUMMY_SENSOR_ATTRIBUTE = "NbDummySensor";
	public static final String PROTOCOL_TYPE = "DUMMY";
	public static int NB_DUMMY_SENSORS=100;
	private final byte[] reading= new String("439041101").getBytes();

	/**
	 * @param i 
	 * @param t 
	 * @param c
	 * @param d
	 * @throws ConfigurationException 
	 */
	public DummyProtocol(ProtocolID i, ProtocolConfiguration c) throws ConfigurationException {
		super(i, PROTOCOL_TYPE, c);		

		//Add to the list of supported EndPoint IDs
		supportedEndPointTypes.add(FSEndPoint.ENDPOINT_TYPE);
		//Disable autodetection unless specified in config
		autoDetectionInterval = 0;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_getCMLStoreKey(jcu.sal.components.sensors.Sensor)
	 */
	@Override
	protected String internal_getCMLStoreKey(Sensor s){
		return CMLDescriptionStore.DUMMY_KEY;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_isSensorSupported(jcu.sal.components.sensors.Sensor)
	 */
	@Override
	protected boolean internal_isSensorSupported(Sensor s) {
		return true;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_parseConfig()
	 */
	@Override
	protected void internal_parseConfig() throws ConfigurationException {
		try { NB_DUMMY_SENSORS = Integer.parseInt(getParameter(NB_DUMMY_SENSOR_ATTRIBUTE));}
		catch (Exception e) {}
		cmls = CMLDescriptionStore.getStore();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_probeSensor(jcu.sal.components.sensors.Sensor)
	 */
	@Override
	protected boolean internal_probeSensor(Sensor s) {
		s.enable();
		return true;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_remove()
	 */
	@Override
	protected void internal_remove() {}

	/* (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_start()
	 */
	@Override
	protected void internal_start() throws ConfigurationException {}

	/* (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_stop()
	 */
	@Override
	protected void internal_stop() {}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#detectConnectedSensors()
	 */
	@Override
	protected List<String> detectConnectedSensors() {
		List<String> detected = new Vector<String>();
		for(int i=0;i<NB_DUMMY_SENSORS;i++)
			detected.add("DUMMY_"+i);		
		return detected;
	}
	
	/*
	 * Command handling methods
	 */
	public static String GET_READING_METHOD = "getReading";
	public  byte[] getReading(Command c, Sensor s){
		return reading;
	}

}
