package jcu.sal.plugins.config.hal;


import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jcu.sal.common.Slog;
import jcu.sal.config.HwProbeInterface;
import jcu.sal.config.plugins.PluginList;

import org.apache.log4j.Logger;

import au.edu.jcu.haldbus.HardwareWatcher;
import au.edu.jcu.haldbus.filter.HalFilterInterface;

/**
 * Objects of this class run a single thread waiting for HAL DeviceAdded/DeviceRemoved signals. When a DeviceAdded signal is received, 
 * a list of {@link HalFilterInterface} objects is matched against the new device's properties. If enough 
 * matches are successful, {@link HalFilterInterface#deviceAdded(Map)} is called. When a DeviceRemoved signal is received, 
 * {@link HalFilterInterface#deviceRemoved(Map)} is called. In both cases, the given map contains the value of properties
 * of interest as contained in the {@link HalFilterInterface}.   
 * The list of {@link HalFilterInterface} objects can be changed at runtime using {@link #addClient(HalFilterInterface)}, 
 * {@link #removeClient(HalFilterInterface)},  or
 * @author gilles
 *
 */
public class HalHelper implements HwProbeInterface{
	private static Logger logger = Logger.getLogger(HalHelper.class);
	static {Slog.setupLogger(logger);}
	
	/**
	 * The name of this helper
	 */
	public final static String NAME = "HalHelper";

	private HardwareWatcher watcher;
	
	/**
	 * Default constructor. It initialises the new object's members.
	 * No clients are registered at this stage. This must be done by calling 
	 * {@link #registerNewClients()} subsequently;
	 */
	public HalHelper(){
		watcher = new HardwareWatcher();
	}

	@Override
	public void start() throws Exception{
		watcher.start();
	}
	
	@Override
	public synchronized void stop(){
		watcher.stop();
	}

	
	/**
	 * FIXME: add code to ProtocolModuleList to call this method whenever filters 
	 * (from a new protocol) are installed or removed
	 * This method is called whenever the current client filter list has changed 
	 * (new elements or existing ones removed). 
	 */
	@Override
	public void listChanged() {
		Vector<HalFilterInterface> v = new Vector<HalFilterInterface>();

		//Create the new Halfilter list 
		for (String name : getNewFilterList())
			try {
				v.add(createClient(name));
			} catch (InstantiationException e) {
				logger.error("error instantiating filter "+name);
			} 
		
		//register it
		watcher.updateClientList(v);
	}


	/*
	 * PRIVATE METHODS 
	 */
	
	private List<String> getNewFilterList(){
		return PluginList.getFilter(NAME);
	}
	
	/**
	 * this method creates a filter given its class name.
	 * @return the filter
	 * @throws InstantiationException if the client filter can not be created for some reason.
	 */
	private HalFilterInterface createClient(String className) throws InstantiationException{
		Constructor<?> c;
		HalFilterInterface h=null;
		try {
			c = Class.forName(className).getConstructor(new Class<?>[0]);
			h = (HalFilterInterface) c.newInstance(new Object[0]);
		} catch (Throwable t) {
			logger.error("Cant instanciate filter "+className);
			t.printStackTrace();
			throw new InstantiationException();
		}
		return h;		
	}
	
	public static void main(String[] args){
		
	}
}
