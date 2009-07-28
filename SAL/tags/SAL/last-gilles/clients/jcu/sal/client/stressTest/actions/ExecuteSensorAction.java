package jcu.sal.client.stressTest.actions;

import java.io.IOException;
import java.util.Random;

import jcu.sal.common.CommandFactory;
import jcu.sal.common.Response;
import jcu.sal.common.Slog;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.agents.SALAgent;
import jcu.sal.common.cml.CMLDescriptions;
import jcu.sal.common.cml.StreamCallback;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SensorControlException;
import jcu.sal.common.sml.SMLDescriptions;

import org.apache.log4j.Logger;

public class ExecuteSensorAction implements Action, StreamCallback {
	private static Logger logger = Logger.getLogger(ExecuteSensorAction.class);
	static {
		Slog.setupLogger(logger);
	}
	public static String name="Dummy_";
	
	private SALAgent agent;
	private Random r;
	private Command cmd;
	
	public ExecuteSensorAction(SALAgent a){
		agent = a;
		r = new Random();
		cmd = null;
	}

	public void execute() {
		SMLDescriptions p;
		CMLDescriptions c;
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
		
		if(cmd==null){			
			try {
				c = new CMLDescriptions(agent.getCML(name.toString()));
				CommandFactory f = new CommandFactory(c.getDescription(100), this);
				cmd = f.getCommand();
			} catch (Exception e) {
				logger.info("Shouldnt be here");
				e.printStackTrace();
				return;
			}			
		}

		try {
			//logger.info("executing command on sensor "+name+" ("+protocol+")");
			agent.setupStream(cmd, name.toString());
			//logger.info("command executed by sensor "+name);
		} catch (NotFoundException e) {
			logger.info("we shouldnt be here");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SensorControlException e) {
			logger.info("Command execution failed");
			e.printStackTrace();
		} 
	}

	@Override
	public void collect(Response r) throws IOException {}

}
