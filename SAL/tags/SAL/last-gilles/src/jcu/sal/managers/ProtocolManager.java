/**
 * 
 */
package jcu.sal.managers;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import jcu.sal.common.Constants;
import jcu.sal.common.Response;
import jcu.sal.common.Slog;
import jcu.sal.common.StreamID;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.cml.CMLDescriptions;
import jcu.sal.common.cml.StreamCallback;
import jcu.sal.common.events.ProtocolListEvent;
import jcu.sal.common.exceptions.ComponentInstantiationException;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.common.exceptions.SensorControlException;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.common.pcml.ProtocolConfigurations;
import jcu.sal.common.sml.SMLConstants;
import jcu.sal.common.sml.SMLDescription;
import jcu.sal.components.Identifier;
import jcu.sal.components.protocols.AbstractProtocol;
import jcu.sal.components.protocols.LocalStreamID;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.components.sensors.Sensor;
import jcu.sal.components.sensors.SensorID;
import jcu.sal.config.FileConfigService;
import jcu.sal.config.plugins.PluginList;
import jcu.sal.events.EventDispatcher;

import org.apache.log4j.Logger;

/**
 * @author gilles
 * 
 */
public class ProtocolManager extends AbstractManager<AbstractProtocol, ProtocolConfiguration> {
	private static Logger logger = Logger.getLogger(ProtocolManager.class);
	static {Slog.setupLogger(logger);}
	
	private static ProtocolManager p = new ProtocolManager();
	private static ResponseDispatcherThread dispatcher = new ResponseDispatcherThread();

	private FileConfigService conf;
	private EventDispatcher ev;
	
	
	/**
	 * Private constructor
	 */
	private ProtocolManager() {
		super();
		conf = FileConfigService.getService();
		ev = EventDispatcher.getInstance();
		ev.addProducer(Constants.PROTOCOL_MANAGER_PRODUCER_ID);
		ev.addProducer(Constants.SENSOR_STATE_PRODUCER_ID);
	}
	
