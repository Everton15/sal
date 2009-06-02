package jcu.sal.plugins.protocols.v4l2;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jcu.sal.common.Slog;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLArgument;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.ResponseType;
import jcu.sal.common.cml.CMLDescription.SamplingBounds;
import jcu.sal.common.exceptions.AlreadyPresentException;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.common.exceptions.SensorControlException;
import jcu.sal.common.exceptions.SensorIOException;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.components.protocols.AbstractProtocol;
import jcu.sal.components.protocols.DefaultStreamingThread;
import jcu.sal.components.protocols.LocalStreamID;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.components.protocols.StreamingThread;
import jcu.sal.components.protocols.StreamingThreadListener;
import jcu.sal.components.sensors.Sensor;
import jcu.sal.plugins.endpoints.PCIEndPoint;
import jcu.sal.plugins.endpoints.UsbEndPoint;

import org.apache.log4j.Logger;

import au.edu.jcu.v4l4j.Control;
import au.edu.jcu.v4l4j.DeviceInfo;
import au.edu.jcu.v4l4j.FrameGrabber;
import au.edu.jcu.v4l4j.ImageFormat;
import au.edu.jcu.v4l4j.InputInfo;
import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

public class V4L2Protocol extends AbstractProtocol {
	
	private static Logger logger = Logger.getLogger(V4L2Protocol.class);
	static {Slog.setupLogger(logger);}
	
	public final static String PROTOCOL_TYPE = "v4l2";
	public final static String DEVICE_ATTRIBUTE_TAG= "deviceFile";
	public final static String WIDTH_ATTRIBUTE_TAG= "width";
	public final static String HEIGHT_ATTRIBUTE_TAG= "height";
	public final static String CHANNEL_ATTRIBUTE_TAG= "channel";
	public final static String STANDARD_ATTRIBUTE_TAG= "standard";	
	public final static String CONTROL_VALUE_ATTRIBUTE_TAG = "ControlValue";

	
	private VideoDevice vd = null;
	private FrameGrabber fg = null;
	private DeviceInfo di = null;
	private Hashtable<String,Control> ctrls = null;
	

	public V4L2Protocol(ProtocolID i, ProtocolConfiguration c) throws ConfigurationException{
		super(i, PROTOCOL_TYPE , c);
		autoDetectionInterval = -1; //run only once
		supportedEndPointTypes.add(PCIEndPoint.ENDPOINT_TYPE);
		supportedEndPointTypes.add(UsbEndPoint.ENDPOINT_TYPE);
	}

	@Override
	protected String internal_getCMLStoreKey(Sensor s) {
		return s.getNativeAddress();
	}

	@Override
	protected boolean internal_isSensorSupported(Sensor s) {
		if(s.getNativeAddress().equals(CMLDescriptionStore.CCD_KEY)) return true;
		return false;
	}

	@Override
	protected void internal_parseConfig() throws ConfigurationException {
		String dev;
		//Check config directives:
		try {
			dev = getParameter(DEVICE_ATTRIBUTE_TAG);
		} catch (NotFoundException e) {
			logger.error("The device file parameter is missing, cant instanciate framegrabber");
			throw new ConfigurationException();
		}
		
		//create the frame grabber object
		try {
			vd = new VideoDevice(dev);
		} catch (V4L4JException e) {
			logger.error("Couldnt create/initialise VideoDevice object");
			e.printStackTrace();
			throw new ConfigurationException();
		} catch(UnsatisfiedLinkError e) {
			logger.error("Cant load JNI library. Couldnt create/initialise VideoDevice object");
			throw new ConfigurationException();
		} catch (NoClassDefFoundError e){
			//thrown the second time a VideoDevice is instantiated and the JNI lib is not found.
			//I suspect that at the first instantiation attempt, since the JVM cant find the JNI lib,
			//it unloads the VideoDevice class, which is why the second attempt throws NoClassDefFound
			logger.error("Cant load JNI library. Couldnt create VideoDevice object");
			throw new ConfigurationException();
		}
		
		//creates the CML descriptions for all controls
		updateCMLStore();

	}
	
