package jcu.sal.client.stressTest.actions;

import java.rmi.RemoteException;
import java.util.Random;

import jcu.sal.common.Slog;
import jcu.sal.common.XMLhelper;
import jcu.sal.common.agents.rmi.RMIAgent;
import jcu.sal.common.agents.rmi.RMICommandFactory;
import jcu.sal.common.agents.rmi.RMICommandFactory.RMICommand;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SensorControlException;
import jcu.sal.common.sml.SMLDescriptions;

import org.apache.log4j.Logger;

public class ExecuteSensorAction implements Action {
	private static Logger logger = Logger.getLogger(ExecuteSensorAction.class);
	static {
		Slog.setupLogger(logger);
	}
	public static String name="Dummy_";
	
	private RMIAgent agent;
	private Random r;
	private RMICommand cmd;
	
	public ExecuteSensorAction(RMIAgent a){
		agent = a;
		r = new Random();
		cmd = null;
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
		
		if(cmd==null){			
			try {
				RMICommandFactory f = new RMICommandFactory(XMLhelper.createDocument(agent.getCML(name.toString())), 100);
				cmd = f.getCommand();
			} catch (Exception e) {
				logger.info("Shouldnt be here");
				e.printStackTrace();
				return;
			}			
		}

		try {
			//logger.info("executing command on sensor "+name+" ("+protocol+")");
			agent.execute(cmd, name.toString());
			//logger.info("command executed by sensor "+name);
		} catch (RemoteException e) {
			logger.info("sensor "+name+" cant be removed");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFoundException e) {
			logger.info("we shouldnt be here");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SensorControlException e) {
			logger.info("Command execution failed");
			e.printStackTrace();
		} 
	}

}