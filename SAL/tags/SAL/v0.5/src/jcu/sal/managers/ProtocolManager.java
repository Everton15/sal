/**
 * 
 */
package jcu.sal.managers;

import java.io.NotActiveException;
import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.Iterator;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.common.Constants;
import jcu.sal.common.Response;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.cml.CMLDescriptions;
import jcu.sal.common.sml.SMLConstants;
import jcu.sal.components.Identifier;
import jcu.sal.components.EndPoints.EndPoint;
import jcu.sal.components.protocols.AbstractProtocol;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.components.sensors.Sensor;
import jcu.sal.components.sensors.SensorID;
import jcu.sal.config.FileConfigService;
import jcu.sal.events.EventDispatcher;
import jcu.sal.events.ProtocolListEvent;
import jcu.sal.utils.ProtocolModulesList;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * @author gilles
 * 
 */
public class ProtocolManager extends AbstractManager<AbstractProtocol> {

	private static ProtocolManager p = new ProtocolManager();
	private Logger logger = Logger.getLogger(ProtocolManager.class);
	private FileConfigService conf;
	private EventDispatcher ev;
	
	
	/**
	 * Private constructor
	 */
	private ProtocolManager() {
		super();
		Slog.setupLogger(this.logger);
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
	protected AbstractProtocol build(Node config, Identifier id) throws InstantiationException {
		AbstractProtocol p = null;
		String type=null;
		ProtocolID i = (ProtocolID) id;
		logger.debug("building protocol "+id.getName());
		try {
			type=getComponentType(config);

			logger.debug("AbstractProtocol type: " + type);
			String className = ProtocolModulesList.getProtocolClassName(type);

			Class<?>[] params = {ProtocolID.class, Hashtable.class, Node.class};
			Constructor<?> c = Class.forName(className).getConstructor(params);
			Object[] o = new Object[3];
			o[0] = i;
			o[1] = getComponentConfig(config);
			//logger.debug("AbstractProtocol config: " + XMLhelper.toString(config));
			o[2] = XMLhelper.getNode("/" + AbstractProtocol.PROTOCOL_TAG + "/" + EndPoint.ENPOINT_TAG, config, true);
			//logger.debug("EndPoint config: " + XMLhelper.toString((Node) o[2])); 
			p = (AbstractProtocol) c.newInstance(o);
			logger.debug("done building protocol "+p.toString());			
		} catch (ParseException e) {
			logger.error("Error while parsing the DOM document. XML doc:");
			logger.error(XMLhelper.toString(config));
			//e.printStackTrace();
			throw new InstantiationException();
		} catch (Exception e) {
			logger.error("Error in new protocol instanciation.");
			e.printStackTrace();
			logger.error("XML doc:\n");
			logger.error(XMLhelper.toString(config));
			throw new InstantiationException();
		}
		
		//check if there are other instances of the same type
		try {
			if(!p.supportsMultipleInstances() && getComponentsOfType(type).size()!=0) {
				logger.debug("Found another instance of type '"+type+"' which doesnt support multiple instance, deleting this protocol");
				throw new InstantiationException();
			}
		} catch (ConfigurationException e) {} //no other instances
		
		//Parse the protocol's configuration
		try {
			p.parseConfig();
		} catch (ConfigurationException e1) {
			logger.error("Error in the protocol configuration");
			throw new InstantiationException();
		}

		logger.debug("saving its config");
		try {
			conf.addProtocol(config);
		} catch (ConfigurationException e) {
			logger.error("Cant save the new protocol config");
			throw new InstantiationException();
		}
		
		try {
			ev.queueEvent(new ProtocolListEvent(ProtocolListEvent.PROTOCOL_ADDED, i.getName(), Constants.PROTOCOL_MANAGER_PRODUCER_ID));
		} catch (ConfigurationException e) {logger.error("Cant queue event");}
		return p;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.managers.ManagerFactory#getComponentID(org.w3c.dom.Document)
	 */
	@Override
	protected Identifier getComponentID(Node n) throws ParseException {
		Identifier id = null;
		try {
			id = new ProtocolID(XMLhelper.getAttributeFromName("//" + AbstractProtocol.PROTOCOL_TAG, AbstractProtocol.PROTOCOLNAME_TAG, n));
		} catch (Exception e) {
			e.printStackTrace();
			throw new ParseException("Couldnt find the protocol identifier", 0);
		}
		return id;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.managers.ManagerFactory#getComponentType(org.w3c.dom.Document)
	 */
	@Override
	protected String getComponentType(Node n) throws ParseException {
		String type = null;
		try {
			type = XMLhelper.getAttributeFromName("//" + AbstractProtocol.PROTOCOL_TAG, AbstractProtocol.PROTOCOLTYPE_TAG, n);
		} catch (Exception e) {
			throw new ParseException("Couldnt find the protocol type", 0);
		}
		return type;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.managers.ManagerFactory#remove(java.lang.Object)
	 */
	@Override
	protected void remove(AbstractProtocol component) {
		ProtocolID pid=component.getID();
		logger.debug("Removing protocol " + pid.toString());
		component.remove(this);
		SensorManager.getSensorManager().destroyComponents(component.getSensors());
		componentRemovable(pid);
		try {
			ev.queueEvent(new ProtocolListEvent(ProtocolListEvent.PROTOCOL_REMOVED,component.getID().getName(),Constants.PROTOCOL_MANAGER_PRODUCER_ID));
		} catch (ConfigurationException e) {logger.error("Cant queue event");}
	}
	
	/*
	 * 
	 *  START OF SALAgent API methods
	 * 
	 */

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
		
		Iterator<Node> iter = conf.getProtocols().iterator();
		while(iter.hasNext()) {
			try {
				createComponent(iter.next());
			} catch (ConfigurationException e){}
		}
				
		iter = conf.getSensors().iterator();
		while(iter.hasNext()) {
			try {
				SensorManager.getSensorManager().createComponent(iter.next());
			} catch (ConfigurationException e) {
				logger.error("Could not create the sensor");
			}
		} 
		logger.debug("Finished parsing the configuration files");
	}

	/**
	 * Starts all the protcols  at once
	 */
	public void startAll(){
		synchronized(ctable){
			Iterator<AbstractProtocol> iter = ctable.values().iterator();
			while (iter.hasNext()) {
				AbstractProtocol e = iter.next();
				//logger.debug("Starting protocol" + e.toString());
				try { e.start(); }
				catch (ConfigurationException ex) { 
					logger.error("Couldnt start protocol " + e.toString()+"...");
				}
			}
		}
	}
	
	/**
	 * Stops all the protcols  at once
	 */
	public void stopAll(){
		synchronized(ctable){
			Iterator<AbstractProtocol> iter = ctable.values().iterator();
			while (iter.hasNext()) {
				AbstractProtocol e = iter.next();
				logger.debug("Stopping protocol" + e.toString());
				e.stop();
			}
		}
	}
	
	/**
	 * Sends a command to a sensor
	 * @param c the command
	 * @param sid the sensor
	 * @return the result
	 * @throws ConfigurationException if the sensor isnt associated with any protocol
	 * @throws BadAttributeValueExpException if the command cannot be parsed/is incorrect
	 * @throws NotActiveException if the sensor is not available to run commands
	 */
	public Response execute(Command c, SensorID sid) throws ConfigurationException, BadAttributeValueExpException, NotActiveException {
		return new Response(getProtocol(sid).execute(c, sid), sid.getName());
	}
	
	/**
	 * Retrieves the CML doc for a given sensor
	 * @param sid the sensorID
	 * @return the CML document
	 * @throws ConfigurationException if the sensor isnt associated with a protocol
	 * @throws NotActiveException
	 */
	public CMLDescriptions  getCML(SensorID sid) throws ConfigurationException, NotActiveException {
		return getProtocol(sid).getCML(sid);
	}
	
	/**
	 * This method removes the protocol XMl configuration from the platform configuration XML file.
	 * @param pid the protocol ID to be removed 
	 * @param removeSensors whether or not to remove the sensors configuraion associate with this protocol too
	 * @throws ConfigurationException if the protocol is still active, ie it hasnt been removed first.
	 */
	public void removeProtocolConfig(ProtocolID pid, boolean removeSensors) throws ConfigurationException{
		//Check if the sensor is still active
		if(getComponent(pid)!=null) {
			logger.error("Cant remove an active protocol configuration");
			throw new ConfigurationException();
		}
		try { conf.removeProtocol(pid);}
		catch (ConfigurationException e) { logger.error("error deleting the protocol config");}
		if(removeSensors) {
			try { conf.removeSensors(pid);}
			catch (ConfigurationException e) { logger.error("error deleting the associated sensors' config");}
		}
	}
	
	/*
	 * 
	 * END OF SALAgent API METHODS
	 * 
	 */
		
	/**
	 * Adds a sensor to the appropriate protocol. Checks if this sensor is supported by the protocol
	 * @return the protocol to which the sensor has been added 
	 * @throws ConfigurationException if the sensor cannot be added (wrong ProtocolName field, or unsupported sensor)
	 */
	AbstractProtocol associateSensor(Sensor sensor) throws ConfigurationException{
		AbstractProtocol p = null;
		String pname = null;
		try {
			pname = sensor.getConfig(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE);
			if((p = getComponent(new ProtocolID(pname)))!=null) {
					p.associateSensor(sensor);
			} else {
				logger.error("Cant find protocol "+pname+"to associate the sensor with");
				throw new ConfigurationException();				
			}
		} catch (BadAttributeValueExpException e) {
			logger.error("Can not find the protocol name to associate the sensor with");
			logger.error("Cant associate sensor " + sensor.getID().toString() + "(Cant find protocol " + pname+")");
			throw new ConfigurationException("cant find protocol from sensor config");
		}
		return p;
	}
	
	/**
	 * Unassociate a sensor from the protocol.
	 * This method returns only when the sensor is unassociated, ie if a command is being run while this method is
	 * called, it will block until the command finishes, then unassociated the sensor, thereby preventing it from 
	 * being used again.
	 * @throws ConfigurationException if the sensor cannot be unassociated (wrong ProtocolName field, or unsupported sensor)
	 */
	void unassociateSensor(Sensor s) throws ConfigurationException{
		AbstractProtocol p = null;
		String pname = null;
		try {
			pname = s.getConfig(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE);
			if((p = getComponent(new ProtocolID(pname)))!=null)
				p.unassociateSensor(s.getID());
			else
				throw new ConfigurationException("Cant find "+pname);
		} catch (BadAttributeValueExpException e) {
			logger.error("Can not find the protocol name to unassociate the sensor from");
			logger.error("Cant unassociate sensor " + s.getID().toString() + "(Cant find protocol " + pname+")");
			throw new ConfigurationException("cant find protocol from sensor config");
		} catch (ConfigurationException e) {
			logger.error("Can not find the protocol name to unassociate the sensor from");
			logger.error("Cant unassociate sensor " + s.getID().toString() + "(Cant find protocol " + pname+")");
			throw e;
		}
	}	
	
	/**
	 * Returns the protcol associated with a SensorID (assuming the sensor owner is already associated with a protocol)
	 * @throws ConfigurationException if the protocol can not be found
	 * @throws NotActiveException if the sensor can not be foundx 
	 */
	AbstractProtocol getProtocol(SensorID sid) throws NotActiveException, ConfigurationException{
			AbstractProtocol p=null;
			String pName = null;
			Sensor s;

			//TODO fix all the methods that should return an exception instead of a null pointer
			//TODO so we can get rid of all the if statments and only have try/catch stuff.
			//TODO fix the Identifier issue: with a sensor ID (a single int), to get the protocol associated
			//TODO with it, we have to do the following 4 lines ... ugly !
			if((s=SensorManager.getSensorManager().getComponent(sid))==null) {
				//logger.error("Cannot find the any sensor with this sensorID: " + sid.toString());
				throw new NotActiveException("No sensor with this sensorID");
			}
			if((pName = s.getID().getPIDName())==null){
				logger.error("Cannot find the protocolID associated with this sensorID: " + sid.toString());
				throw new ConfigurationException();
			}
			if((p=getComponent(new ProtocolID(pName)))==null){
				logger.error("Cannot find the protocol associated with this sensorID: " + sid.toString());
				throw new ConfigurationException();
			}

			return p;
	}
	

}