	private void updateCMLStore(){
		cmls = CMLDescriptionStore.getStore();
		
		//add CML for CCD
		createCCDCML();
		
		//get the V4L2 controls
		List<Control> v4l2c = vd.getControlList().getList();
		ctrls = new Hashtable<String, Control>();
		
		for(Control c: v4l2c)
			createCMLs(c, CMLDescriptionStore.CCD_KEY);
		
	}
	
	private void createCCDCML(){
		int i;
		String key, name, mName, desc;
		try {
			di = vd.getDeviceInfo();
		} catch (V4L4JException e1) {
			// if we land here, the video device is most likely used by another
			//app
			logger.error("Unable to create CCD CML as the video device is used"
					+" by another application");
			return;
			
		}
		Map<String,String> values = new Hashtable<String, String>();
		List<CMLArgument> args;
		ResponseType r;
		
		/* 
		 * CCD sensor
		 * */
		key = CMLDescriptionStore.CCD_KEY;
		
		if(vd.supportJPEGConversion()){
			mName = V4L2Protocol.GET_JPEG_FRAME_METHOD;
			name = "GetJPEGFrame";
			desc = "Fetches a single JPEG-encoded frame";
			args = new Vector<CMLArgument>();
			args.add(new CMLArgument(CMLDescriptionStore.WIDTH_VALUE_NAME, ArgumentType.IntegerArgument, "640"));
			args.add(new CMLArgument(CMLDescriptionStore.HEIGHT_VALUE_NAME, ArgumentType.IntegerArgument, "480"));
			
			
			for(InputInfo input: di.getInputs())
				values.put(String.valueOf(input.getIndex()), input.getName()+" - "+(input.getType()==V4L4JConstants.INPUT_TYPE_CAMERA?"Camera":"Tuner"));
			
			args.add(
					new CMLArgument(CMLDescriptionStore.CHANNEL_VALUE_NAME, 
							values, 
							(String) (values.keySet().toArray()[0]))
					);
			values.clear();
			
			values.put(String.valueOf(V4L4JConstants.STANDARD_WEBCAM), "Webcam");
			values.put(String.valueOf(V4L4JConstants.STANDARD_PAL), "PAL");
			values.put(String.valueOf(V4L4JConstants.STANDARD_SECAM), "SECAM");
			values.put(String.valueOf(V4L4JConstants.STANDARD_NTSC), "NTSC");
			
			args.add(
					new CMLArgument(CMLDescriptionStore.STANDARD_VALUE_NAME,
							values,
							String.valueOf(V4L4JConstants.STANDARD_WEBCAM))
					);
			values.clear();		
			
			for(ImageFormat im: di.getFormatList().getJPEGEncodableFormats())
				values.put(String.valueOf(im.getIndex()), im.getName());
			
			args.add(
					new CMLArgument(CMLDescriptionStore.FORMAT_VALUE_NAME,
							values,
							(String) (values.keySet().toArray()[0]))
					);
				
			
			args.add(
					new CMLArgument(CMLDescriptionStore.QUALITY_VALUE_NAME,
							V4L4JConstants.MIN_JPEG_QUALITY,
							V4L4JConstants.MAX_JPEG_QUALITY,
							1,
							80
							)
					);
			r = new ResponseType(CMLConstants.RET_TYPE_BYTE_ARRAY,CMLConstants.CONTENT_TYPE_JPEG);
			try {
				i = cmls.addPrivateCommand(key, mName, name, desc, args, r, new SamplingBounds(50,10*1000,true));
				//generic GetReading
				cmls.addGenericCommand(CMLDescriptionStore.CCD_KEY, CMLDescriptionStore.GENERIC_GETREADING, i);
			} catch (AlreadyPresentException e) {
				//we shouldnt be here
				e.printStackTrace();
			} catch (NotFoundException e) {
				//we shouldnt be here
				e.printStackTrace();
			}
		}

	}
	
