package jcu.sal.agent;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import jcu.sal.common.CommandFactory;
import jcu.sal.common.Response;
import jcu.sal.common.Slog;
import jcu.sal.common.StreamID;
import jcu.sal.common.agents.SALAgent;
import jcu.sal.common.agents.rmi.RMIAgent;
import jcu.sal.common.agents.rmi.RMIEventHandler;
import jcu.sal.common.agents.rmi.RMIStreamCallback;
import jcu.sal.common.agents.rmi.RMICommandFactory.RMICommand;
import jcu.sal.common.cml.StreamCallback;
import jcu.sal.common.events.ClientEventHandler;
import jcu.sal.common.events.Event;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SensorControlException;

import org.apache.log4j.Logger;

/**
 * This class acts as an adapter around a {@link SALAgent} object and transforms
 * it into an {@link RMIAgent}
 * @author gilles
 *
 */
public class RMIAgentImpl implements RMIAgent {
	private static class SALClient {
		public static int RMI_REGISTRY_PORT = 1099;
		private int port;
		private String ip;
		private Registry registry;
		
		public SALClient(String ip, int port) throws RemoteException{
			registry = LocateRegistry.getRegistry(ip);
			this.ip = ip;
			this.port = port;
		}
		
		public SALClient(String ip) throws RemoteException{
			this(ip, RMI_REGISTRY_PORT);
		}
		
		public Remote getRef(String objName) throws RemoteException{
			try {
				return registry.lookup(objName);
			} catch (Exception e) {
				System.out.println("Cant find object '"+objName+"' at "+ip+":"+port);
				e.printStackTrace();
				throw new RemoteException();
			}
		}
	}
	
	private static Logger logger = Logger.getLogger(LocalAgentImpl.class);
	static {Slog.setupLogger(logger);}
	
	private LocalAgentImpl agent;
	private Map<String, SALClient> clients;
	
	public RMIAgentImpl(){
		clients = new Hashtable<String, SALClient>();
		agent = new LocalAgentImpl();
	}
	
	public void start(String pc, String sc) throws ConfigurationException{
		agent.start(pc,sc);
	}
	
	public void stop(){
		agent.stop();
	}

	@Override
	public void addProtocol(String xml, boolean loadSensors) throws ConfigurationException, SALDocumentException {
		agent.addProtocol(xml, loadSensors);
	}

	@Override
	public String addSensor(String xml) throws ConfigurationException, SALDocumentException {
		return agent.addSensor(xml);
	}

	@Override
	public StreamID setupStream(RMICommand c, String sid) throws RemoteException, NotFoundException, SensorControlException {
		List<String> l = c.getRMIStreamCallBack();
		StreamCallback target = new ProxyStreamCallback(
				(RMIStreamCallback )clients.get(l.get(0)).getRef(l.get(1)), 
				l.get(0), 
				l.get(1) );

		return agent.setupStream(CommandFactory.getCommand(c.getCommand(), target), sid);
	}
	
	@Override
	public void startStream(StreamID sid) throws RemoteException, NotFoundException {
		agent.startStream(sid);
	}
	
	@Override
	public void terminateStream(StreamID sid) throws RemoteException, NotFoundException {
		agent.terminateStream(sid);
	}

	@Override
	public String getCML(String sid) throws NotFoundException{
		return agent.getCML(sid);
	}

	@Override
	public String listActiveSensors(){
		return agent.listActiveSensors();
	}

	@Override
	public String listSensors() {
		return agent.listSensors();
	}
	
	public String listSensor(String sid) throws NotFoundException {
		return agent.listSensor(sid);
	}

	@Override
	public void registerClient(String name, String ipAddress) throws ConfigurationException, RemoteException {
		SALClient c = new SALClient(ipAddress);
		synchronized(clients){
			if(clients.containsKey(name))
				throw new ConfigurationException();
			clients.put(name, c);
		}
		System.out.println("Client '"+name+"'@"+ipAddress+" registered");
	}

	@Override
	public void unregisterClient(String name) throws ConfigurationException {
		synchronized(clients){
			if(!clients.containsKey(name))
				throw new ConfigurationException();
			clients.remove(name);
		}
		System.out.println("Client '"+name+"' unregistered");
	}

	@Override
	public void registerEventHandler(String rmiName, String objName, String producerID) throws RemoteException, NotFoundException {
		ClientEventHandler eh;
		synchronized (clients) {
			if(!clients.containsKey(rmiName))
				throw new NotFoundException("Cant find RMI client named '"+rmiName+"'");
			
			eh = new ProxyEventHandler((RMIEventHandler) clients.get(rmiName).getRef(objName),rmiName, objName,producerID);
			agent.registerEventHandler(eh, producerID);
			
			/* null any ref to remote objects for GC*/
			eh = null;
		}
	}
	
