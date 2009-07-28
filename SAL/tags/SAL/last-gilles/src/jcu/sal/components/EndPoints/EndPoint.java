/**
 * @author gilles
 */
package jcu.sal.components.EndPoints;

import java.util.ArrayList;
import java.util.Hashtable;

import jcu.sal.common.Slog;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.pcml.EndPointConfiguration;
import jcu.sal.components.AbstractComponent;
import jcu.sal.components.componentRemovalListener;
import jcu.sal.components.protocols.ProtocolID;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public abstract class EndPoint extends AbstractComponent<EndPointID, EndPointConfiguration> {
	
	private static Logger logger = Logger.getLogger(EndPoint.class);
	static { Slog.setupLogger(logger); }

	
	/**
	 * The table containing the device change listeners and their associated usb IDs
	 * access to this variable must be synchronized(listeners)
	 */
	protected static Hashtable<String,ArrayList<DeviceListener>> listeners;
	
	protected boolean enabled;
	protected boolean configured;
	protected boolean autodetect=false;

	
	/**
	 * This constructor initialises the attributes in this abstract class, and checks that
	 * the given EndPointConfiguration is of type 't'
	 * @param i the EndPoint ID associated with this endpoint
	 * @param t the type of this endpoint
	 * @param c the EndPointConfiguration object associated with this EP
	 * @throws ConfigurationException if the given configuration object isnt of type 't'
	 */
	public EndPoint(EndPointID i, String t, EndPointConfiguration c) throws ConfigurationException{
		super(c,i);
		if(!c.getType().equals(t))
			throw new ConfigurationException("Configuration object is of type '"+c.getType()+"', expected "+t);
		enabled=false;
		configured=false;
	}
	
	/**
	 * returns a textual representation of a End Point's instance
	 * @return the textual representation of the Logical Port's instance
	 */
	public String toString() {
		return "EndPoint "+id.getName()+"("+config.getType()+")";
	}
	

	/* (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#remove()
	 */
	@Override
	public final void remove(componentRemovalListener c) {
		synchronized (this) {
			if(enabled)
				stop();
			configured=false;
			internal_remove();
			//logger.debug(config.getType()+" Endpoint removed");	
		}
		c.componentRemovable(id);
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#start()
	 */
	@Override
	public final void start() throws ConfigurationException{
		synchronized (this) {
			if(configured && !enabled) {
				//logger.debug("Starting "+config.getType()+" Endpoint.");
				internal_start();
				enabled=true;
			}
		}
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#stop()
	 */
	@Override
	public final void stop() {
		synchronized (this) {
			if(enabled) {
				//logger.debug("Stopping "+config.getType()+" Endpoint.");
				internal_stop();
				enabled=false;
			}
		}
	}

	
	/**
	 * Stops the endpoint.
	 * this method should be overridden by Endpoints if more things need to be done 
	 */
	protected void internal_stop() {}
	
	/**
	 * Starts the endpoint.
	 * this method should be overridden by Endpoints if more things need to be done 
	 */
	protected void internal_start() throws ConfigurationException {}
	
	/**
	 * Prepare the subclass to be removed 
	 * this method should be overridden by Endpoints if more things need to be done 
	 */
	protected void internal_remove() {}
	
	/**
	 * Sets the protocol with which we are associated
	 */
	public final void setPid(ProtocolID i){
		id.setPid(i);
	}

	
	/**
	 * this method registers device change listeners. Whenever a device is plugged/unplugged,
	 * its associated device listeners are notified.
	 * @param d the device listener which should be notifier
	 * @param ids the device ID which should be watched
	 * @throws UnsupportedOperationException if the EndPoint does not support autodetection
	 */
	public final void registerDeviceListener(DeviceListener d, String[] ids) throws UnsupportedOperationException{
		if(!autodetect)
			throw new UnsupportedOperationException("Autodetection of sensor native controllers not supported by this Endpoint");
		
		for (int i = 0; i < ids.length; i++) {
			synchronized (listeners) {
				if(!listeners.containsKey(ids[i])) 
					listeners.put(ids[i], new ArrayList<DeviceListener>());
				listeners.get(ids[i]).add(d);
			}
			//logger.debug("Added device listener for ID: '"+ids[i]+"', "+listeners.containsKey(ids[i])+" - "+listeners.get(ids[i]));
		}
	}
	/**
	 * This method unregisters device listeners
	 * @param d the listener to be removed
	 * @throws UnsupportedOperationException if the EndPoint does not support autodetection
	 */
	public final void unregisterDeviceListener(DeviceListener d) throws UnsupportedOperationException{
		if(!autodetect)
			throw new UnsupportedOperationException("Autodetection of sensor native controllers not supported by this Endpoint");
		
		synchronized (listeners) {
			for(ArrayList<DeviceListener> a: listeners.values())
				a.remove(d);	
		}
	}

	/**
	 * This method returns the number of currently connected devices with a given set of USB IDs. This method is
	 * primarily used by Protocols at instanciation. These protocols may need to know the number of initially connected
	 * devices they re interested in.  
	 * @param ids the USB IDs we re looking for
	 * @return the number of currently connected devices with the given set of USB IDs
	 * @throws UnsupportedOperationException if the EndPoint does not support autodetection
	 */
	public int getConnectedDeviceNum(String ids) throws UnsupportedOperationException{
		throw new UnsupportedOperationException("Autodetection of sensor native controllers not supported by this Endpoint");
	}	
	
}