	private void createCMLs(Control c, String key){
		String name, ctrlName, desc;
		List<CMLArgument> args = new Vector<CMLArgument>();
		ctrlName = c.getName();
		
		if(c.getType()==V4L4JConstants.CTRL_TYPE_BUTTON){
			//setValue command
			name = "Activate"+ctrlName.replace(" ", "");
			desc = "Activates the button '"+ctrlName+"'";
			addControl(c, key, SET_CONTROL_METHOD, name, desc, null, ResponseType.Void, null);
		} else if (c.getType()==V4L4JConstants.CTRL_TYPE_SLIDER){
			//getValue
			name = "Get"+ctrlName.replace(" ", "");
			desc = "Fetches the value of "+ctrlName;
			addControl(c, key, GET_CONTROL_METHOD, name, desc, null, ResponseType.Integer, new SamplingBounds(100,10*1000,false));
			
			//setValue command
			name = "Set"+ctrlName.replace(" ", "");
			desc = "Sets the value of "+ctrlName;
			args.add(new CMLArgument(CMLDescriptionStore.CONTROL_VALUE_NAME, c.getMinValue(), c.getMaxValue(), c.getStepValue(), c.getDefaultValue()));
			addControl(c, key, SET_CONTROL_METHOD, name, desc, args, ResponseType.Void, null);
		} else if (c.getType()==V4L4JConstants.CTRL_TYPE_SWITCH){
			//getValue
			name = "Get"+ctrlName.replace(" ", "");
			desc = "Fetches the state of "+ctrlName;
			addControl(c, key, GET_CONTROL_METHOD, name, desc, null, ResponseType.Integer, new SamplingBounds(100,10*1000,false));
			
			//setValue command
			name = "Set"+ctrlName.replace(" ", "");
			desc = "Enable/disable  "+ctrlName;
			args.add(new CMLArgument(CMLDescriptionStore.CONTROL_VALUE_NAME, 0, 1, 1, c.getDefaultValue()));
			addControl(c, key, SET_CONTROL_METHOD, name, desc, args, ResponseType.Void, null);
		} else if (c.getType()==V4L4JConstants.CTRL_TYPE_DISCRETE){
			Map<String,String> map = new Hashtable<String,String>();
			List<String> discreteNames = c.getDiscreteValueNames();
			List<Integer> discreteValues = c.getDiscreteValues();
			//getValue
			name = "Get"+ctrlName.replace(" ", "");
			desc = "Fetches the state of "+ctrlName;
			addControl(c, key, GET_DISCRETE_CONTROL_METHOD, name, desc, null, ResponseType.String, new SamplingBounds(100,10*1000,false));
			
			//setValue command
			name = "Set"+ctrlName.replace(" ", "");
			desc = "Set the value of "+ctrlName;
			for(int i=0; i<discreteNames.size();i++)
				map.put(discreteValues.get(i).toString(), discreteNames.get(i));
			args.add(new CMLArgument(CMLDescriptionStore.CONTROL_VALUE_NAME, map,  null));
			addControl(c, key, SET_CONTROL_METHOD, name, desc, args, ResponseType.Void, null);
		} else {
			logger.error("unknown control type '"+c.getType()+"'");
			throw new SALRunTimeException("unknown control type");
		}

	}
	
	private void addControl(Control c, String key, String mName, String name, String shortDesc, List<CMLArgument> args, ResponseType r, SamplingBounds b){
		int cid;
		try {
			cid = cmls.addPrivateCommand(key, mName, name, shortDesc, args, r, b);
		} catch (AlreadyPresentException e) {
			logger.error("we shouldnt be here - trying to insert a duplicate element in CML table");
			e.printStackTrace();
			throw new SALRunTimeException("trying to insert a duplicate element in CML table",e);
		}
		ctrls.put(String.valueOf(cid), c);
	}

