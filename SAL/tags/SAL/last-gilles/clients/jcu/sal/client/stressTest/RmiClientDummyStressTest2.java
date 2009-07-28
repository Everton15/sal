package jcu.sal.client.stressTest;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import jcu.sal.client.stressTest.actions.Action;
import jcu.sal.client.stressTest.actions.AddProtocolAction;
import jcu.sal.client.stressTest.actions.AddSensorAction;
import jcu.sal.client.stressTest.actions.ExecuteSensorAction;
import jcu.sal.client.stressTest.actions.RemoveProtocolAction;
import jcu.sal.client.stressTest.actions.RemoveSensorAction;
import jcu.sal.common.Slog;
import jcu.sal.common.agents.SALAgent;
import jcu.sal.common.agents.SALAgentFactory;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.common.pcml.ProtocolConfigurations;
import jcu.sal.common.sml.SMLDescription;
import jcu.sal.common.sml.SMLDescriptions;

import org.apache.log4j.Logger;

public class RmiClientDummyStressTest2{
	private static Logger logger = Logger.getLogger(RmiClientDummyStressTest2.class);
	static {
		Slog.setupLogger(logger);
	}
	
	/**
	 * how long do we run for
	 */
	public static int RUN_LENGTH=10*1000;
	
	/**
	 * how many client threads do we want
	 */
	public static int NB_CLIENTS = 10;
	
	/**
	 * how long to sleep between actions
	 */
	public static int SLEEP = 0;
	
	private Vector<Client> clients;
	private SALAgent agent;
	private AtomicBoolean setupFlag;
	private ProtocolConfigurations protocolState;
	private SMLDescriptions sensorState;
	
	private class Client implements Runnable{
		private Action[] actions = new Action[10];
		private Thread t;
		private String name;
		private Random r;
		
		
		public Client(String n){
			name = n;
			int i=0;
			actions[i++] = new AddProtocolAction(agent);
			actions[i++] = new RemoveProtocolAction(agent);
			actions[i++] = new AddSensorAction(agent);
			actions[i++] = new RemoveSensorAction(agent);
			actions[i++] = new ExecuteSensorAction(agent);
			r = new Random();
		}
		
		public void start(){
			t = new Thread(this, name);
			t.start();
		}
		
		public void stop(){
			t.interrupt();
			try {
				t.join();
			} catch (InterruptedException e) {}			
			logger.info("Client "+name+" joined");
		}
		
		public void run(){
			Action a;
			if(setupFlag.compareAndSet(false, true)){
				actions[0].execute();
				actions[2].execute();
				actions[2].execute();
				actions[2].execute();
			}
			try {
				Thread.sleep(r.nextInt(5)*1000);
				while(!Thread.interrupted()){
					if(r.nextInt(3)==2)
						a = actions[r.nextInt(4)];
					else
						a = actions[4];

					a.execute();
					Thread.sleep(SLEEP);
				}
			} catch (InterruptedException e) {}
			logger.info("client "+name+" exiting");
		}
	}	
	
	public RmiClientDummyStressTest2(String rmiName, String agentRMIRegIP, String ourIP) throws RemoteException {
		clients = new Vector<Client>();
		setupFlag = new AtomicBoolean(false);
		try {
			agent = SALAgentFactory.getFactory().createRMIAgent(agentRMIRegIP, ourIP, rmiName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException();
		}
	}	

	
	public void createClients(int nb){
		for(int i = 0; i<nb; i++)
			clients.add(new Client("Client "+i));
	}
	
	public void startClients(){
		for(Client c: clients)
			c.start();
	}
	
	public void stopClients(){
		for(Client c: clients)
			c.stop();
	}
	
	public void saveState() throws RemoteException, SALDocumentException{
		protocolState = new ProtocolConfigurations(agent.listProtocols());
		sensorState = new SMLDescriptions(agent.listSensors());
	}
	
	public void removeAllComponents() throws RemoteException, NotFoundException{
		for(String pid: protocolState.getPIDs())
			agent.removeProtocol(pid, true);
	}
	
	public void restoreState() throws RemoteException, SALDocumentException, ConfigurationException {
		for(ProtocolConfiguration p: protocolState.getConfigurations())
			agent.addProtocol(p.getXMLString(), false);
		
		for(SMLDescription s: sensorState.getDescriptions())
			try { 
				agent.addSensor(s.getXMLString());
			} catch (ConfigurationException e){}
	}
	
	public void run(){
		try {
			saveState();
		} catch (Exception e1) {
			logger.info("cant save the state of the agent. exiting");
			e1.printStackTrace();
			return;
		} 
		try {
			removeAllComponents();
		} catch (Exception e1) {
			logger.info("cant remove existing components on the agent. exiting");
			e1.printStackTrace();
			return;
		}
		
		createClients(NB_CLIENTS);
		startClients();
		try {
			Thread.sleep(RUN_LENGTH);
		} catch (InterruptedException e) {}
		stopClients();
		
		try {
			restoreState();
		} catch (Exception e) {
			logger.info("error restoring agent's state");
			e.printStackTrace();
		}
	}
	
	public static void main(String [] args) throws RemoteException, NotBoundException{
		if(args.length!=3) {
			System.out.println("We need three arguments:");
			System.out.println("1: our RMI name - 2: the IP address of our agentRegistry - 3: the IP address of the Agent agentRegistry");
			System.exit(1);
		}
		
		new RmiClientDummyStressTest2(args[0], args[2], args[1]).run();
		System.out.println("Main exiting");
		System.exit(0);
	}
}

