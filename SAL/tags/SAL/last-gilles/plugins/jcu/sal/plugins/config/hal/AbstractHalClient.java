package jcu.sal.plugins.config.hal;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jcu.sal.common.Parameters;
import jcu.sal.common.Slog;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.components.Identifier;
import jcu.sal.components.protocols.AbstractProtocol;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.config.FileConfigService;
import jcu.sal.managers.ProtocolManager;

import org.apache.log4j.Logger;

import au.edu.jcu.haldbus.filter.AbstractHalFilter;

public abstract class AbstractHalClient extends AbstractHalFilter {
	private ProtocolManager pm;
	private static Logger logger = Logger.getLogger(AbstractHalClient.class);
	static {Slog.setupLogger(logger);}
	
	protected AbstractHalClient(String n, int when){
		super(n, when);
		pm = ProtocolManager.getProcotolManager();
	}
	
	protected AbstractHalClient(String n){
		this(n, ALWAYS_RUN_FLAG);
	}
	
	/**
	 * This method returns whether a protocol of a given type is already instantiated
	 * @param type the type of protocols
	 * @return whether a protocol of a given type is already instantiated
	 */
	protected boolean isProtocolRunning(String type){
		try {
			return pm.getComponentsOfType(type).size()!=0;
		} catch (NotFoundException e) {
			return false;
		}
	}
	
	/**
	 * This method returns a Map of configuration directives for each protocol of a given type. If there are no protocols of
	 * the given type, an empty list is returned.
	 * @param type the type of protocols
	 * @return a Map of their configuration
	 */
	protected Map<Identifier, Parameters> findRunningProtocolConfigFromType(String type){
		Map<Identifier, Parameters> ret = new Hashtable<Identifier, Parameters>();
		try {
			Iterator<Identifier> i = pm.getComponentsOfType(type).iterator();
			Identifier id;
			while(i.hasNext()) {
				id = i.next();
				ret.put(id, pm.getComponent(id).getParameters());
			}
		} catch (NotFoundException e){}
			
		return ret;
	}
	
	/**
	 * This method gathers the configuration directives of running protocols of a given type. It then searches these directives
	 * for a specified name/value pair given in second and third argument. The returned list is empty if there are no matches.
	 * @param type the type of running protocols to search  
	 * @param param the name of the directive to search for
	 * @param value the expected value of the searched directive
	 * @return a list of protocol identifiers matching the criteria
	 */
	protected List<Identifier> findRunningProtocolNameFromConfig(String type, String param, String value){
		List<Identifier> l = new Vector<Identifier>();
		Map<Identifier, Parameters> m = findRunningProtocolConfigFromType(type);
		Identifier id;
		Iterator<Identifier> i = m.keySet().iterator();

		while(i.hasNext()){
			id = i.next();
			try {
				if(m.get(id).hasValue(param,value))
					l.add(id);
			} catch (Exception e){}				
		}
		return l;
	}
	
	/**
	 * this method checks the File config service to retreive the whole configuration document of a protocol
	 * given part of it. Given a parameter name (ex: "device_file"), and the expected value (ex: "/dev/video0"),
	 * the File config service will look for a protocol configuration containing this parameter and its value, and if
	 * found, the protocol configuration object will be returned.  
	 * @param param the parameter name
	 * @param value the expected parameter value
	 * @return the protocol configuration object , if found 
	 * @throws NotFoundException if nothing matches the given values 
	 */
	protected ProtocolConfiguration findProtocolConfigFromFile(String param, String value) throws NotFoundException {
		return FileConfigService.getService().findProtocol(param, value);
	}
	
	/**
	 * this method creates and starts a protocol givne its 
	 * {@link ProtocolConfiguration} object
	 * @param pc the configuration object
	 * @throws ConfigurationException
	 */
	protected void createProtocol(ProtocolConfiguration pc) throws ConfigurationException{
		//logger.debug("Creating new protocol with document: \n"+pc.getXMLString());
		AbstractProtocol p;
		try {p = pm.createComponent(pc);}
		catch (Throwable t){
			logger.error("Cant instanciate protocol");
			t.printStackTrace();
			throw new ConfigurationException("Error instanciating the new protocol",t);
		}
		try{p.start();}
		catch(Throwable t){
			logger.error("Error starting protocol - removing it");
			t.printStackTrace();
			try {
				pm.destroyComponent(p.getID());
			} catch (NotFoundException e) {
				//we shouldnt be here...
			}
			throw new ConfigurationException("Error starting the new protocol",t);
		}
	}
	/**
	 * This method removes a protocol and its configuration
	 * @param i the protocol identifier
	 * @throws ConfigurationException if there is an error removing the configuration
	 * @throws NotFoundException if no protocol matches the given id
	 */
	protected void removeProtocol(Identifier i) throws ConfigurationException, NotFoundException{
		//logger.debug("Removing existing protocol "+i.toString());
		pm.destroyComponent(i);
		pm.removeProtocolConfig((ProtocolID) i, false);
	}
	
}
