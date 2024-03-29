package jcu.sal.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.channels.ClosedChannelException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.naming.ConfigurationException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import jcu.sal.common.Constants;
import jcu.sal.common.RMICommandFactory;
import jcu.sal.common.Response;
import jcu.sal.common.RMICommandFactory.RMICommand;
import jcu.sal.common.agents.RMISALAgent;
import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.CMLDescription;
import jcu.sal.common.cml.CMLDescriptions;
import jcu.sal.common.cml.RMIStreamCallback;
import jcu.sal.common.events.Event;
import jcu.sal.common.events.RMIEventHandler;
import jcu.sal.common.sml.SMLDescription;
import jcu.sal.common.sml.SMLDescriptions;

public class RmiClient implements RMIEventHandler, RMIStreamCallback{
	
	public static class JpgMini {
		private JLabel l;
		private JFrame f;
		private long start = 0;
		private int n;
		private String sid;
		
	    public JpgMini(String sid){
	        f = new JFrame();
	        this.sid = sid;
	        l = new JLabel();
	        f.getContentPane().add(l);
	        f.setSize(640,480);
	        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	        setVisible();
	    }
	    
	    public void setImage(byte[] b) {
	    	l.setIcon(new ImageIcon(b));
	    	if(start==0)
	    		start = System.currentTimeMillis();
	    	else if(System.currentTimeMillis()>start+10000) {
    			System.out.println("SID: "+sid+" - FPS: "+ (((float) 1000*n/(System.currentTimeMillis()-start))  ));
    			start = System.currentTimeMillis();
    			n = 0;
    		} else
    			n++;
	    }
	    
	    public void close() {
	    	f.dispose();
	    }
	    
	    public void setVisible(){
	    	f.setVisible(true);
	    }
	}
	
	private Map<String, JpgMini> viewers;
	private RMISALAgent agent;
	private Registry agentRegistry, ourRegistry;
	private String RMIname;
	private BufferedReader b;
	
	public RmiClient(String rmiName, String agentRMIRegIP, String ourIP) throws RemoteException {
		agentRegistry = LocateRegistry.getRegistry(agentRMIRegIP);
		ourRegistry = LocateRegistry.getRegistry(ourIP);
		viewers = new Hashtable<String, JpgMini>();
		RMIname = rmiName;
		try {
			agent = (RMISALAgent) agentRegistry.lookup(RMISALAgent.RMI_STUB_NAME);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException();
		}
		b = new BufferedReader(new InputStreamReader(System.in));
	}
	
	
	public void start(String ourRmiIP) throws ConfigurationException{
		
		try {
			agent.registerClient(RMIname, ourRmiIP);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new ConfigurationException();
		}
		try {
			export(RMIname, this);
		} catch (AccessException e) {
			e.printStackTrace();
			throw new ConfigurationException();
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new ConfigurationException();
		}
		
		try {
			agent.registerEventHandler(RMIname, RMIname, Constants.SENSOR_MANAGER_PRODUCER_ID);
			agent.registerEventHandler(RMIname, RMIname, Constants.PROTOCOL_MANAGER_PRODUCER_ID);
			agent.registerEventHandler(RMIname, RMIname, Constants.SENSOR_STATE_PRODUCER_ID);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new ConfigurationException();
		}
	}
	
	public int getAction(){
		int sid=-1;;
		boolean ok=false;
		System.out.println("Enter either :\n\ta sensor id to send a command\n\t-1 to quit\n\t-2 to see a list of active sensors (XML)");
		System.out.println("\t-3 to see a list of active sensors (shorter, human readable listing)");
		System.out.println("\t-4 to add a new protocol\n\t-5 to remove a protocol\n\t-6 to add a new sensor\n\t-7 to remove a sensor");
		System.out.println("\t-8 to list all sensors (XML)\n\t-9 to list all sensors(shorter, human readable listing)");
		while(!ok)
			try {
				sid = Integer.parseInt(b.readLine());
				ok = true;
			} catch (Exception e) {}

		return sid;
	}
	
	public void doActionSensor(int sid) throws Exception{
		String str, str2;
		RMICommandFactory cf;
		RMICommand c = null;
		Response res;
		ArgumentType t;
		CMLDescription tmp;
		CMLDescriptions cmls;
		int j;
		
		cmls = new CMLDescriptions(agent.getCML(String.valueOf(sid)));
		System.out.println("\n\nHere is the CML document for this sensor:");
		System.out.println(cmls.getCMLString());
		System.out.println("Print human-readable form ?(Y/n)");
		if(!b.readLine().equals("n")){
			Iterator<CMLDescription> i = cmls.getDescriptions().iterator();
			while(i.hasNext()){
				tmp = i.next();
				System.out.print("CID: "+tmp.getCID());
				System.out.println(" - "+tmp.getDesc());
			}
		}
		System.out.println("Enter a command id:");
		j=Integer.parseInt(b.readLine());
		
		cf = new RMICommandFactory(cmls.getDescription(j));
		boolean argOK=false, argsDone=false;
		while(!argsDone) {
			Iterator<String> e = cf.listMissingArgNames().iterator();
			while(e.hasNext()){
				str = e.next();
				t = cf.getArgType(str);
				if(!t.getArgType().equals(CMLConstants.ARG_TYPE_CALLBACK)) {
					while(!argOK) {
						System.out.println("Enter value of type '"+t.getArgType()+"' for argument '"+str+"'");
						str2 = b.readLine();
						try {cf.addArgumentValue(str, str2); argOK = true;}
						catch (ConfigurationException e1) {System.out.println("Wrong value"); argOK=false;}
					}
				} else {
					cf.addArgumentCallback(str,RMIname, RMIname);
					viewers.put(String.valueOf(sid), new JpgMini(String.valueOf(sid)));	
				}
			}
			try {c = cf.getCommand(); argsDone=true;}
			catch (ConfigurationException e1) {System.out.println("Values missing"); throw e1; }//argsDone=false;}
		}
		
		res = agent.execute(c, String.valueOf(sid));
		
		if(cmls.getDescription(j).getReturnType().equals(CMLConstants.RET_TYPE_BYTE_ARRAY))
			new JpgMini(String.valueOf(sid)).setImage(res.getBytes());
		else
			System.out.println("Command returned: " + res.getString());				
	}
	
