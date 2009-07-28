package jcu.sal.plugins.protocols.osData;


import java.util.Map;

import jcu.sal.common.Slog;
import jcu.sal.common.pcml.EndPointConfiguration;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.plugins.config.hal.AbstractHalClient;
import jcu.sal.plugins.endpoints.FSEndPoint;

import org.apache.log4j.Logger;

import au.edu.jcu.haldbus.match.GenericMatch;


public class HalClient extends AbstractHalClient {
	private static Logger logger = Logger.getLogger(HalClient.class);
	static {Slog.setupLogger(logger);}
	
	public HalClient(){	
		super("OS Data HAL filter");
		addMatch("1-volume.mount_point", new GenericMatch<String>("volume.mount_point", "/"));
	}

	@Override
	public void deviceAdded(Map<String,String> l) {
		logger.debug("Found Operating System data files");
		if(!isProtocolRunning(OSDataConstants.PROTOCOL_TYPE)){
			try {
				createProtocol(new ProtocolConfiguration(
						"osdata",
						OSDataConstants.PROTOCOL_TYPE,
						new EndPointConfiguration(FSEndPoint.ENDPOINT_TYPE+"-osdata", FSEndPoint.ENDPOINT_TYPE)
						));
			} catch (Exception e) {
				logger.error("Instancation failed");
			}
		} //else
			//logger.debug("OSdata already instanciated");
	}
	
	@Override
	public void deviceRemoved(Map<String, String> l) {}
}
