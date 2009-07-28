/**
 * 
 */
package jcu.sal.plugins.protocols.owfs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import jcu.sal.common.Slog;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SensorControlException;
import jcu.sal.common.exceptions.SensorDisconnectedException;
import jcu.sal.common.exceptions.UnsupportedCommandException;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.components.protocols.AbstractProtocol;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.components.sensors.Sensor;
import jcu.sal.plugins.endpoints.UsbEndPoint;
import jcu.sal.utils.PlatformHelper;
import jcu.sal.utils.PlatformHelper.ProcessOutput;

import org.apache.log4j.Logger;

/**
 * @author gilles
 *
 */
public class OWFSProtocol extends AbstractProtocol{

	private static Logger logger = Logger.getLogger(OWFSProtocol.class);
	static {Slog.setupLogger(logger);}
	private int adapterNb=0;
	private int maxAdaptersSeen=0;

	public final static String OWFSPROTOCOL_TYPE = "owfs";
	public final static String OWFSLOCATIONATTRIBUTE_TAG = "Location";
	public final static String OWFSMOUNTPOINTATTRIBUTE_TAG = "MountPoint";
	public final static int OWFSSTART_MAX_ATTEMPTS = 2;
	public final static String DS2490_USBID= "04fa:2490";
	
	public static String DS_10_FAMILY = "10.";
	public static String DS_26_FAMILY = "26.";
	
	private String w1MountPoint;
	
