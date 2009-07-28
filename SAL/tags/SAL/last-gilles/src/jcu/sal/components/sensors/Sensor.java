/**
 * @author gilles
 */
package jcu.sal.components.sensors;

import jcu.sal.common.Slog;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.common.sml.SMLConstants;
import jcu.sal.common.sml.SMLDescription;
import jcu.sal.components.AbstractComponent;
import jcu.sal.components.componentRemovalListener;
import jcu.sal.components.protocols.ProtocolID;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class Sensor extends AbstractComponent<SensorID, SMLDescription> {
	
	private static Logger logger = Logger.getLogger(Sensor.class);
	static {Slog.setupLogger(logger);}
	private SensorState state;
	
	/**
	 * Sensor constructor
	 * @param i the sensor ID
	 * @param c the configuration table
	 */
	public Sensor(SensorID i, SMLDescription s) {
		super(s,i);
		state = new SensorState(i);
	}

	public void setPid(ProtocolID pid) throws ConfigurationException {
		String p;
		try {
			p = config.getParameter(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE);
		} catch (NotFoundException e) {
			logger.error("Shouldnt be here, we have a sensor ("+id.toString()+") without a protocol name");
			e.printStackTrace();
			throw new SALRunTimeException("We have a sensor ("+id.toString()+") without a protocol name",e);
		}
		
		if(!pid.getName().equals(p)) {
			logger.error("Trying to associate with protocol '"+pid.getName()+"' but sensor config expected '"+p+"'");
			throw new ConfigurationException("cant associate with this protocol ("+pid.getName()+") - sensor config says "+p+" instead");
		}		
		id.setPid(pid);
	}
	
	public String getNativeAddress() {
		try {
			return config.getParameter(SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE);
		} catch (NotFoundException e) {
			logger.error("Shouldnt be here, we have a sensor ("+id.toString()+") without a native address");
			e.printStackTrace();
			throw new SALRunTimeException("We have a sensor ("+id.toString()+") without a native address",e);
		}
	}
		
	public boolean startStream() {
		return state.startStream();
	}
	
	public boolean stopStream() {
		return state.stopStream();
	}
	
	public boolean disable() {
/*		logger.debug("Disabling sensor " + toString());*/
		return state.disable();
	}
	
	public boolean disconnect(){
/*		logger.debug("Disconnecting sensor " + toString());*/
		return state.disconnect();
	}
	
	public boolean reconnect() {
/*		logger.debug("Reconnecting sensor " + toString());*/
		return state.reconnect();
	}
	
	public boolean enable() {
/*		logger.debug("Enabling sensor " + toString());*/
		return state.enable();
	}
	
	public boolean isAvailableForCommand() {
		return state.isAvailableForCommand();
	}
	
	public boolean isDisconnected() {
		return state.isDisconnectedDisabled();
	}
	
	public boolean isStreaming() {
		return state.isStreaming();
	}
	
	public String getStateToString() {
		return state.toString();
	}

	/*
	 * End of state management methods
	 */
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#parseConfig()
	 */
	@Override
	public void parseConfig() throws ConfigurationException {}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#remove(jcu.sal.components.componentRemovalListener)
	 */
	@Override
	public void remove(componentRemovalListener c) {
		//logger.debug("Registering removal of sensor " + toString());
		state.remove(c);
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#start()
	 */
	@Override
	public void start() {
		state.enable();
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#stop()
	 */
	@Override
	public void stop() {
		state.disable();
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#toString()
	 */
	@Override
	public String toString() {
		return "Sensor " + id.getName() + " (" + getNativeAddress() +") State: "+state.toString()+" AbstractProtocol: "+id.getPIDName().toString();
	}
}