	@Override
	protected boolean internal_probeSensor(Sensor s) {
		if(internal_isSensorSupported(s)) {s.enable(); return true;}
		else { s.disconnect(); return false; }
	}

	@Override
	protected void internal_start() throws ConfigurationException {}

	@Override
	protected void internal_stop() {
		vd.releaseControlList();
		vd.release();
	}
	
	@Override
	protected void internal_remove() {
		ctrls.clear();
	}
	
	@Override
	protected List<String> detectConnectedSensors() {
		List <String> v = new Vector<String>();
		v.add(CMLDescriptionStore.CCD_KEY);
		return v; 
	}

	@Override
	protected StreamingThread createStreamingThread(
			Command c, Sensor s, LocalStreamID lid) throws SensorControlException {
		StreamingThread st;
		String mName;
		
		
		try {
			mName = cmls.getMethodName(CMLDescriptionStore.CCD_KEY, c.getCID());
		} catch (NotFoundException e) {
			logger.error("We shouldnt be here - error creating the streaming thread");
			e.printStackTrace();
			throw new SensorControlException("error creating the streaming thread");
		}
		
		
		if(mName.equals(GET_JPEG_FRAME_METHOD)){
			//setup JPEG image stream
			getJPEGFrameGrabber(c, s);
			try {
				st = new V4lStreamingThread(
						getClass().getDeclaredMethod("getFrame", new Class<?>[] {Command.class,Sensor.class}),
						this, s, c, this, lid);
			} catch (Throwable e) {
				logger.error("We shouldnt be here - error creating the streaming thread");
				e.printStackTrace();
				putFrameGrabber();
				throw new SensorControlException("Error creating the streaming thread");
			} 
		} else 
			st = super.createStreamingThread(c, s, lid);

		
		return st;
	}
	
	/*
	 * COMMAND HANDLING METHODS
	 */
	
	public static final String GET_CONTROL_METHOD = "getControl";
	public byte[] getControl(Command c, Sensor s) throws SensorControlException{
		Control ctrl = ctrls.get(String.valueOf(c.getCID()));
		if(ctrl!=null) {
			try {
				return String.valueOf(ctrl.getValue()).getBytes();
			} catch (V4L4JException e) {
				logger.error("Could NOT read the value for control "+ctrl.getName());
				throw new SensorIOException("Error reading the contol value (CID:'"+c.getCID()+"'): "+e.getMessage());
			}			
		} else {
			logger.error("Could NOT find the control matching command ID "+c.getCID());
			throw new SensorIOException("Error finding the control to read from (CID:'"+c.getCID()+"')");
		}
	}
	
	public static final String GET_DISCRETE_CONTROL_METHOD = "getDiscreteControl";
	public byte[] getDiscreteControl(Command c, Sensor s) throws SensorControlException{
		Control ctrl = ctrls.get(String.valueOf(c.getCID()));
		if(ctrl!=null) {
			try {
				return ctrl.getDiscreteValueNames().get(ctrl.getDiscreteValues().indexOf(ctrl.getValue())).getBytes();
			} catch (V4L4JException e) {
				logger.error("Could NOT read the value for control "+ctrl.getName());
				throw new SensorIOException("Error reading the contol value (CID:'"+c.getCID()+"'): "+e.getMessage());
			}			
		} else {
			logger.error("Could NOT find the control matching command ID "+c.getCID());
			throw new SensorIOException("Error finding the control to read from (CID:'"+c.getCID()+"')");
		}
	}