	/**
	 * Construct the OSDataProtocol object.
	 * @throws ConfigurationException 
	 */
	public OWFSProtocol(ProtocolID i, ProtocolConfiguration c) throws ConfigurationException{
		super(i,OWFSPROTOCOL_TYPE ,c);
		epIds = new String[]{DS2490_USBID};
		autoDetectionInterval = 100;
		multipleInstances = false;
		supportedEndPointTypes.add(UsbEndPoint.ENDPOINT_TYPE);
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.AbstractComponent#parseConfig()
	 */
	@Override
	protected void internal_parseConfig() throws ConfigurationException {
		ProcessOutput c;

		try {
			w1MountPoint = getParameter(OWFSMOUNTPOINTATTRIBUTE_TAG);
			if(w1MountPoint.length()==0) throw new NotFoundException("Empty mount point directive...");
			if(!PlatformHelper.isDir(w1MountPoint)) {
				//try creating it
				logger.error("OWFS Mount point doesnt exist, try creating it");
				if (! new File(w1MountPoint).mkdirs()) throw new NotFoundException("Cant create mount point");
				logger.error("ok");
			} else if(!PlatformHelper.isDirReadWrite(w1MountPoint)) {
				//it is unlikely we have rights to change the permissions
				logger.error("OWFS Mount point not readable/writeable");
				throw new NotFoundException("OWFS Mount point not readable/writeable");
			}

			//Next, we check that OWFS is installed in the given directory
			//and try to get the OWFS version 
			//logger.debug("Detecting OWFS version");
			c = PlatformHelper.captureOutputs(getParameter(OWFSLOCATIONATTRIBUTE_TAG) + " --version", true);
			//BufferedReader[] b = c.getBuffers();
			//while((temp = b[0].readLine()) != null) logger.debug(temp);
			//while((temp = b[1].readLine()) != null) logger.debug(temp);
			c.destroyProcess();
			//logger.debug("OWFS protocol configured");
			
		} catch (IOException e) {
			logger.error("Could NOT run/read owfs");
			e.printStackTrace();
			throw new ConfigurationException("Could NOT run/read owfs",e);
		} catch (NotFoundException e) {
			logger.error("incorrect OWFS configuration directives...");
			e.printStackTrace();
			throw new ConfigurationException("Could not setup OWFS protocol",e);
		}
		cmls = CMLDescriptionStore.getStore();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.Protocol#internal_remove()
	 */
	protected void internal_stop() {
		stopOWFS();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.Protocol#internal_start()
	 */
	protected void internal_start() {
		adapterNb=0;
		maxAdaptersSeen=0;

		
		/*
		 * make sure no copies of owfs are currently running !
		 * owfs is only started when DS2490 are reported by the endpoint !	 
		 */		
		stopOWFS();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.Protocol#internal_stop()
	 */
	protected void internal_remove() {}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_isSensorSupported(jcu.sal.components.sensors.Sensor)
	 */
	@Override
	protected boolean internal_isSensorSupported(Sensor sensor) {
		//TODO check the sensor family and make sure it is supported
		//When supporting a new family, modify getReading() to support it
		if(getFamily(sensor).equals("10.") || getFamily(sensor).equals("26."))
			return true;
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_probeSensor(jcu.sal.components.sensors.Sensor)
	 */
	@Override
	protected boolean internal_probeSensor(Sensor s) {
		try {
			//logger.debug("Probing sensor " + s.getNativeAddress());
			if(PlatformHelper.isDirReadable(w1MountPoint+"/"+s.getNativeAddress())) {
				s.enable();
				//logger.debug("Sensor " + s.getNativeAddress()+ " present");
				return true;
			}
		} catch (Exception e) {
			//logger.error("couldnt probe sensor "+s.toString());
			//logger.error("Raised exception: "+e.getMessage());
		}
		s.disconnect();
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_getCMLStoreKey(jcu.sal.components.sensors.Sensor)
	 */
	@Override
	protected String internal_getCMLStoreKey(Sensor s){
		return getFamily(s);  
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.EndPoints.DeviceListener#deviceChange(int)
	 */
	@Override
	public void adapterChange(int n, String id) {
		synchronized(removed){
			if(!removed.get()){
				if(n>adapterNb) {
					//logger.debug("new OWFS adapters have been plugged in, adapterNB:"+adapterNb+" maxSeen:"+maxAdaptersSeen+" currently plugged:"+n);
					if(n>maxAdaptersSeen){
						//logger.debug("Restarting OWFS to detect new adapters");
						try {
							stopAutodetectThread();
							synchronized(sensors) {
								Enumeration<Sensor> es = sensors.elements();
								while(es.hasMoreElements()) {
									Sensor s = es.nextElement();
									synchronized(s) {
										logger.debug("Disconnecting "+s.toString());
										s.disconnect();
									}
								}
							}
							stopOWFS();
							try { Thread.sleep(100); } catch (InterruptedException e) {}
							startOWFS();
							startAutodetectThread();
							//logger.debug("Done restarting OWFS");
							maxAdaptersSeen=n;
						} catch (ConfigurationException e) {
							logger.error("Unable to run owfs: "+e.getClass()+" - "+e.getMessage());
							e.printStackTrace();
						} 
					} else {
						//logger.debug("no need to restart OWFS to detect new adapter, max>adapternb");
					}
					adapterNb=n;
				} else if (n<adapterNb) {
					//logger.debug("a new OWFS adapter has been unplugged, adapterNB:"+adapterNb+" maxSeen:"+maxAdaptersSeen+" currently plugged:"+n);
					//adapterNb=n;
				} 
				//may happen right after instanciation if endpoint reports device plugged in atfer Protocol.start() has already
				//detected it and called this method
				//else logger.error("Weird condition, recevied a device change event, but the reported device count("+n+") is the same as ours("+adapterNb+")");
			}
		}
	}
	
	
	private void stopOWFS() {PlatformHelper.killProcesses("owfs");}
	
	
	private void startOWFS() throws ConfigurationException{
		StringBuffer err = new StringBuffer();
		String s;
		ProcessOutput c;
		int attempt=0;
		boolean started=false;
		
		try {
			//try loading fuse
			PlatformHelper.loadModule("fuse");
			//try unloading ds2490 and wire
			PlatformHelper.unloadModules(new String [] {"ds2490", "wire"});
			
			//concurrent instances of owfs cant coexist 
			//Check whether instances of owfs are running
			if(!PlatformHelper.getPid("owfs").isEmpty()){
				logger.error("An instance of owfs seems to be running");
				logger.error("Try killing it");
				PlatformHelper.killProcesses("owfs");
				
				//sleep for a while
				try { Thread.sleep(1000);} catch (InterruptedException e) {}
				
				//and check again
				if(!PlatformHelper.getPid("owfs").isEmpty()){
					logger.error("CAnt kill it...");
					throw new ConfigurationException("OWFS already running");
				}
			}

			while(++attempt<=OWFSSTART_MAX_ATTEMPTS && !started) {
				c = PlatformHelper.captureOutputs(
						getParameter(
								OWFSProtocol.OWFSLOCATIONATTRIBUTE_TAG)+
								" -uall --timeout_directory 1 --timeout_presence 1 --timeout_volatile 1 "+w1MountPoint,
						false);
				BufferedReader r[] = c.getBuffers(); 
				try {Thread.sleep(100);} catch (InterruptedException e) {}
				//check stdout & stderr
				while ((s=r[0].readLine())!=null)
					err.append("out: "+s);
				while ((s=r[1].readLine())!=null)
					err.append("err: "+s);
				if(err.length()!=0)
				logger.error("OWFS said:\n"+err);
				err.delete(0, err.length());
				
				//Check that it actually started ...
				if(PlatformHelper.getPid("owfs").isEmpty()){
					logger.error("Starting OWFS command failed.");
					logger.error("Killing any instances of owfs");
					PlatformHelper.killProcesses("owfs");
					started=false;
					c.destroyProcess();
					continue;
				}
				
				started=true;
			}
			
			if(!started) {
				logger.error("Coudlnt start the OWFS process");
				throw new ConfigurationException("Coudlnt start the OWFS process");
			}
			
		} catch (IOException e) {
			logger.error("Coudlnt run the OWFS process");
			throw new ConfigurationException("Coudlnt run the OWFS process", e);
		} catch (NotFoundException e) {
			logger.error("Cant find the 1w mount point / OWFS bin location");
			throw new ConfigurationException("Cant find the 1w mount point / OWFS bin location", e);
		}
	}
	
	
	/**
	 * this method should be overriden by protocols which provide sensor autodetection
	 * and return a List of native address (strings) of currently connected/visible sensors 
	 */
	@Override
	protected List<String> detectConnectedSensors() {
		List<String> v = new Vector<String>();
		File dir = new File(w1MountPoint);
		String[] info = dir.list();
		if(info!=null) {
		    for (int i = 0; i < info.length; i++) {
		      if (info[i].indexOf(".") != 2) { // name doesn't match
		        continue;
		      }
		      try { Integer.parseInt(info[i].substring(0,2)); } catch (NumberFormatException e) { continue; } 

		      if(!info[i].substring(0, 2).equals("81"))
			      v.add(info[i]);
		    }
		}
		return v;
	}
	
	private String getFamily(Sensor s){
		return s.getNativeAddress().substring(0, 3);
	}
	
	
	/*
	 * Command handling methods
	 */
	public static String GET_READING_METHOD = "getReading";
	public byte[] getReading(Command c, Sensor s) throws SensorControlException{
		if(getFamily(s).equals("10.")) {
			//temperature sensor, read from temperature file
			return getTemperature(c, s);
		} else if(getFamily(s).equals("26.")) {
			//humidity sensor, read from humidityfile
			return getHumidity(c, s);
		}
		logger.error("1-wire sensor family not yet supported");
		throw new UnsupportedCommandException("1-wire Family not supported yet");
	}
	
	public static String GET_TEMPERATURE_METHOD = "getTemperature";
	public byte[] getTemperature(Command c, Sensor s) throws SensorControlException{
		if(getFamily(s).equals("10.") || getFamily(s).equals("26.")) {
			//temperature sensor, read from temperature file
			return getRawReading(s.getNativeAddress()+ "/" + "temperature");
		}
		logger.error("1-wire sensor family doesnot support this command");
		throw new UnsupportedCommandException("sensor doesnt support this command");
	}
	
	public static String GET_HUMIDITY_METHOD = "getHumidity";
	public byte[] getHumidity(Command c, Sensor s) throws SensorControlException{
		if(getFamily(s).equals("26.")) {
			//Humidity sensor, read from humidityfile
			return getRawReading(s.getNativeAddress()+ "/" + "humidity");
		}
		logger.error("1-wire sensor family doesnot support this command");
		throw new UnsupportedCommandException("sensor doesnt support this command");
	}
	
	public static String GET_HIH400_METHOD = "getHumidityHIH4000";
	public byte[] getHumidityHIH4000(Command c, Sensor s) throws SensorControlException{
		if(getFamily(s).equals("26.")) {
			//Humidity sensor, read from humidityfile
			return getRawReading(s.getNativeAddress()+ "/" + "HIH4000/humidity");
		}
		logger.error("1-wire sensor family doesnot support this command");
		throw new UnsupportedCommandException("sensor doesnt support this command");
	}
	
	public static String GET_HTM1735_METHOD = "getHumidityHTM1735";
	public byte[] getHumidityHTM1735(Command c, Sensor s) throws SensorControlException{
		if(getFamily(s).equals("26.")) {
			//Humidity sensor, read from humidityfile
			return getRawReading(s.getNativeAddress()+ "/" + "HTM1735/humidity");
		}
		logger.error("1-wire sensor family doesnot support this command");
		throw new UnsupportedCommandException("sensor doesnt support this command");
	}
	
	public static String GET_VAD_METHOD = "getVAD";
	public byte[] getVAD(Command c, Sensor s) throws SensorControlException{
		if(getFamily(s).equals("26.")) {
			//Humidity sensor, read from humidityfile
			return getRawReading(s.getNativeAddress()+ "/" + "VAD");
		}
		logger.error("1-wire sensor family doesnot support this command");
		throw new UnsupportedCommandException("sensor doesnt support this command");
	}
	
	public static String GET_VDD_METHOD = "getVDD";
	public byte[] getVDD(Command c, Sensor s) throws SensorControlException{
		if(getFamily(s).equals("26.")) {
			//Humidity sensor, read from humidityfile
			return getRawReading(s.getNativeAddress()+ "/" + "VDD");
		}
		logger.error("1-wire sensor family doesnot support this command");
		throw new UnsupportedCommandException("sensor doesnt support this command");
	}
	
	public static String GET_VIS_METHOD = "getVIS";
	public byte[] getVIS(Command c, Sensor s) throws SensorControlException{
		if(getFamily(s).equals("26.")) {
			//Humidity sensor, read from humidity file
			return getRawReading(s.getNativeAddress()+ "/" + "vis");
		}
		logger.error("1-wire sensor family doesnot support this command");
		throw new UnsupportedCommandException("sensor doesnt support this command");
	}
	
	/**
	 * Gets a raw reading from an OWFS file
	 * @param f the file within OWFSMOUNTPOINTATTRIBUTE which should be read
	 * @return the reading
	 * @throws SensorControlException if something goes wrong
	 */
	private byte[] getRawReading(String f) throws SensorControlException {
		try {
			return PlatformHelper.readFromFile(w1MountPoint+"/"+f).getBytes();
		} catch (IOException e) {
			logger.error("Error reading from "+f);
			throw new SensorDisconnectedException("Error reading from "+f,e);
		} 
	}
}
