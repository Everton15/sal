package jcu.sal.client;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.Vector;

import javax.naming.ConfigurationException;

import jcu.sal.common.CommandFactory;
import jcu.sal.common.Parameters;
import jcu.sal.common.Response;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.Parameters.Parameter;
import jcu.sal.common.agents.SALAgent;
import jcu.sal.common.agents.SALAgentFactory;
import jcu.sal.common.cml.CMLDescriptions;
import jcu.sal.common.cml.StreamCallback;
import jcu.sal.common.pcml.EndPointConfiguration;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.common.sml.SMLConstants;
import jcu.sal.common.sml.SMLDescription;
import jcu.sal.common.sml.SMLDescriptions;

/**
 * This SAL RMI client creates NB_SENSORS dummy sensor on a SAL agent. It then starts NB_CLIENTS
 * threads all sending generic command 100 to a SAL agent for RUN_LENGTH milliseconds. At the end,
 * statistics about the number of commands sent per second and the average command execution time 
 * are printed. 
 * @author gilles
 *
 */
public class RmiClientDummyStressTest implements StreamCallback{
	public static int RUN_LENGTH=120*1000;
	public static int NB_SENSORS = 5000;
	public static int NB_CLIENTS[] = {1, 10, 25, 50, 100};
	private String[] sids = new String[NB_SENSORS];
	private Vector<Thread> threads;
	private SALAgent agent;
	private long sumAvgExe = 0, sumCounts;
	
	private class Control implements Runnable{
		private Command command;
		private long  executionTotal;
		private int count;
		
		public Control(Command c){
			command = c;
			count = 0;
			executionTotal = 0;
		}
		
		public void run(){
			long t;
			Random r = new Random();
			while(!Thread.interrupted()){ 
				t = System.currentTimeMillis();
				try {agent.setupStream(command, sids[r.nextInt(sids.length)]);} catch(Exception e) {}
				executionTotal += (System.currentTimeMillis() - t);
				count++;
			}
			
			//System.out.println("Sent "+count+" cmds - avg exe: "+getAvgExe());
			sumAvgExe += getAvgExe();
			sumCounts += count;
		}

		public long getAvgExe(){
			return executionTotal/count;
		}
	}	
	
	public RmiClientDummyStressTest(String rmiName, String agentRMIRegIP, String ourIP) throws RemoteException {
		threads = new Vector<Thread>();
		try {
			agent = SALAgentFactory.getFactory().createRMIAgent(agentRMIRegIP, ourIP, rmiName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException();
		}
	}	
	
	public void populateSensorList() throws ConfigurationException{
		int i=0;
		try {
			for(SMLDescription s: new SMLDescriptions(agent.listActiveSensors()).getDescriptions())
				sids[i++] = s.getID();

		} catch (Exception e) {
			e.printStackTrace();
			throw new ConfigurationException();
		}
	}
	
	public void createDummySensors(){
		Vector<Parameter> v = new Vector<Parameter>();
		Integer id = new Integer(1);

		ProtocolConfiguration p = new ProtocolConfiguration("dummy0", "DUMMY",
															new EndPointConfiguration("fs","fs"));
		try {
			agent.addProtocol(p.getXMLString(), false);
		} catch (Exception e) {
			e.printStackTrace();
		}


		for(int i=0;i<NB_SENSORS; i++) {
			v.removeAllElements();
			v.add(new Parameter(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE, "dummy0"));
			v.add(new Parameter(SMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE, "DUMMY"));
			v.add(new Parameter(SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE, "_"+i));
			
			try {
				agent.addSensor(new SMLDescription(id, new Parameters(v)).getXMLString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void createClients(int nb){
		CommandFactory cf;
		threads.removeAllElements();

		try {
			CMLDescriptions cmls = new CMLDescriptions(agent.getCML(sids[0]));
			cf = new CommandFactory(cmls.getDescription(100), this);
			
			for(int i=0; i<nb; i++) 
				threads.add(new Thread(new Control(cf.getCommand())));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void run(){
		long t;

		try {
			System.out.print("Creating dummy sensors");
			t = System.currentTimeMillis();
			createDummySensors();
			System.out.println("("+(System.currentTimeMillis()-t)+" ms)");
			
			System.out.print("Populating dummy sensor list");
			t = System.currentTimeMillis();
			populateSensorList();
			System.out.println("("+(System.currentTimeMillis()-t)+" ms)");
			
			for(int i=0; i<NB_CLIENTS.length; i++) {
				System.out.println("Creating clients");
				createClients(NB_CLIENTS[i]);
				System.out.print("Running stress test");
				
				//Starts all client threads 
				t = System.currentTimeMillis();
				for(Thread th: threads)
					th.start();
				
				Thread.sleep(RUN_LENGTH);
				
				//Stops all the threads
				for(Thread th: threads)
					th.interrupt();
				
				//Joins all the threads
				for(Thread th: threads)
					th.join();
				
				System.out.println("("+(System.currentTimeMillis()-t)+" ms)");
	
				System.out.println("Grand Total");
				System.out.println(NB_CLIENTS[i]+" clients - "+NB_SENSORS+" sensors");
				System.out.println(sumCounts+" commands processed ("+((1000.0*sumCounts)/RUN_LENGTH)+"cmd/sec)");
				System.out.println("Avg exe time: "+(sumAvgExe/NB_CLIENTS[i]));
				sumCounts = sumAvgExe = 0;
			}
			agent.removeProtocol("dummy0", true);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String [] args) throws RemoteException, NotBoundException{
		if(args.length!=3) {
			System.out.println("We need three arguments:");
			System.out.println("1: our RMI name - 2: the IP address of our agentRegistry - 3: the IP address of the Agent agentRegistry");
			System.exit(1);
		}
		
		new RmiClientDummyStressTest(args[0], args[2], args[1]).run();
		
		System.out.println("Main exiting");
	}

	@Override
	public void collect(Response r) throws IOException {}
}