	public static final String SET_CONTROL_METHOD = "setControl";
	public byte[] setControl(Command c, Sensor s) throws SensorControlException{
		Control ctrl = ctrls.get(String.valueOf(c.getCID()));
		if(ctrl!=null) {
			try {
				ctrl.setValue(Integer.parseInt(c.getValue(CMLDescriptionStore.CONTROL_VALUE_NAME)));
				return null;
			} catch (V4L4JException e) {
				logger.error("Could NOT set the value for control "+ctrl.getName());
				throw new SensorIOException("Error setting the value on the control (CID:'"+c.getCID()+"'): "+e.getMessage());
			}			
		} else {
			logger.error("Could NOT find the control matching command ID "+c.getCID());
			throw new SensorIOException("Error finding the control to set value on (CID:'"+c.getCID()+"')");
		}
	}
	
	public void getJPEGFrameGrabber(Command c, Sensor s) throws SensorControlException{
		try {
			ImageFormat imf = di.getFormatList().getJPEGEncodableFormat(c.getIntValue(CMLDescriptionStore.FORMAT_VALUE_NAME));
			fg = vd.getJPEGFrameGrabber(c.getIntValue(CMLDescriptionStore.WIDTH_VALUE_NAME),
					c.getIntValue(CMLDescriptionStore.HEIGHT_VALUE_NAME), c.getIntValue(CMLDescriptionStore.CHANNEL_VALUE_NAME),
					c.getIntValue(CMLDescriptionStore.STANDARD_VALUE_NAME), c.getIntValue(CMLDescriptionStore.QUALITY_VALUE_NAME),
					imf);
		} catch (V4L4JException e) {
			logger.error("Error getting JPEG frame grabber");
			throw new SensorIOException("Error getting JPEG frame grabber: "+e.getMessage());
		}
		
		try {
			fg.startCapture();
		} catch (V4L4JException e) {
			logger.error("Error starting capture");
			vd.releaseFrameGrabber();
			throw new SensorIOException("Error starting capture: "+e.getMessage());
		}
	}
	
	public byte[] getFrame(Command c, Sensor s) throws SensorControlException{
		
		byte[] b;
		ByteBuffer bb;
		
		try {
			bb = fg.getFrame();
			b = new byte[bb.limit()];
			bb.get(b);
		} catch (V4L4JException e1) {
			logger.error("Error while capturing frame");
			throw new SensorIOException("Error while capturing frame: "+e1.getMessage());
		}
		return b;
	}
	
	public void putFrameGrabber(){
		fg.stopCapture();
		vd.releaseFrameGrabber();
	}
	
	public static final String GET_JPEG_FRAME_METHOD = "getJPEGFrame";
	public byte[] getJPEGFrame(Command c, Sensor s) throws SensorControlException{
		byte[] b;
		getJPEGFrameGrabber(c, s);
		try{b = getFrame(c,s);}
		finally{putFrameGrabber();}
		return b;
	}

	/**
	 * This class creates a streaming thread for v4l
	 * @author gilles
	 *
	 */
	private class V4lStreamingThread implements StreamingThread, StreamingThreadListener{
		private DefaultStreamingThread thread;
		private StreamingThreadListener listener;
		
		/**
		 * This methods build a new {@link V4lStreamingThread}. The FrameGrabber <code>fg</code> must be setup before
		 * calling this method.
		 * @param m the method to call to stream images
		 * @param o the object where this method belongs
		 * @param s the Sensor
		 * @param c the command
		 * @param l the listener to be notified when this thread exits
		 * @param id the LocalStreamId
		 */
		public V4lStreamingThread(Method m, AbstractProtocol o, Sensor s, Command c, StreamingThreadListener l, LocalStreamID id){
			listener = l; 
			thread = new DefaultStreamingThread(m, o, s, c, this, id);
		}
		
		@Override
		public synchronized void stop(){
			thread.stop();
		}

		@Override
		public void threadExited(LocalStreamID lid) {
			putFrameGrabber();
			listener.threadExited(lid);
		}

		@Override
		public synchronized void start() {
			thread.start();
		}

		@Override
		public void update(int i) {
			thread.update(i);
		}

		@Override
		public LocalStreamID getLocalStreamID() {
			return thread.getLocalStreamID();
		}
	}
}
