package jcu.sal.plugins.protocols.v4l2;


import java.util.List;
import java.util.Map;
import java.util.Vector;

import jcu.sal.common.Parameters;
import jcu.sal.common.Slog;
import jcu.sal.common.Parameters.Parameter;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.pcml.EndPointConfiguration;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.components.Identifier;
import jcu.sal.plugins.config.hal.AbstractHalClient;

import org.apache.log4j.Logger;

import au.edu.jcu.haldbus.match.AlwaysMatch;
import au.edu.jcu.haldbus.match.GenericMatch;
import au.edu.jcu.haldbus.match.NextMatch;
import au.edu.jcu.haldbus.match.VectorMatch;


public class HalClient extends AbstractHalClient {
	private static Logger logger = Logger.getLogger(HalClient.class);
	static {Slog.setupLogger(logger);}
	
	public HalClient(){
		super("V4L HAL Filter");
		
		addMatch("1-capability", new VectorMatch<String>("info.capabilities", "video4linux"));
		addMatch("2-category", new GenericMatch<String>("info.category", "video4linux"));
		addMatch("3-category.capture", new VectorMatch<String>("info.capabilities", "video4linux.video_capture"));
		addMatch("4-video4linux.device", new GenericMatch<String>("video4linux.device", "video", true,true));
		addMatch("5-deviceFile", new AlwaysMatch("linux.device_file"));
		addMatch("7-linux.subsystem", new NextMatch("@info.parent", new AlwaysMatch("linux.subsystem")));
		//addMatch("8-info.product", new NextMatch("@info.parent", new AlwaysMatch("info.product")));v
		addMatch("8-info.product", new AlwaysMatch("info.product"));
		//the following has been removed: linux UVC creates the V4L UDI as a child of the USB video ifce UDI, which
		//itself is a child of the USB device IDU (where info.vendor is located)
		//whereas other drivers (pwc, bttv, ...) create the V4L UDI as a child of the USB UDI.
		//so the following works with everything except linux UVC
		//addMatch("9-info.vendor", new NextMatch("@info.parent", new AlwaysMatch("info.vendor")));
		
	}

	@Override
	public void deviceAdded(Map<String,String> l) {
		//logger.debug("Found "+l.get("8-info.product")+" - "+l.get("9-info.vendor")+ " on "+l.get("5-deviceFile"));
		logger.debug("Found "+l.get("8-info.product")+"  on "+l.get("5-deviceFile"));
		ProtocolConfiguration pc;
		int width=640, height=480;

		try {
			//check if a running protocol already uses our device file (can happen during the initial run if for instance a protocol is 
			//instanciated with its config taken from the platform config file, and the device is detected again during the initial run)
			if(findRunningProtocolNameFromConfig(V4L2Protocol.PROTOCOL_TYPE, V4L2Protocol.DEVICE_ATTRIBUTE_TAG, l.get("5-deviceFile")).size()!=0){
				logger.debug("An existing protocol already uses "+l.get("5-deviceFile"));
				return;
			}
			
			//the device is available. check if there is an existing config for it in the  PlatformConfig file
			pc = findProtocolConfigFromFile(V4L2Protocol.DEVICE_ATTRIBUTE_TAG, l.get("5-deviceFile"));
			//logger.debug("Found config for "+l.get("5-deviceFile")+" in platformConfig file - reusing it");
		} catch (NotFoundException e) {
			//if we re here, there is no pre exiting config for this device file, so we create a generic one
			//logger.debug("No existing configuration for V4L protocol with device "+l.get("5-deviceFile"));
			
			//QUIRKS:
			//if pci capture card, limit the width and height, otherwise, bttv 
			//returns a green or blue image is the resolution is too high...
			//if usb webcam, limit the width and height to 640x480 because at higher resolution,
			//the frame rate decreases
			Vector<Parameter> v = new Vector<Parameter>();
			v.add(new Parameter(V4L2Protocol.DEVICE_ATTRIBUTE_TAG, l.get("5-deviceFile")));
			v.add(new Parameter(V4L2Protocol.CHANNEL_ATTRIBUTE_TAG, "0"));
			v.add(new Parameter(V4L2Protocol.STANDARD_ATTRIBUTE_TAG, "0"));
			v.add(new Parameter(V4L2Protocol.WIDTH_ATTRIBUTE_TAG, String.valueOf(width)));
			v.add(new Parameter(V4L2Protocol.HEIGHT_ATTRIBUTE_TAG, String.valueOf(height)));
			Parameters params = new Parameters(v);
			
			pc = new ProtocolConfiguration("v4l-"+l.get("5-deviceFile"),
											V4L2Protocol.PROTOCOL_TYPE,
											params,
											new EndPointConfiguration(
													l.get("7-linux.subsystem")+"-"+l.get("5-deviceFile"),
													l.get("7-linux.subsystem")
													)
											);
		}
		try {
			createProtocol(pc);
		} catch (ConfigurationException e) {
			logger.error("Instancation failed");
		}
	}
	
	@Override
	public void deviceRemoved(Map<String, String> l) {			
		List<Identifier> ids = findRunningProtocolNameFromConfig(V4L2Protocol.PROTOCOL_TYPE, V4L2Protocol.DEVICE_ATTRIBUTE_TAG, l.get("5-deviceFile"));
		if(ids.size()==0)
			return;

		if(ids.size()>1)
			logger.error("More than one protocol was handling device "+l.get("5-deviceFile")+" it seems. Removing  1st one" + ids.get(0).getName());
		try {
			removeProtocol(ids.get(0));
		} catch (Exception e) {
			logger.error("Couldnt remove protocol "+ids.get(0).getName());
			e.printStackTrace();
		}
	}
}