	/**
	 * Returns the instance of the ProtocolManager 
	 * @return
	 */
	public static ProtocolManager getProcotolManager() {
		return p;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.managers.ManagerFactory#build(org.w3c.dom.Document)
	 */
	@Override
	protected AbstractProtocol build(ProtocolConfiguration config, Identifier id) throws ComponentInstantiationException  {
		AbstractProtocol p = null;
		String type=config.getType();
		ProtocolID i = (ProtocolID) id;
		try {

			//logger.debug("building AbstractProtocol type: " + type);
			String className = PluginList.getProtocolClassName(type);

			Class<?>[] params = {ProtocolID.class, ProtocolConfiguration.class};
			Constructor<?> c = Class.forName(className).getConstructor(params);
			Object[] o = new Object[2];
			o[0] = i;
			o[1] = config;
			//logger.debug("AbstractProtocol config: " + XMLhelper.toString(config));
			p = (AbstractProtocol) c.newInstance(o);
			//logger.debug("done building protocol "+p.toString());
		} catch (Throwable e) {
			logger.error("Error in new protocol instanciation.");
			e.printStackTrace();
			logger.error("XML doc:\n");
			logger.error(config.getXMLString());
			throw new ComponentInstantiationException("Unable to instantiate component",e);
		}
		
		//check if there are other instances of the same type
		try {
			if(!p.supportsMultipleInstances() && getComponentsOfType(type).size()!=0) {
				logger.debug("Found another instance of type '"+type+"' which doesnt support multiple instance, deleting this protocol");
				throw new ComponentInstantiationException("Found another instance of type '"+type+"' which doesnt support multiple instances");
			}
		} catch (NotFoundException e) {} //no other instances
		
		//Parse the protocol's configuration
		try {
			p.parseConfig();
		} catch (ConfigurationException e1) {
			logger.error("Error in the protocol configuration:\n"+e1.getMessage());
			//e1.printStackTrace();
			throw new ComponentInstantiationException();
		}

		//save protocol config
		conf.addProtocol(config);
		
		ev.queueEvent(new ProtocolListEvent(ProtocolListEvent.PROTOCOL_ADDED, i.getName(), Constants.PROTOCOL_MANAGER_PRODUCER_ID));
		logger.debug("Created protocol '"+config.getID()+"' - type: " + type);
		return p;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.managers.ManagerFactory#getComponentID(org.w3c.dom.Document)
	 */
	@Override
	protected Identifier getComponentID(ProtocolConfiguration n){
		return new ProtocolID(n.getID());
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.managers.ManagerFactory#remove(java.lang.Object)
	 */
	@Override
	protected void remove(AbstractProtocol component) {
		ProtocolID pid=component.getID();
		//logger.debug("Removing protocol " + pid.toString());
		component.remove(this);
		/** the sensors associated with the protocol must be removed AFTER the protocol
		 * otherwise the autodetection could try and create them again between the moment we remove them
		 * and the moment we remove the protocol
		 */
		SensorManager.getSensorManager().destroyComponents(component.getSensors());
		componentRemovable(pid);
		ev.queueEvent(new ProtocolListEvent(ProtocolListEvent.PROTOCOL_REMOVED,component.getID().getName(),Constants.PROTOCOL_MANAGER_PRODUCER_ID));
		logger.debug("Removed protocol '"+component.getID().getName()+"' - type: " + component.getType());
	}
	
	/*
	 * 
	 *  START OF SALAgent API methods
	 * 
	 */	
	
	/**
	 * This method removes the protocol configuration object from the platform configuration file.
	 * @param pid the protocol ID to be removed 
	 * @param removeSensors whether or not to remove the sensors configuration associate with this protocol too
	 * @throws ConfigurationException if the protocol is still active, ie it hasnt been removed first.
	 * @throws NotFoundException if the protocol ID doesnt match any existing protocol
	 */
	public void removeProtocolConfig(ProtocolID pid, boolean removeSensors) throws ConfigurationException, NotFoundException{
		//Check if the protocol is still active
		if(getComponent(pid)!=null) {
			logger.error("Cant remove an active protocol configuration");
			throw new ConfigurationException("Configuration cant be removed because the protocol hasnt been removed before");
		}
		conf.removeProtocol(pid);
		if(removeSensors)
			conf.removeSensors(pid);

	}

	/**
	 * Creates all protocols, associated endpoints and sensors given SML and PCML
	 * @throws ConfigurationException if there is a problem parsing the XML files
	 */
	public void init(String sml, String pcml) throws ConfigurationException {
		try {
			conf.init(pcml,sml);
		} catch (ConfigurationException e) {
			logger.error("Could not read the configuration files");
			throw e;
		}
		
		for(ProtocolConfiguration p: conf.getProtocols())
			try {createComponent(p);} catch (ConfigurationException e){} 
		
		for(SMLDescription s: conf.getSensors())
			try {SensorManager.getSensorManager().createComponent(s);} catch (ConfigurationException e){} 

	}
	
	/**
	 * This method stops the Protocol Manager. It must be called it <code>init()</code> was successful.
	 *
	 */
	public void stop() {
		conf.stop();
		dispatcher.stop();
	}
	
	/**
	 * This method returns an PlatformConfiguration object listing all the protocol configuration
	 * @param onlyActive if set, the returned SMLDescriptions will be limited to currently active sensors. Otherwise, all
	 * known sensors will be included.   
	 * @return an SMLDescriptions object for the selected set of sensors
	 */
	public ProtocolConfigurations listProtocols(){
		return new ProtocolConfigurations(conf.getProtocols());
	}

	/**
	 * Starts all the protocols  at once
	 */
	public void startAll(){
		synchronized(ctable){
			for(AbstractProtocol p: ctable.values())
				try { p.start(); }
				catch (ConfigurationException ex) { 
					logger.error("Couldnt start protocol " + p.toString()+"...");
				}
		}
	}
	
	/**
	 * Stops all the protocols  at once
	 */
	public void stopAll(){
		synchronized(ctable){
			for(AbstractProtocol p: ctable.values())
				p.stop();
		}
	}
	
	/**
	 * Setup a stream given a command and a sensor
	 * @param c the command
	 * @param sid the sensor
	 * @return a {@link StreamID}
	 * @throws SensorControlException if there is an error controlling the sensor
	 * @throws NotFoundException if the given sensor id doesnt match any existing sensor
	 */
	public LocalStreamID setupStream(Command c, SensorID sid) throws SensorControlException, NotFoundException {
		return getProtocol(sid).setupStream(c, sid);
	}
	
	/**
	 * STarts the given stream
	 * @param sid the stream id0
	 * @throws NotFoundException if the given stream id does not exit
	 */
	public void startStream(LocalStreamID lid) throws  NotFoundException {
		getProtocol(new SensorID(lid.getSID())).startStream(lid);
	}
	
	/**
	 * Stops the given stream
	 * @param sid the stream id0
	 * @throws NotFoundException if the given stream id does not exit
	 */
	public void stopStream(LocalStreamID sid) throws  NotFoundException {
		getProtocol(new SensorID(sid.getSID())).stopStream(sid);
	}

	/**
	 * Retrieves the CML doc for a given sensor
	 * @param sid the sensorID
	 * @return the CML document
	 * @throws NotFoundException if the specified sensor is not found
	 * @throws SALRunTimeException if the associated protocol cant be located
	 */
	public CMLDescriptions getCML(SensorID sid) throws NotFoundException {
		return getProtocol(sid).getCML(sid);
	}
	
	/*
	 * 
	 * END OF SALAgent API METHODS
	 * 
	 */
		
	/**
	 * Adds a sensor to the appropriate protocol. Checks if this sensor is supported by the protocol
	 * @return the protocol to which the sensor has been added 
	 * @throws NotFoundException if the protocol specified in the sensor config does not exist
	 * @throws ConfigurationException if the sensor config is invalid (no protocol name, or specified protocol not found)
	 */
	AbstractProtocol associateSensor(Sensor sensor) throws ConfigurationException, NotFoundException{
		AbstractProtocol p = null;
		String pname = null, ptype=null;
		try {
			pname = sensor.getParameter(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE);
			ptype = sensor.getParameter(SMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE);
		} catch (NotFoundException e) {
			logger.error("Can not find the protocol name / type to associate the sensor with");
			logger.error("Cant associate sensor " + sensor.getID().toString() + "(Cant find protocol " + pname+")");
			throw new ConfigurationException("cant find the name of protocol to associate with from sensor config", e);
		}
		
		if((p = getComponent(new ProtocolID(pname)))!=null) {
			if(ptype.equals(p.getType()))
				p.associateSensor(sensor);
			else {
				logger.error("Specified protocol type "+ptype+" doesnt match existing protocol name "+pname+" 's type ("+p.getType()+")");
				throw new ConfigurationException("Specified protocol type "+ptype+" doesnt match existing protocol name "+pname+" 's type ("+p.getType()+")");
			}
		} else {
			logger.error("Cant find protocol "+pname+" to associate the sensor with");
			throw new NotFoundException("Cant find protocol '"+pname+"'");				
		}
		
		return p;
	}
	
	/**
	 * Unassociate a sensor from the protocol.
	 * This method returns only when the sensor is unassociated, ie if a command is being run while this method is
	 * called, it will block until the command finishes, then unassociated the sensor, thereby preventing it from 
	 * being used again.
	 * @throws ConfigurationException if the protocol name cant be found in the sensor config, if the protocol name doesnt exist or
	 * if the sensor isnt associated with the protocol anymore
	 */
	void unassociateSensor(Sensor s) throws ConfigurationException{
		AbstractProtocol p = null;
		String pname = null;
		try {
			pname = s.getParameter(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE);
		} catch (NotFoundException e) {
			logger.error("Can not find the protocol name to unassociate the sensor from");
			logger.error("Cant unassociate sensor " + s.getID().toString() + "(Cant find protocol " + pname+")");
			throw new ConfigurationException("cant find protocol name in sensor config", e);
		} 
		if((p = getComponent(new ProtocolID(pname)))!=null)
			try {
				p.unassociateSensor(s.getID());
			} catch (NotFoundException e) {
				throw new ConfigurationException("Sensor not associated with protocol '"+pname+"'", e);
			}
		else {
			logger.error("Cant unassociate sensor " + s.getID().toString() + "(Cant find protocol " + pname+")");
			throw new ConfigurationException("cant find protocol name '"+pname+"'");
		}

	}	
	
	/**
	 * Returns the protocol associated with a SensorID (assuming the sensor owner is already associated with a protocol)
	 * @throws NotFoundException if the sensor can not be found
	 * @throws SALRunTimeException if the sensoor isnt associated with any protocol, or the protocol object cant be found (shouldnt happen, but...)
	 */
	AbstractProtocol getProtocol(SensorID sid) throws NotFoundException{
			AbstractProtocol p=null;
			String pName = null;
			Sensor s;

			if((s=SensorManager.getSensorManager().getComponent(sid))==null) {
				//logger.error("Cannot find the any sensor with this sensorID: " + sid.toString());
				throw new NotFoundException("No sensor with this sensorID");
			}
			if((pName = s.getID().getPIDName())==null){
				logger.error("Cannot find the protocolID associated with this sensorID: " + sid.toString());
				throw new SALRunTimeException("we shouldnt be here - sensor not associated with any protocol ??");
			}
			if((p=getComponent(new ProtocolID(pName)))==null){
				logger.error("Cannot find the protocol associated with this sensorID: " + sid.toString());
				throw new SALRunTimeException("We shouldnt be here - cant find the protocol the sensor is associated with");
			}

			return p;
	}
	
	public static void queueResponse(Response r, StreamCallback c){
		dispatcher.queueResponse(r, c);
	}
	
	private static class ResponseDispatcherThread implements Runnable{
		private static class Data{
			public Response r;
			public StreamCallback c;
			public Data(Response r, StreamCallback c){
				this.r = r;
				this.c = c;
			}
		}
		private int QUEUE_SIZE = 5000;
		Thread t;
		private BlockingQueue<Data> queue;
		
		public ResponseDispatcherThread(){
			t = new Thread(this, "Response dispatcher");
			queue = new LinkedBlockingQueue<Data>(QUEUE_SIZE);
			t.start();
		}
		
		public void stop() {
			t.interrupt();
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void queueResponse(Response r, StreamCallback c){
			if(!queue.offer(new Data(r,c)))
				logger.error("Cant queue event, queue full");
		}
		
		public void run(){
			Data d;
			try {
				while(!Thread.interrupted()){
					d = queue.take();
					try {d.c.collect(d.r);} catch (IOException e) {}
				}
			} catch (InterruptedException e1) {}
		}
	}
}
