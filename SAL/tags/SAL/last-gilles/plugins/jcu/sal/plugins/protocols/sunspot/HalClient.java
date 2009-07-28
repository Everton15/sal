package jcu.sal.plugins.protocols.sunspot;


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
import jcu.sal.plugins.endpoints.SerialEndPoint;

import org.apache.log4j.Logger;

import au.edu.jcu.haldbus.match.AlwaysMatch;
import au.edu.jcu.haldbus.match.GenericMatch;
import au.edu.jcu.haldbus.match.NextMatch;
import au.edu.jcu.haldbus.match.VectorMatch;

import com.sun.spot.spotselector.SpotStateChecker;


public class HalClient extends AbstractHalClient {
	private static Logger logger = Logger.getLogger(HalClient.class);
	static {Slog.setupLogger(logger);}
	
	public HalClient(){
		super("SunSpot HAL Filter");
		
		addMatch("1-SunSpotProduct",
				new NextMatch("@info.parent",
						new NextMatch("@info.parent",new GenericMatch<String>("info.product", "Sun SPOT"))
				)
		);
		addMatch("2-ACMDevice",
					new NextMatch("@info.parent",new GenericMatch<String>("info.linux.driver", "cdc_acm"))
		);
		addMatch("3-capability", new VectorMatch<String>("info.capabilities", "serial"));
		addMatch("4-category", new GenericMatch<String>("info.category", "serial"));
		addMatch("5-id",
				new NextMatch("@info.parent",
						new NextMatch("@info.parent",new AlwaysMatch("usb_device.serial"))
				)
		);
		addMatch("6-deviceFile", new AlwaysMatch("serial.device"));		
	}

	@Override
	public void deviceAdded(Map<String,String> l) {
		int state;
		SpotStateChecker c = new SpotStateChecker();
		//logger.debug("Found "+l.get("8-info.product")+" - "+l.get("9-info.vendor")+ " on "+l.get("5-deviceFile"));
		logger.debug("Found SunSpot "+l.get("5-id")+"  on "+l.get("6-deviceFile"));
		
		if(isProtocolRunning(SunSPOTProtocol.PROTOCOL_TYPE)){
			logger.debug("SALSpot base station already connected - skipping");
			return;
		}
		
		//wait to carry out the check
		try {Thread.sleep(2000);} catch (InterruptedException e1) {}
		
		try {
			state = c.determineState(l.get("6-deviceFile"), true);
			logger.debug("state: "+SpotStateChecker.stateStringFor(state));
		} catch (Throwable t){
			//this try-catch stuff is required because determineState can throw many
			//things if unable to communicate through the ACM serial port
			//if this happens, most likely it s not a BS
			state = -1;
		}
		
        if(state!=SpotStateChecker.RUNNING_BASESTATION){
        	logger.debug("Not a basestation - skipping ");
        	return;
        }
        
		ProtocolConfiguration pc;

		try {
			//check if a running protocol already uses our device file (can happen during the initial run if for instance a protocol is 
			//instantiated with its config taken from the platform config file, and the device is detected again during the initial run)
			if(findRunningProtocolNameFromConfig(
					SunSPOTProtocol.PROTOCOL_TYPE, 
					SunSPOTProtocol.DEVICE_ATTRIBUTE_TAG, 
					l.get("6-deviceFile"))
				.size()!=0){
				logger.debug("An existing protocol already uses "+l.get("6-deviceFile"));
				return;
			}
			
			//the device is available. check if there is an existing config for it in the  PlatformConfig file
			pc = findProtocolConfigFromFile(SunSPOTProtocol.DEVICE_ATTRIBUTE_TAG, l.get("6-deviceFile"));
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
			v.add(new Parameter(SerialEndPoint.PORTDEVICEATTRIBUTE_TAG, l.get("6-deviceFile")));
			//add the no setup tag so the end point does not try to set parity,baud rate, ... on tty
			v.add(new Parameter(SerialEndPoint.NOSETUP_TAG, ""));
			Parameters params = new Parameters(v);
			
			pc = new ProtocolConfiguration("SALSpot-"+l.get("6-deviceFile"),
						SunSPOTProtocol.PROTOCOL_TYPE,
						new EndPointConfiguration(
								SerialEndPoint.ENDPOINT_TYPE+"-"+l.get("6-deviceFile"),
								SerialEndPoint.ENDPOINT_TYPE,
								params
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
		List<Identifier> ids = findRunningProtocolNameFromConfig(
				SunSPOTProtocol.PROTOCOL_TYPE, 
				SunSPOTProtocol.DEVICE_ATTRIBUTE_TAG, 
				l.get("6-deviceFile")
				);
		if(ids.size()==0)
			return;

		if(ids.size()>1)
			logger.error("More than one protocol was handling device "+
					l.get("6-deviceFile")+" it seems. Removing  1st one" + ids.get(0).getName());
		try {
			removeProtocol(ids.get(0));
		} catch (Exception e) {
			logger.error("Couldnt remove protocol "+ids.get(0).getName());
			e.printStackTrace();
		}
	}
}