	@Override
	public void unregisterEventHandler(String rmiName, String objName, String producerID) throws NotFoundException, RemoteException {
		ClientEventHandler eh;
		synchronized (clients) {
			if(!clients.containsKey(rmiName))
				throw new NotFoundException("Cant find RMI client named '"+rmiName+"'");

			eh = new ProxyEventHandler((RMIEventHandler) clients.get(rmiName).getRef(objName), rmiName, objName,producerID);
			agent.unregisterEventHandler(eh, producerID);
			
			/* null any ref to remote objects for GC*/
			eh = null;
		}
	}

	@Override
	public void removeProtocol(String pid, boolean removeSensors) throws NotFoundException {
		agent.removeProtocol(pid, removeSensors);
	}

	@Override
	public void removeSensor(String sid) throws NotFoundException {
		agent.removeSensor(sid);
	}
	
	@Override
	public String listProtocols(){
		return agent.listProtocols();
	}
	
	public static void main (String args[]) throws ConfigurationException, IOException{
		if(args.length!=3) {
			System.out.println("We need three arguments:");
			System.out.println("1: the IP address of our registry - 2: the platform configuration file - 3: the sensor configuration file");
			System.exit(1);
		}
		Registry registry;
		try {
			//registry = LocateRegistry.getRegistry(args[0]);
			try {
				registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			} catch (ExportException e){
				//a registry is already running ? try getRegistry instead
				registry = LocateRegistry.getRegistry(args[0]);
			}
		} catch (RemoteException e1) {
			System.out.println("Error finding our registry");
			throw e1;
		}
		
		RMIAgentImpl agent = new RMIAgentImpl();
		agent.start(args[1], args[2]);
		try {
			//System.out.println("RMI SAL Agent @ "+args[0]+" exporting");
			RMIAgent stub = (RMIAgent) UnicastRemoteObject.exportObject((RMIAgent) agent, 0);
			registry.rebind(RMI_STUB_NAME, stub);
			System.out.println("RMI SAL Agent @ "+args[0]+" ready. Press <Enter> to quit");
			System.in.read();
		} catch (RemoteException e) {
			System.out.println("Error binding agent to the RMI registry");
			e.printStackTrace();
		} catch(Throwable t){
			t.printStackTrace();
		} finally {
			//System.out.println("RMI SAL Agent @ "+args[0]+" stopping");
			agent.stop();
			System.exit(0);
		}
	}
	
	private static class ProxyEventHandler implements ClientEventHandler{
		private RMIEventHandler r;
		private String rmiName, objName, producerID;
		public ProxyEventHandler(RMIEventHandler r, String rmiName, String objName, String producerID){
			this.r = r;
			this.rmiName = rmiName;
			this.objName = objName;
			this.producerID = producerID;
		}

		@Override
		public void handle(Event e, SALAgent s) throws IOException {
			try {
				r.handle(e);
			} catch (Throwable e1) {
				e1.printStackTrace();
				throw new IOException("Error dispatching event to handler - "+e1.getMessage());
			}
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((objName == null) ? 0 : objName.hashCode());
			result = prime * result
					+ ((producerID == null) ? 0 : producerID.hashCode());
			result = prime * result
					+ ((rmiName == null) ? 0 : rmiName.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ProxyEventHandler other = (ProxyEventHandler) obj;
			if (objName == null) {
				if (other.objName != null)
					return false;
			} else if (!objName.equals(other.objName))
				return false;
			if (producerID == null) {
				if (other.producerID != null)
					return false;
			} else if (!producerID.equals(other.producerID))
				return false;
			if (rmiName == null) {
				if (other.rmiName != null)
					return false;
			} else if (!rmiName.equals(other.rmiName))
				return false;

			return true;
		}
	}
	
	private static class ProxyStreamCallback implements StreamCallback{
		private RMIStreamCallback s;
		private String rmiName, objName;
		public ProxyStreamCallback(RMIStreamCallback s, String rmiName, String objName){
			this.s = s;
			this.rmiName = rmiName;
			this.objName = objName;
		}

		@Override
		public void collect(Response r) throws IOException {
			try {
				s.collect(r);
			} catch (Throwable e1) {
				//e1.printStackTrace();
				//be careful not to pass client thrown exceptions
				//since their classes may not be in the class path of the SAL agent
				throw new IOException("Error collecting response.\n"+e1.getMessage());
			}			
		}
		
		@Override
		public int hashCode() {
			final int PRIME = 13;
			int result = 1;
			result = PRIME * result + ((objName == null) ? 0 : objName.hashCode());
			result = PRIME * result + ((rmiName == null) ? 0 : rmiName.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final ProxyEventHandler other = (ProxyEventHandler) obj;
			if (objName == null) {
				if (other.objName != null)
					return false;
			} else if (!objName.equals(other.objName))
				return false;
			if (rmiName == null) {
				if (other.rmiName != null)
					return false;
			} else if (!rmiName.equals(other.rmiName))
				return false;
			return true;
		}
	}


}
