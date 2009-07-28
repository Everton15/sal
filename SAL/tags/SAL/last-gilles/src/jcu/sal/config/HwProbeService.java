package jcu.sal.config;

import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import jcu.sal.common.Slog;
import jcu.sal.config.plugins.PluginList;

import org.apache.log4j.Logger;

/**
 * This class checks what Hardware probes are available
 * It offers methods for starting and stopping them.
 * @author gilles
 *
 */
public class HwProbeService{
	private static Logger logger = Logger.getLogger(HwProbeService.class);
	static {Slog.setupLogger(logger);}
	
	private static HwProbeService d = new HwProbeService();
	
	/**
	 * This map contains a list of helper classes, and a reference to their instance.
	 */
	private Map<String, HwProbeInterface> helperMap;
	
	/**
	 * Singleton contructor
	 *
	 */
	private HwProbeService(){
		helperMap = new Hashtable<String, HwProbeInterface>();
	}
	
	/**
	 * This method returns an instance of the DeviceDetectionService.
	 * @return
	 */
	public static HwProbeService getService(){
		return d;
	}
	
	/**
	 * This method reloads the list of helpers and starts the new ones
	 *
	 */
	public synchronized void loadAll() {
		Map<String, HwProbeInterface> m = loadHelpers(); 
		HwProbeInterface h;
		logger.debug("reloading all hardware detection probes");
		for(String name: m.keySet()){
			try {
				h = m.get(name);
				//add filters to probe first
				//so they are notified during the initial run
				//which happens when h.start() is invoked
				h.listChanged();
				h.start();
				helperMap.put(name, h);
			} catch (Exception e) {
				logger.error("error starting helper");
				e.printStackTrace();
			}	
		}
	}
	
	public synchronized void stopAll() {
		logger.debug("Removing all hardware detection probes");
		for(String name: helperMap.keySet()){
			//logger.debug("Stopping "+name);
			helperMap.get(name).stop();
			helperMap.remove(name);
		}
	}
	
	
	/**
	 * This methods fetches the list of known DeviceDetection helpers and instantiate any new ones
	 * that are not in <code>m</code> already. It then returns a list of the newly instantiated helpers.
	 * Helpers that have been removed from the list as returned by DeviceDetectionList, are already loaded
	 * will be removed when <code>stopAll()</code> is invoked.
	 * @return a list of the newly instantiated helpers (if any)
	 */
	private Map<String, HwProbeInterface> loadHelpers(){
		Map<String, HwProbeInterface> ret = new Hashtable<String, HwProbeInterface>();
		List<String> list = PluginList.getHelperClassNames();
		for (String name : list) {
			//Instantiate the new helper if it isnt in the map already
			if(!helperMap.containsKey(name)) {
				Constructor<?> c;
				try {
					c = Class.forName(name).getConstructor(new Class<?>[0]);
					HwProbeInterface h = (HwProbeInterface) c.newInstance(new Object[0]);
					ret.put(name, h);
				} catch (Throwable t) {
					logger.error("Cant instanciate hardware probe "+name);
					t.printStackTrace();
				}
			}
		}
		return ret;
	}

	public void listChanged() {
		new Thread(new Runnable() {
			public void run() {loadAll();}
		}).start();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((helperMap == null) ? 0 : helperMap.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HwProbeService other = (HwProbeService) obj;
		if (helperMap == null) {
			if (other.helperMap != null)
				return false;
		} else if (!helperMap.equals(other.helperMap))
			return false;
		return true;
	}
}
