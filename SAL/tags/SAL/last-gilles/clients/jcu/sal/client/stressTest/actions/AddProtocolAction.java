package jcu.sal.client.stressTest.actions;

import java.util.Set;

import jcu.sal.common.Slog;
import jcu.sal.common.agents.SALAgent;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.pcml.EndPointConfiguration;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.common.pcml.ProtocolConfigurations;

import org.apache.log4j.Logger;

public class AddProtocolAction implements Action {
	private static Logger logger = Logger.getLogger(AddProtocolAction.class);
	static {
		Slog.setupLogger(logger);
	}
	public static String name="Dummy_";
	public static int MAX_PROTOCOLS=5;
	
	private static String type = "DUMMY";
	
	private SALAgent agent;
	
	public AddProtocolAction(SALAgent a){
		agent = a;
	}

	public void execute() {
		ProtocolConfigurations p;
		int i;
		try {
			p = new ProtocolConfigurations(agent.listProtocols());
		} catch (Exception e1) {
			logger.info("we shouldnt be here");
			e1.printStackTrace();
			return;
		}

		String n1=null, n2;
		Set<String> s = p.getPIDs();
		for(i=0; i<MAX_PROTOCOLS;i++){
			n2 = name+i; 
			if(!s.contains(n2)){
				n1 = n2;
				break;
			}			
		}
		if(n1==null){
			//logger.info("cant create more protocols, MAX reached");
			return;
		}
		
		try {
			//logger.info("creating protocol "+n1);
			agent.addProtocol(new ProtocolConfiguration(n1, type, new EndPointConfiguration("fs_"+i, "fs")).getXMLString(),false);
			//logger.info("protocol "+n1+" created");
		} catch (ConfigurationException e) {
			logger.info("cant instanciate protocol "+n1);
		} catch (SALDocumentException e) {
			logger.info("we shoulnt be here - Cant create protocol "+n1);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
