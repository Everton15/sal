package jcu.sal.client.stressTest.actions;

import java.util.Random;
import java.util.Vector;

import jcu.sal.common.Parameters;
import jcu.sal.common.Slog;
import jcu.sal.common.Parameters.Parameter;
import jcu.sal.common.agents.SALAgent;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.pcml.ProtocolConfigurations;
import jcu.sal.common.sml.SMLConstants;
import jcu.sal.common.sml.SMLDescription;
import jcu.sal.common.sml.SMLDescriptions;

import org.apache.log4j.Logger;

public class AddSensorAction implements Action {
	private static Logger logger = Logger.getLogger(AddSensorAction.class);
	static {
		Slog.setupLogger(logger);
	}
	public static String name="Dummy_";
	public static int MAX_SENSORS = 100;
	
	private SALAgent agent;
	private Random r;
	
	public AddSensorAction(SALAgent a){
		agent = a;
		r = new Random();
	}

	public void execute() {
		SMLDescriptions s;
		try {
			s = new SMLDescriptions(agent.listSensors());
		} catch (Exception e1) {
			logger.info("we shouldnt be here");
			e1.printStackTrace();
			return;
		}

		if(s.getSize()>MAX_SENSORS){
			//logger.info("cant create sensors, MAX reached");
			return;
		}
		
		ProtocolConfigurations p;
		try {
			p = new ProtocolConfigurations(agent.listProtocols());
		} catch (Exception e1) {
			logger.info("we shouldnt be here");
			e1.printStackTrace();
			return;
		}
		
		String n1;
		if(p.getSize()==0) {
			//logger.info("Cant add sensor - no protocols setup yet");
			return;
		} else
			n1= name+r.nextInt(p.getSize());

			
		
		Vector<Parameter> v = new Vector<Parameter>();
		v.add(new Parameter(SMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE, "DUMMY"));
		v.add(new Parameter(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE, n1));
		v.add(new Parameter(SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE, String.valueOf(r.nextInt())));

		try {
			//logger.info("creating sensor for protocol "+n1);
			agent.addSensor(new SMLDescription(new Integer(1), new Parameters(v)).getXMLString());
			//logger.info("sensor "+ret+" created");
		} catch (ConfigurationException e) {
			logger.info("sensor cant be instanciated");
			e.printStackTrace();
		} catch (SALDocumentException e) {
			logger.info("we shouldnt be here - sensor cant be created");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
