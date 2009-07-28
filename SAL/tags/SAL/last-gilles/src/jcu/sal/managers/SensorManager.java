/**
 * 
 */
package jcu.sal.managers;

import java.util.HashSet;
import java.util.Set;

import jcu.sal.common.Constants;
import jcu.sal.common.Slog;
import jcu.sal.common.events.SensorNodeEvent;
import jcu.sal.common.exceptions.ComponentInstantiationException;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.common.sml.SMLDescription;
import jcu.sal.common.sml.SMLDescriptions;
import jcu.sal.components.Identifier;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.components.sensors.Sensor;
import jcu.sal.components.sensors.SensorID;
import jcu.sal.config.FileConfigService;
import jcu.sal.events.EventDispatcher;

import org.apache.log4j.Logger;


/**
 * @author gilles
 * 
 */
public class SensorManager extends AbstractManager<Sensor, SMLDescription> {
	private static Logger logger = Logger.getLogger(SensorManager.class);
	static {Slog.setupLogger(logger);}
	
	/**
	 * specifies (in seconds) how often the sensor removal thread kick in
	 */
	public static int REMOVE_SENSOR_INTERVAL = 0;
	
	private static SensorManager s = new SensorManager();

	private FileConfigService conf;
	private ProtocolManager pm;
	private EventDispatcher ev;
	
	
	/**
	 * Private constructor
	 */
	private SensorManager() {
		super();
		pm = ProtocolManager.getProcotolManager();
		conf = FileConfigService.getService();
		ev = EventDispatcher.getInstance();
		ev.addProducer(Constants.SENSOR_MANAGER_PRODUCER_ID);
	}

	/**
	 * Returns the instance of the SensorManager 
	 * @return
	 */
	public static SensorManager getSensorManager() {
		return s;
	}

	@Override
	protected Sensor build(SMLDescription s, Identifier id) throws ComponentInstantiationException {
		SensorID i = (SensorID) id;
		Sensor sensor = null;
		//logger.debug("building sensor: "+id.getName());
		
		//check if the ID is the same as in the config object
		if(!id.getName().equals(s.getID())) {
			try {
				s = new SMLDescription(new Integer(id.getName()),s.getParameters());
			} catch (SALDocumentException e1) {
				logger.error("We shouldnt be here - error creating the new SMLDescription from the old one");
				e1.printStackTrace();
				throw new SALRunTimeException("Cant create a new SMLDescription",e1);
			}
		}
		
		//build the sensor
		sensor = new Sensor(i, s);

		
		//save sensor config
		conf.addSensor(s);

		//associate it with its protocol
		try { pm.associateSensor(sensor); }
		catch (ConfigurationException e) {
			logger.error("Couldnt associate sensor '"+s.getID()+"' with protocol '"+s.getProtocolName()+"'");
			throw new ComponentInstantiationException();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ev.queueEvent(new SensorNodeEvent(SensorNodeEvent.SENSOR_NODE_ADDED, i.getName(), Constants.SENSOR_MANAGER_PRODUCER_ID));
		
		logger.debug("created sensor: "+id.getName());
		return sensor;
	}
	
	@Override
	protected Identifier getComponentID(SMLDescription s){
		/*
		 * The order in which a sensor id is looked up:
		 * first, the sensor config file is checked. if it isnt in the config file, a new one is generated
		 */
		Identifier id = null;
		try {
			//we first check to see if the sensor exists in the sensor configuration file
			id =conf.findSensor(s);
			//logger.debug("Found the sid "+id.getName()+" in sensor config file");
		} catch (NotFoundException e) {
			//we havent found a matching sensor in the sensor config file, so we are going to generate a new ID
			id = new SensorID(generateNewSensorID());
			//logger.debug("created a new sensor id "+id.getName());
		}

		return id;
	}
	

	@Override
	protected void remove(Sensor component) {
		try {
			pm.unassociateSensor(component);
		} catch (ConfigurationException e) {
			logger.error("Error unassociating sensor");
			e.printStackTrace();
		}
		component.remove(this);
		ev.queueEvent(new SensorNodeEvent(SensorNodeEvent.SENSOR_NODE_REMOVED,component.getID().getName(),Constants.SENSOR_MANAGER_PRODUCER_ID));
		logger.debug("removed sensor: "+component.getID().getName());
	}

	
	/**
	 * This method returns an SMLDescriptions object for all sensors (if <code>onlyActive</code> is false), or only for
	 * currently-active sensors otherwise.
	 * @param onlyActive if set, the returned SMLDescriptions will be limited to cuurently active sensors. Otherwise, all
	 * known sensors will be included.   
	 * @return an SMLDescriptions object for the selected set of sensors
	 */
	public SMLDescriptions listSensors(boolean onlyActive){
		if(onlyActive){
			HashSet<SMLDescription> m = new HashSet<SMLDescription>();
			synchronized(ctable){
				for(Sensor s: ctable.values())
					if(!s.isDisconnected())
						m.add(s.getConfig());
			}
			return new SMLDescriptions(m);
		} else
			return new SMLDescriptions(conf.getSensors());		
	}
	
	/**
	 * This method returns an SMLDescriptions object for a sensor given its identifier
	 * @param id the sensor identifier   
	 * @return an SMLDescriptions object for the sensor
	 * @throws NotFoundException if no sensor match the given sensor ID
	 */
	public SMLDescription listSensor(SensorID id) throws NotFoundException{
		synchronized(ctable){
			if(ctable.containsKey(id))
				return ctable.get(id).getConfig();
		}		
		throw new NotFoundException("No sensor matching ID: "+id.toString());
	}
	
	/**
	 * This method removes a sensor's XML config information from the sensor config file
	 * @param sid the sensor ID for which the configuration information must be removed
	 * @throws ConfigurationException if the sensor is still active or the config info cant be deleted
	 * @throws NotFoundException if the sensor ID doesnt match any existing sensor
	 */
	public void removeSensorConfig(SensorID sid) throws ConfigurationException, NotFoundException {
		//Check if the sensor is still active
		if(getComponent(sid)!=null) {
			logger.error("Cant remove an active sensor configuration");
			throw new ConfigurationException("Cant remove a sensor's configuration if it hasnt been removed first");
		}

		conf.removeSensor(sid);
	}
	
	/**
	 * This method creates sensors for the specified protocol using information found 
	 * in the sensor configuration file.
	 * @param pid the protocol ID for which sensors must be created
	 * @throws ConfigurationException if the sensor is still active or the config info cant be deleted
	 */
	public void loadSensorsFromConfig(ProtocolID pid) throws ConfigurationException {
		//logger.debug("Loading sensors from config file associated with protocol "+pid.getName());
		for(SMLDescription s: conf.listSensors(pid))
			createComponent(s);
	}

	
	/**
	 * Returns the first available unused sensor ID
	 * Must hold the lock to ctable
	 * @return the first available unused sensor ID
	 */
	private String generateNewSensorID() {
		//Get the SIDs existing in sensor config file
		Set<String> sids = conf.listSensorID();

		int i = 1;
		while(sids.contains(String.valueOf(i))) i++;
		
		return String.valueOf(i);
	}
}
