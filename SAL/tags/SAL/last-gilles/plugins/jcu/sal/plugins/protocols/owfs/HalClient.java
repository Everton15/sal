package jcu.sal.plugins.protocols.owfs;


import java.util.Map;
import java.util.Vector;

import jcu.sal.common.Parameters;
import jcu.sal.common.Slog;
import jcu.sal.common.Parameters.Parameter;
import jcu.sal.common.pcml.EndPointConfiguration;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.plugins.config.hal.AbstractHalClient;
import jcu.sal.plugins.endpoints.UsbEndPoint;

import org.apache.log4j.Logger;

import au.edu.jcu.haldbus.match.GenericMatch;


public class HalClient extends AbstractHalClient {
	private static Logger logger = Logger.getLogger(HalClient.class);
	static {Slog.setupLogger(logger);}

	
	public HalClient(){
		super("OWFS HAL filter");
		
		//THe following rules have been commented out because on some machines, USB product and vendor descriptions
		//are not up-to-date and instead of "DS1490F 2-in-1 Fob, 1-Wire adapter" & "Dallas Semiconductor", HAL reports
		//'Unknown (0x2490)' & 'Unknown (0x04fa)'
		//addMatch("1-product", new GenericMatch<String>("info.product", "DS1490F 2-in-1 Fob, 1-Wire adapter"));
		//addMatch("2-vendor", new GenericMatch<String>("info.vendor", "Dallas Semiconductor"));
		addMatch("3-productId", new GenericMatch<Integer>("usb_device.product_id", new Integer(9360)));
		addMatch("4-vendorId", new GenericMatch<Integer>("usb_device.vendor_id", new Integer(1274)));		
	}

	@Override
	public void deviceAdded(Map<String,String> l) {
		logger.debug("Found DS9490 1-wire adapter");
		if(!isProtocolRunning(OWFSProtocol.OWFSPROTOCOL_TYPE)){
			try {
				Vector<Parameter> v = new Vector<Parameter>();
				v.add(new Parameter("Location", "/opt/owfs/bin/owfs"));
				v.add(new Parameter("MountPoint", "/mnt/w1"));
				createProtocol(new ProtocolConfiguration(
						"1wirefs",
						OWFSProtocol.OWFSPROTOCOL_TYPE,
						new Parameters(v),
						new EndPointConfiguration("usb-ds9490", UsbEndPoint.ENDPOINT_TYPE)
						));
			} catch (Exception e) {
				logger.error("Instancation failed");
			}
		}
	}
	
	@Override
	public void deviceRemoved(Map<String, String> l) {}
}