	public void run(){
		int sid=0;
		String str, str2;
		StringBuilder sb = new StringBuilder();	

		while((sid=getAction())!=-1) {
			try {
	
				if(sid>=0)
					doActionSensor(sid);										
				else if(sid==-2)
					System.out.println(agent.listActiveSensors());
				else if(sid==-3){
					SMLDescription tmp;
					Iterator<SMLDescription> i = new SMLDescriptions(agent.listActiveSensors()).getDescriptions().iterator();
					while(i.hasNext()) {
						tmp = i.next();
						System.out.print("SID: "+tmp.getSID());
						System.out.print(" - "+tmp.getSensorAddress());
						System.out.println(" - "+tmp.getProtocolName());
					}
				} else if(sid==-4) {
					System.out.println("Enter the XML doc for the new procotol:");
					sb.delete(0, sb.length());
					while(!(str=b.readLine()).equals(""))
						sb.append(str);
					System.out.println("Load associated sensors from config file ? (yes-NO)");
					str2=b.readLine();
					agent.addProtocol(sb.toString(), (str2.equals("yes"))?true:false);
					sb.delete(0, sb.length());
				}else if(sid==-5) {
					System.out.println("Enter the ID of the protocol to be removed:");
					str=b.readLine();
					System.out.println("Remove associated sensors from config file ? (yes-NO)");
					str2=b.readLine();
					agent.removeProtocol(str, (str2.equals("yes"))?true:false);
				} else if(sid==-6) {
					System.out.println("Enter the XML doc for the new sensor:");
					sb.delete(0, sb.length());
					while(!(str=b.readLine()).equals(""))
						sb.append(str);
					agent.addSensor(sb.toString());
					sb.delete(0, sb.length());
				} else if(sid==-7) {
					System.out.println("Enter the ID of the Sensor to be removed:");
					str=b.readLine();
					agent.removeSensor(str);
				} else if(sid==-8)
					System.out.println(agent.listSensors());
				else if(sid==-9) {
					SMLDescription tmp;
					Iterator<SMLDescription> i = new SMLDescriptions(agent.listSensors()).getDescriptions().iterator();
					while(i.hasNext()) {
						tmp = i.next();
						System.out.print("SID: "+tmp.getSID());
						System.out.print(" - "+tmp.getSensorAddress());
						System.out.println(" - "+tmp.getProtocolName());
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void stop() throws RemoteException{
		try {
			agent.unregisterEventHandler(RMIname, RMIname, Constants.SENSOR_MANAGER_PRODUCER_ID);
			agent.unregisterEventHandler(RMIname, RMIname, Constants.PROTOCOL_MANAGER_PRODUCER_ID);
			agent.unregisterEventHandler(RMIname, RMIname, Constants.SENSOR_STATE_PRODUCER_ID);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			agent.unregisterClient(RMIname);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		agent = null;
		System.gc();
		
	}
	
	
	public static void main(String [] args) throws RemoteException, NotBoundException{
		if(args.length!=3) {
			System.out.println("We need three arguments:");
			System.out.println("1: our RMI name - 2: the IP address of our agentRegistry - 3: the IP address of the Agent agentRegistry");
			System.exit(1);
		}
		
		RmiClient c = new RmiClient(args[0], args[2], args[1]);
		try {
			c.start(args[1]);
			c.run();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try{c.stop();} catch(RemoteException e){}
			System.out.println("Main exiting");
			System.exit(0);
		}
	}

	public void handle(Event e) {
		System.out.println("Received "+e.toString());
	}

	private int n;
	private long ts;
	
	public void collect(Response r) {
		if(ts==0)
			ts = System.currentTimeMillis();
		else if((ts+10000)<System.currentTimeMillis()){
			System.out.println("FPS: "+( (float) ((float) 1000*n/((float)(System.currentTimeMillis()-ts))) ) );
			ts = System.currentTimeMillis();
			n=0;
		} else
			n++;
		
		try {
			viewers.get(r.getSID()).setImage(r.getBytes());
		} catch (ConfigurationException e) {
			System.out.println("Stream from sensor "+r.getSID()+" returned an error");
			viewers.remove(r.getSID());
		} catch (ClosedChannelException e) {
			System.out.println("Stream from sensor "+r.getSID()+" completed");
			viewers.remove(r.getSID());
		}
	}
	
	public void export(String name, Remote r) throws AccessException, RemoteException{
		ourRegistry.rebind(name, UnicastRemoteObject.exportObject(r, 0));
	}
}

