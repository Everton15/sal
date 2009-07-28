/**
 * 
 */
package jcu.sal.plugins.endpoints;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import jcu.sal.common.Slog;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.pcml.EndPointConfiguration;
import jcu.sal.components.EndPoints.DeviceListener;
import jcu.sal.components.EndPoints.EndPoint;
import jcu.sal.components.EndPoints.EndPointID;
import jcu.sal.utils.PlatformHelper;
import jcu.sal.utils.PlatformHelper.ProcessOutput;

import org.apache.log4j.Logger;


/**
 * @author gilles
 *
 */
public class UsbEndPoint extends EndPoint{

	private static Logger logger = Logger.getLogger(EndPoint.class);
	static {Slog.setupLogger(logger);}
	public static String ENDPOINT_TYPE="usb";
	private static String LSUSBOUTPUT_KEY = "Bus";
	
	/**
	 * Contains the USB ID of the USB host controllers so they can be filtered out when
	 * looking for new USB devices
	 */
	private static String NODEVICE_USBID = "0000:0000";
	
	/**
	 * Contains the currently connected devices IDs (String) and how many of these there are (Integer)
	 */
	private  static Hashtable<String, Integer> devices;
	
	/**
	 * The thread watching for adapter devices plug/unplug events
	 */
	private static Autodetection deviceWatcher= null;
	
	/**
	 * How many UsbEndPoint are there ? (used as ref count to know when to stop the thread)
	 */
	private static int ep_nb=0;
	
