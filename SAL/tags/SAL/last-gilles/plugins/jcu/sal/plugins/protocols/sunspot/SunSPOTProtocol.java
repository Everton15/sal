package jcu.sal.plugins.protocols.sunspot;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import jcu.sal.common.Response;
import jcu.sal.common.Slog;
import jcu.sal.common.StreamID;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.exceptions.ClosedStreamException;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.common.exceptions.SensorControlException;
import jcu.sal.common.exceptions.SensorDisconnectedException;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.components.protocols.AbstractProtocol;
import jcu.sal.components.protocols.LocalStreamID;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.components.protocols.StreamingThread;
import jcu.sal.components.sensors.Sensor;
import jcu.sal.plugins.endpoints.SerialEndPoint;

import org.apache.log4j.Logger;

public class SunSPOTProtocol extends AbstractProtocol {
	private static Logger logger = Logger.getLogger(SunSPOTProtocol.class);
	static {Slog.setupLogger(logger);}

	public final static String PROTOCOL_TYPE = "SALSpot";
	public final static String DEVICE_ATTRIBUTE_TAG= "deviceFile";

	private final static String DELIM = ":";

	public SunSPOTProtocol(ProtocolID i, ProtocolConfiguration p)
	throws ConfigurationException {
		super(i, PROTOCOL_TYPE, p);
		multipleInstances = false;
		autoDetectionInterval = 1000;
		supportedEndPointTypes.add(SerialEndPoint.ENDPOINT_TYPE);
	}

	@Override
	protected String internal_getCMLStoreKey(Sensor s) {
		return toSensorType(s.getNativeAddress());
	}

	@Override
	protected boolean internal_isSensorSupported(Sensor s) {
		if(BaseStation.getSpot(toSpotAddress(s.getNativeAddress()))!=null)
			return true;
		else
			return false;
	}

	@Override
	protected void internal_parseConfig() throws ConfigurationException {
		String dev;
		autoDetectionInterval = 1000;
		try {
			dev = ep.getParameter(SerialEndPoint.PORTDEVICEATTRIBUTE_TAG);
		} catch (NotFoundException e) {
			logger.error("The Serial endpoint is missing the serial device file");
			throw new ConfigurationException("not serial device file");
		}
		System.setProperty("SERIAL_PORT", dev);
		cmls = CMLDescriptionStore.getStore();
	}

	@Override
	protected boolean internal_probeSensor(Sensor s) {
		if(internal_isSensorSupported(s)){
			s.enable();
			return true;
		} else {
			s.disconnect();
			return false;
		}
	}

	@Override
	protected void internal_remove() {}

	@Override
	protected void internal_start() {
		BaseStation.start();
	}

	@Override
	protected void internal_stop() {
		BaseStation.stop();
	}

	@Override
	protected List<String> detectConnectedSensors() {
		Vector<String> v = new Vector<String>();
		for(SALSpot s: BaseStation.getSpots())
			for(String t: SensorConstants.getSensorTypeList())
				v.add(toSensorNativeAddress(s.getAddress(), t));

		return v;
	}

	/**
	 * This method returns a SALSpot sensor native address
	 * from the SALSpot's address, sensor and command
	 * @param a the SAL spot address
	 * @param t the sensor type
	 * @return the sensor native address
	 */
	private static String toSensorNativeAddress(String a, String t){
		return a+DELIM+t;
	}

	/**
	 * This method returns the SAL spot address from a SAL sensor native address
	 * @param nativeAddress the SAL sensor native address
	 * @return the SAL spot address
	 */
	private static String toSpotAddress(String nativeAddress){
		return nativeAddress.substring(
				0,
				nativeAddress.indexOf(DELIM)
		);
	}

	/**
	 * This method returns the sensor type from a SAL sensor native address
	 * @param nativeAddress the SAL sensor native address
	 * @return the sensor type
	 */
	private static String toSensorType(String nativeAddress){
		return nativeAddress.substring(
				nativeAddress.indexOf(DELIM)+1
		);
	}
	
	@Override
	protected StreamingThread createStreamingThread(
			Command c, Sensor s, LocalStreamID lid)
	throws SensorControlException {

		return new SALSpotThread(s,c,lid,this);
	}

	/*
	 * 
	 * S E N S O R   C O M M A N D S
	 * 
	 */
	public static final String SEND_ACCEL_TOTAL_METHOD="sendAccelTotal";
	public byte[] sendAccelTotal(Command c, Sensor s) 
	throws SensorControlException{
		return sendCommand(c, s, SensorConstants.ACCEL_GET_TOTAL);
	}

	public static final String SEND_ACCEL_X_METHOD="sendAccelX";
	public byte[] sendAccelX(Command c, Sensor s) 
	throws SensorControlException{
		return sendCommand(c, s, SensorConstants.ACCEL_GET_X);
	}
	
	public static final String SEND_ACCEL_Y_METHOD="sendAccelY";
	public byte[] sendAccelY(Command c, Sensor s) 
	throws SensorControlException{
		return sendCommand(c, s, SensorConstants.ACCEL_GET_Y);
	}
	
	public static final String SEND_ACCEL_Z_METHOD="sendAccelZ";
	public byte[] sendAccelZ(Command c, Sensor s) 
	throws SensorControlException{
		return sendCommand(c, s, SensorConstants.ACCEL_GET_Z);
	}
	
	public static final String SEND_TEMP_C_METHOD="sendTempC";
	public byte[] sendTempC(Command c, Sensor s) 
	throws SensorControlException{
		return sendCommand(c, s, SensorConstants.TEMP_GET_C);
	}
	
