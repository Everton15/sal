package jcu.sal.config.plugins;

import java.io.File;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jcu.sal.common.Slog;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.utils.JaxbHelper;
import jcu.sal.components.protocols.AbstractProtocol;
import jcu.sal.config.plugins.xml.HelperNameClassType;
import jcu.sal.config.plugins.xml.NameClassType;
import jcu.sal.config.plugins.xml.ProtocolType;
import jcu.sal.config.plugins.xml.SalPlugins;

import org.apache.log4j.Logger;

/**
 * This class maintains a list of all known {@link AbstractProtocol}s and their device detection
 * filters.  
 * FIXME: This class needs rework.   
 * @author gilles
 */
public class PluginList {
	private static Logger logger = Logger.getLogger(PluginList.class);
	static {
		Slog.setupLogger(logger);
	}
	
	private static PluginList e;
	static{
		try {
			e = new PluginList();
		} catch (SALDocumentException e) {}
	}
	
	
	/**
	 * Map of all existing protocol names and their associated class 
	 */
	private Map<String,String> protocolTable = new Hashtable<String,String>();

	/**
	 * Map of all configuration plugin names and class names
	 * ie HalHelper - jcu.sal.plugins.protocols.owfs.HalClient
	 */
	private Map<String, List<String>> configPlugins = new Hashtable<String,List<String>>();
	
	/**
	 * Map of all existing endpoint names and their associated class 
	 */
	private Map<String, String> endPointTable = new Hashtable<String,String>();
	
	private Map<String, String> configHelperTable = new Hashtable<String,String>();
	
	private SalPlugins type;
	


	private PluginList() throws SALDocumentException {
		if(System.getProperty("jcu.sal.plugin.configFile")!=null) {
			try {
				type = JaxbHelper.fromFile(SalPlugins.class , new File(System.getProperty("jcu.sal.plugin.configFile")));
			} catch (SALDocumentException e) {
				logger.error("Cant parse plugin XML config");
				e.printStackTrace();
				throw e;
			}
			parseXML();
		} else
			System.err.println("Can not find the plugin config file, property missing");
	}
	
	private void parseXML(){
		parseProtocols();
		parseEndPoints();
		parseConfigPlugins();
	}
	
	private void parseProtocols(){
		for(ProtocolType p: type.getProtocolPlugins().getProtocol()){
			protocolTable.put(p.getName(), p.getClazz());
			for(HelperNameClassType n : p.getConfigPlugin()){
				if(!configPlugins.containsKey(n.getHelperName()))
					configPlugins.put(n.getHelperName(), new Vector<String>());

				configPlugins.get(n.getHelperName()).add(n.getClazz());
			}
		}
	}	
	private void parseEndPoints(){
		for(NameClassType n : type.getEndPointPlugins().getEndPoint())
			endPointTable.put(n.getName(), n.getClazz());
	}
	
	private void parseConfigPlugins(){
		for(NameClassType n : type.getConfigHelpers().getHelper())
			configHelperTable.put(n.getName(), n.getClazz());
	}
	
	/**
	 * this method returns the class name for a protocol given its name
	 * @param n the name of the protocol
	 * @return the class name
	 * @throws ClassNotFoundException if no protocols have the given name
	 */
	public static String getProtocolClassName(String type) throws ClassNotFoundException {	
		String c = e.protocolTable.get(type);
		if (c==null) {
			logger.error("Cant find the protocol class name from protocol type: " + type);
			throw new ClassNotFoundException("Cant find the protocol class name from protocol type: " + type);
		}
		//else e.logger.debug("Found protocol class " + c + " for type " + type);
		
		return c;
	}
	
	/**
	 * This method returns a list of class names representing hardware filters for a given
	 * hardware detection probe. For instance, all HAL filters register with this object so
	 * that the {@link HalHelper} can retrieve a list of all its HAL filters. 
	 * @param fitlerName the name of the hardware detection probe, for instance {@link HalHelper#NAME}.
	 * @return a list of class names representing filters for the given hardware detection probe.
	 */
	public static List<String> getFilter(String fitlerName) {
		return new Vector<String>(e.configPlugins.get(fitlerName));
	}
	
	/**
	 * this method returns the class name for an endpoint given its name
	 * @param n the name of the endpoint 
	 * @return the class name
	 * @throws ClassNotFoundException if no endpoints have the given name
	 */
	public static String getEndPointClassName(String n) throws ClassNotFoundException
	{	
		String c = e.endPointTable.get(n);
		if (c==null) {
			logger.error("Cant find the class name from EndPoint type: " + n);
			throw new ClassNotFoundException("Cant find the class name from EndPoint type: " + n);
		}
		
		return c;
	}
	
	/**
	 * this method returns the class name of hardware detection helpers
	 * @return a list of class names used to help detecting hardware-related events
	 */
	public static List<String> getHelperClassNames(){	
		return new LinkedList<String>(e.configHelperTable.values());
	}
}
