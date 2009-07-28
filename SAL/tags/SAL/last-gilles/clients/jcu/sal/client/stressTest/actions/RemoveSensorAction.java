package jcu.sal.client.stressTest.actions;

import java.util.Random;

import jcu.sal.common.Slog;
import jcu.sal.common.agents.SALAgent;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.sml.SMLDescriptions;

import org.apache.log4j.Logger;

public class RemoveSensorAction implements Action {
	private static Logger logger = Logger.getLogger(RemoveSensorAction.class);
	static {
		Slog.setupLogger(logger);
	}
	public static String name="Dummy_";
	
	private SALAgent agent;
	private Random r;
	
	public RemoveSensorAction(SALAgent a){
		agent = a;
		r = new Random();
	}

	public void execute() {
		SMLDescriptions p;
		try {
			p = new SMLDescriptions(agent.listActiveSensors());
		} catch (Exception e1) {
			logger.info("we shouldnt be here");
			e1.printStackTrace();
			return;
		}

		if(p.getSize()==0){
			//logger.info("no sensors yet");
			return;
		}
		
		Integer name=null;
		int i = r.nextInt(p.getSize());
		int j=0;
		for(Integer n: p.getSIDs()){
			if(j++==i) {
				name = n;
				break;
			}
		}

		try {
			//logger.info("removing sensor "+name);
			agent.removeSensor(name.toString());
			//logger.info("sensor "+name+" removed");
		} catch (NotFoundException e) {
			logger.info("sensor "+name+" cant be removed");
			e.printStackTrace();
		} 
	}

}