	/**
	 * how often the thread should chek for plugged/unplugged devices (in milliseconds)
	 */
	protected static long deviceWatchInterval = 1 * 1000;


	
	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public UsbEndPoint(EndPointID i, EndPointConfiguration c) throws ConfigurationException {
		super(i,ENDPOINT_TYPE,c);
		devices = new Hashtable<String, Integer>();
		listeners = new Hashtable<String,ArrayList<DeviceListener>>();
		autodetect = true;
		parseConfig();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#parseConfig()
	 */
	@Override
	public void parseConfig() throws ConfigurationException {
		// Check if we have any USB ports on this platform
		//logger.debug("check if we have USB ports.");
		ProcessOutput c = null;
		try {
			c = PlatformHelper.captureOutputs("lsusb", true);
			BufferedReader b[] = c.getBuffers();
			if(!b[0].readLine().contains("Bus"))
				throw new ConfigurationException("Did not detect USB ports");
			configured = true;
			//logger.debug("Yes we have. USB EndPoint initialised");
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Problem capturing output of lsusb");
			throw new ConfigurationException("Did not detect USB ports", e);
		} finally {
			if(c!=null) c.destroyProcess();
		}
	}
	
	@Override
	protected void internal_stop() {stopAutoDectectThread();}
	
	@Override
	protected void internal_start() throws ConfigurationException {startAutoDectectThread();}
	
	@Override
	public int getConnectedDeviceNum(String ids){
		int nb = 0;
		try {
			for(String s: getConnectedDevices())
				if(s.equals(ids))
					nb++;
		} catch (IOException e) {}		
		return nb;
	}
	
	/**
	 * This method runs the "lsusb" and parse the output to find out the currently connected USB devices 
	 * @return a list of USB IDs (string) of currently connected devices 
	 * @throws IOException if there s a problem running/parsing the lsusb
	 */	
	private static List<String> getConnectedDevices() throws IOException {
		/** 
		 * noDevId contains the USB id "0000:0000" which belongs to the USB host controller
		 * obviously this is not a USB device we re interested in, so we add this ID to noDevId
		 * the elements in noDevId are removed from the output of lsusb every time it is called  
		 */
		ProcessOutput c;
		List<String> plists, noDevId = new ArrayList<String>();
		noDevId.add(NODEVICE_USBID);
		c = PlatformHelper.captureOutputs("lsusb", true);
		BufferedReader b[] = c.getBuffers();
		plists = PlatformHelper.getFieldsFromBuffer(b[0], LSUSBOUTPUT_KEY, 6, " ", false);
		c.destroyProcess();
		plists.removeAll(noDevId);
		return plists;
	}
	
	
	private static synchronized void startAutoDectectThread() {
		if(ep_nb==0) {
			deviceWatcher = new Autodetection("USB");
			if(deviceWatchInterval!=0) {
				//logger.debug("Starting autodetect thread");
				deviceWatcher.start();
			} else if (deviceWatchInterval==0){
				//logger.info("Autodetect interval set to 0 in the endpoint config.");
				//logger.info("Disabling sensor autodetection");
			} else {
				//logger.debug("Sensor autodetection not supported");
			}
		}// else logger.debug("USB device watcher already started - ep_nb:"+ep_nb+" isalive?"+deviceWatcher.isAlive());
		ep_nb++;
	}
	
	private static synchronized void stopAutoDectectThread() {
		if(deviceWatcher.isAlive() && ep_nb==1) {
			deviceWatcher.interrupt();
			try { deviceWatcher.join();}
			catch (InterruptedException e) {}
			deviceWatcher=null;
			//logger.debug("autodetect thread stopped");
		}// else logger.debug("USB device watcher not stopped- ep_nb:"+ep_nb+" isalive?"+deviceWatcher.isAlive());
		ep_nb--;
	}
	
	/**
	 * This inner class runs a thread that automatically detects native adapter additions / removals 
	 * @author gilles
	 *
	 */
	private static class Autodetection implements Runnable{
		Thread t;
		int stop=0;
		
		public Autodetection(String n) {
			t= new Thread(this, "EP_autodetection_thread_"+n);
		}
		
		public void start() {
			stop=0;
			t.start();
		}
		
		public void interrupt() {
			stop=1;
			t.interrupt();
		}
		
		public boolean isAlive(){
			return t.isAlive();
		}

		public void join() throws InterruptedException {
			t.join();
		}
		
		private void notifyListeners(HashSet<String> ids) {
			List<DeviceListener> d = null;

			for(String temp: ids){
				/* finds connected devices*/
				synchronized(listeners) {
					if(listeners.containsKey(temp))
						/* make a copy of the array of device listeners
						 * so we can call each of them without holding the listeners
						 * lock to avoid race conditions
						 */
						d = new ArrayList<DeviceListener>(listeners.get(temp));
				}
				
				if(d!=null)
					for(DeviceListener i: d)
						//logger.debug("Notifying change on device(s) with ID: "+temp+", nb: "+devices.get(temp));
						i.adapterChange(devices.get(temp), temp);

			}
			ids.clear();
		}
		
		/**
		 * Implements the autodetection thread
		 */
		public void run(){
			List<String> previousList, currentList, tempList;
			HashSet<String> changedList = new HashSet<String>();
			
			try {
				previousList = getConnectedDevices();
				for(String temp: previousList){
					devices.put(temp, (devices.get(temp)==null) ? 1 : devices.get(temp)+1);
					changedList.add(temp);
					//logger.debug("device "+temp+" initially connected");
				}
				
				/* notify the listeners of the connected devices */
				notifyListeners(changedList);
				
				while(!Thread.interrupted() && stop==0){
					
					Thread.sleep(deviceWatchInterval);

					/* get the list of connected usb devices */
					tempList = currentList = getConnectedDevices();
					
					/* parse it */
					for(String temp: currentList){
						
						/* finds new plugged-in devices*/
						if(!previousList.contains(temp)) {
							logger.debug("Found newly connected USB device with ID: "+temp);
							devices.put(temp, (devices.get(temp)==null) ? 1 : devices.get(temp)+1);
							changedList.add(temp);
						} else 
							previousList.remove(temp);
					}

					for(String temp: previousList){
						
						/* finds unplugged devices*/
						logger.debug("Found newly disconnected USB device with ID: "+temp);
						devices.put(temp, (devices.get(temp)==null) ? 0 : devices.get(temp)-1);
						changedList.add(temp);
					}
					
					notifyListeners(changedList);
					
					previousList = tempList;
				}
			} catch (InterruptedException e) {}
			catch (IOException e) {
				logger.error("error detecting connected devices");
				e.printStackTrace();
			}
		}
		
	}
}