	public static final String SEND_LIGHT_LUX_METHOD="sendLightLux";
	public byte[] sendLightLux(Command c, Sensor s) 
	throws SensorControlException{
		return sendCommand(c, s, SensorConstants.LIGHT_GET_LUX);
	}
	
	private byte[] sendCommand(Command c, Sensor s, String cmd) 
	throws SensorControlException{
		SALSpot spot = BaseStation.getSpot(toSpotAddress(s.getNativeAddress()));
		if(spot!=null){
			ProxyStreamDataHandler p = new ProxyStreamDataHandler();
			//tell the spot to start streaming
			try {
				spot.sendDoCommand(
						toSensorType(s.getNativeAddress()),
						cmd,
						-1,
						p
				);
				return p.getData().getData().getBytes();
			} catch (StreamException e) {
				//stream already started...
				throw new SensorControlException("error: "+e.getMessage());
			} catch (IOException e) {
				//the spot disappeared
			}
		} 
		//if we re here there s been some error
		s.disconnect();
		throw new SensorDisconnectedException("the sensor has "
				+"been disconnected");
	}

	public static class ProxyStreamDataHandler implements StreamDataHandler{
		private StreamData data;
		public ProxyStreamDataHandler(){
			data = null;
		}

		@Override
		public void handle(StreamData d) throws IOException {
			synchronized (this){
				data = d;
				notify();
			}
		}

		public StreamData getData(){
			synchronized(this){
				if(data==null)
					try {
						wait();
					} catch (InterruptedException e) {}

					return data;
			}
		}

	}




	public class SALSpotThread 
	implements StreamingThread, StreamDataHandler {
		private Sensor sensor;
		private Command cmd;
		private LocalStreamID lid;
		private StreamID sid;
		private SALSpot spot;
		private AbstractProtocol listener;

		public SALSpotThread(Sensor s, Command c, LocalStreamID l,
				AbstractProtocol lt){
			sensor = s;
			cmd = c;
			lid = l;
			listener = lt;
			sid = new StreamID(l.getSID(),l.getCID(),l.getPID());
		}

		@Override
		public LocalStreamID getLocalStreamID() {
			return lid;
		}

		@Override
		public synchronized void start() {
			Response r;
			if(spot == null){
				spot = BaseStation.getSpot(
						toSpotAddress(sensor.getNativeAddress())
				);
				if(spot!=null){
					//tell the spot to start streaming
					try {
						spot.sendDoCommand(
								toSensorType(sensor.getNativeAddress()),
								toCommand(sensor, cmd),
								cmd.getInterval(),
								this
						);
						return;
					} catch (StreamException e) {
						//stream already started...
						r = new Response(sid, 
								new SensorControlException(
										"stream already started"
								));
					} catch (IOException e) {
						//the spot disappeared
						r = new Response(sid, 
								new SensorControlException(
										"the sensor is disconnected"
								));
						sensor.disconnect();
					}
				} else {
					r = new Response(sid, 
							new SensorControlException(
									"the sensor is disconnected"
							));
					sensor.disconnect();
				}
				//if we re here, there s been some error
				try {
					cmd.getStreamCallBack().collect(r);
				} catch (IOException e) {
					//error delivering response
				}
				listener.threadExited(lid);

			} else 
				throw new SALRunTimeException("This thread has already "
						+"been started");

		}

		@Override
		public synchronized void stop() {
			if(spot!=null){
				try {
					spot.sendStopCommand(
							toSensorType(sensor.getNativeAddress()),
							toCommand(sensor,cmd)
					);
					sensor.stopStream();
					listener.threadExited(lid);
					return ;
				} catch (IOException e) {
					sensor.disconnect();
				}

				listener.threadExited(lid);
				return;
			}

			throw new SALRunTimeException("This thread has already"
					+" been stopped");
		}

		@Override
		public void update(int i) {
			// TODO Not supported by SALSpots
		}

		@Override
		public void handle(StreamData d) throws IOException {
			Response r;
			try {
				r = new Response(d.getData().getBytes(), sid);
			} catch (StreamClosedException e) {
				r = new Response(sid, new ClosedStreamException());
				sensor.stopStream();
			} catch (StreamException e) {
				r = new Response(sid, new SensorControlException(e.getMessage()));
				sensor.stopStream();
			}

			try {
				cmd.getStreamCallBack().collect(r);
			} catch (Throwable t){
				logger.error("error collecting response:\n"+t.getMessage());
				sensor.stopStream();
				throw new IOException(t.getMessage());
			}
		}
		
		/**
		 * This method returns the sensor type from a SAL sensor native address
		 * @param nativeAddress the SAL sensor native address
		 * @return the sensor type
		 */
		private String toCommand(Sensor s, Command c){
			String mName;
			try {
				mName = cmls.getMethodName(internal_getCMLStoreKey(s), c.getCID());
			} catch (NotFoundException e) {
				logger.error("We shouldnt be here - error creating the streaming thread");
				e.printStackTrace();
				throw new SALRunTimeException("error looking up the method name");
			}
			if(mName.equals(SEND_ACCEL_TOTAL_METHOD))
				return SensorConstants.ACCEL_GET_TOTAL;
			else if(mName.equals(SEND_ACCEL_X_METHOD))
				return SensorConstants.ACCEL_GET_X;
			else if(mName.equals(SEND_ACCEL_Y_METHOD))
				return SensorConstants.ACCEL_GET_Y;
			else if(mName.equals(SEND_ACCEL_Z_METHOD))
				return SensorConstants.ACCEL_GET_Z;
			else if(mName.equals(SEND_LIGHT_LUX_METHOD))
				return SensorConstants.LIGHT_GET_LUX;
			else
				return SensorConstants.TEMP_GET_C;
		}

	}
}
