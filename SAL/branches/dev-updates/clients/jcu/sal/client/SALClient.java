package jcu.sal.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import jcu.sal.agent.LocalAgentImpl;
import jcu.sal.common.CommandFactory;
import jcu.sal.common.Constants;
import jcu.sal.common.Response;
import jcu.sal.common.StreamID;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.agents.SALAgent;
import jcu.sal.common.agents.SALAgentFactory;
import jcu.sal.common.cml.CMLArgument;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.CMLDescription;
import jcu.sal.common.cml.CMLDescriptions;
import jcu.sal.common.cml.ResponseType;
import jcu.sal.common.cml.StreamCallback;
import jcu.sal.common.events.ClientEventHandler;
import jcu.sal.common.events.Event;
import jcu.sal.common.exceptions.ArgumentNotFoundException;
import jcu.sal.common.exceptions.ClosedStreamException;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SensorControlException;
import jcu.sal.common.sml.SMLDescription;
import jcu.sal.common.sml.SMLDescriptions;
import jcu.sal.common.utils.XMLhelper;

public class SALClient implements ClientEventHandler, StreamCallback{
	public static class JpgMini implements StreamCallback{
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
	    	f.setVisible(true);
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
	    	f=null;
	    }

		@Override
		public void collect(Response r) throws IOException {
			if(f==null)
				throw new IOException("JFrame closed");
			try {
				setImage(r.getBytes());
			} catch (SensorControlException e) {
				System.out.println("Stream terminated");
				if(!e.getClass().equals(ClosedStreamException.class))
					System.out.println("Error: "+e.getMessage());
				close();
			}
		}
	    
	    
	}
	
	private SALAgent agent;
	private BufferedReader b;
	private List<StreamID> streams;
	
	public SALClient(SALAgent a) throws RemoteException {
		agent = a;
		b = new BufferedReader(new InputStreamReader(System.in));
		streams = new Vector<StreamID>();
	}
	
	
	public void start() throws ConfigurationException{
		try {
			agent.registerEventHandler(this, Constants.SENSOR_MANAGER_PRODUCER_ID);
			agent.registerEventHandler(this, Constants.PROTOCOL_MANAGER_PRODUCER_ID);
			agent.registerEventHandler(this, Constants.SENSOR_STATE_PRODUCER_ID);
		}  catch (NotFoundException e) {
			//cant register for event, we can keep going
		}
	}
	
	public int getAction(){
		int sid=-1;;
		boolean ok=false;
		System.out.println("Enter either :\n\ta sensor id to send a command\n\t-1 to quit\n\t-2 to see a list of active sensors (XML)");
		System.out.println("\t-3 to see a list of active sensors (shorter, human readable listing)");
		System.out.println("\t-4 to add a new protocol\n\t-5 to remove a protocol\n\t-6 to list all protocols");
		System.out.println("\t-7 to add a new sensor\n\t-8 to remove a sensor");
		System.out.println("\t-9 to list all sensors (XML)\n\t-10 to list all sensors(shorter, human readable listing)");
		System.out.println("\t-11 to stop a stream");
		while(!ok)
			try {
				sid = Integer.parseInt(b.readLine());
				ok = true;
			} catch (Exception e) {}

		return sid;
	}
	
	public void doActionSensor(int sid) throws Exception{
		String str2;
		CommandFactory cf;
		Command c = null;
		StreamID id;
		CMLArgument arg;
		CMLDescriptions cmls;
		CMLDescription cml;
		ResponseType r;
		int i, j;
		
		cmls = new CMLDescriptions(agent.getCML(String.valueOf(sid)));
		System.out.println("\n\nHere is the CML document for this sensor:");
		System.out.println(cmls.getXMLString());
		System.out.println("Print human-readable form ?(Y/n)");
		if(!b.readLine().equals("n")){
			for(CMLDescription tmp : cmls.getDescriptions()){
				System.out.print("CID: "+tmp.getCID());
				System.out.println(" - "+tmp.getShortDesc());
			}
		}
		System.out.println("Enter a command id:");
		i=Integer.parseInt(b.readLine());
		cml = cmls.getDescription(i);
		
		cf = new CommandFactory(cml);
		
		if(cml.isStreamable()){
			System.out.println("How often the command should be run (ms) ?(min:"+
					cml.getSamplingBounds().getMin()+"-max:"+cml.getSamplingBounds().getMax()+") ");
			j=Integer.parseInt(b.readLine());
			if(j!=-1 && j<cml.getSamplingBounds().getMin() || j>cml.getSamplingBounds().getMax()){
				System.out.println("Invalid value - using "+cml.getSamplingBounds().getMax());
				j = cml.getSamplingBounds().getMax();
			}
			cf.setInterval(j);			
		}
		
		
		r = cml.getResponseType();
		boolean argOK=false, argsDone=false;
		while(!argsDone) {			
			for(String str: cf.listArgNames()){
				arg = cf.getArg(str);
				argOK=false;
				while(!argOK) {
					System.out.println("Enter a value of type '"+arg.getType()+
							"' for argument '"+str+"' "+ (arg.hasDefaultValue()?"(default:"+arg.getDefaultValue()+")":""));
					str2 = b.readLine();
					try {cf.addArgumentValue(str, str2); argOK = true;}
					catch (ArgumentNotFoundException e1) {
						System.out.println("Wrong value");
						argOK=false;
					}
				}

			}

			if(r.getContentType().equals(CMLConstants.CONTENT_TYPE_JPEG))
				cf.addCallBack(new JpgMini(String.valueOf(sid)));
			else if(r.getContentType().equals(CMLConstants.CONTENT_TYPE_TEXT_PLAIN)){
				cf.addCallBack(this);
			} else {
				System.out.println("Cant handle content "+r.getContentType());
				return;
			}
			
			try {c = cf.getCommand(); argsDone=true;}
			catch (ConfigurationException e1) {System.out.println("Values missing"); throw e1; }//argsDone=false;}
		}
		
		id = agent.setupStream(c, String.valueOf(sid));
		if(id!=null) {
			agent.startStream(id);
			streams.add(id);
			System.out.println("Stream ID: "+ id.getID());
		}
			
	}
	
	public void printSensorList(SMLDescriptions smls){
		for(SMLDescription tmp: smls.getDescriptions()){
			System.out.print("SID: "+tmp.getSID());
			System.out.print(" - "+tmp.getSensorAddress());
			System.out.print(" - "+tmp.getProtocolType());
			System.out.println(" - "+tmp.getProtocolName());
		}
	}
	
	public void printStreamList() throws IOException{
		int i=0, nb = 0;
		for(StreamID id: streams)
			System.out.println("Stream #"+(i++)+" : "+id.getID());
		
		System.out.println("Enter the stream # to terminate or -1 to go back");
		try {nb = Integer.parseInt(b.readLine());}
		catch (NumberFormatException e){}
		if(nb!=-1){
			try {
				System.out.println("stopping "+streams.get(nb).getID());
				agent.terminateStream(streams.get(nb));
				streams.remove(nb);
			} catch (NotFoundException e) {
				System.out.println("No such stream");
			}
			
		}
			
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
				else if(sid==-3)
					printSensorList(new SMLDescriptions(agent.listActiveSensors()));
				else if(sid==-4) {
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
				} else if(sid==-6)
					System.out.println(XMLhelper.toString(agent.listProtocols()));				
				else if(sid==-7) {
					System.out.println("Enter the XML doc for the new sensor:");
					sb.delete(0, sb.length());
					while(!(str=b.readLine()).equals(""))
						sb.append(str);
					agent.addSensor(sb.toString());
					sb.delete(0, sb.length());
				} else if(sid==-8) {
					System.out.println("Enter the ID of the Sensor to be removed:");
					str=b.readLine();
					agent.removeSensor(str);
				} else if(sid==-9)
					System.out.println(agent.listSensors());
				else if(sid==-10)
					printSensorList(new SMLDescriptions(agent.listSensors()));
				else if(sid==-11)
					printStreamList();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void stop() throws RemoteException{
		try {
			agent.unregisterEventHandler(this, Constants.SENSOR_MANAGER_PRODUCER_ID);
			agent.unregisterEventHandler(this, Constants.PROTOCOL_MANAGER_PRODUCER_ID);
			agent.unregisterEventHandler(this, Constants.SENSOR_STATE_PRODUCER_ID);
		} catch (NotFoundException e) {
			//cant unregister our event handler, keep going
		}
	}

	public void collect(Response r) {
		try {
			System.out.println(r.getStreamID().getID()+" : "+r.getString());
		} catch (SensorControlException e) {
			if(e.getClass().equals(ClosedStreamException.class))
				System.out.println("Stream  terminated");
			else
				System.out.println("Stream returned an error");
		}
		return;
	}

	@Override
	public void handle(Event e, SALAgent a) throws IOException {
		System.out.println("Received "+e.toString());
	}
	
	
	
	public static void main(String [] args) throws RemoteException, NotBoundException, ConfigurationException{
		SALClient c;
		SALAgent agent;
		if(args.length==3){
			agent = SALAgentFactory.getFactory().createRMIAgent(args[1], args[2], args[0]);
			c = new SALClient(agent);
		} else if(args.length==2){
			LocalAgentImpl a = new LocalAgentImpl();
			a.start(args[0], args[1]);
			c = new SALClient(a);
			agent = a;
		} else {
			printUsage();			
			return;
		}
		
		c.start();
		try {
			c.run();
		} finally {
			try{c.stop();} catch(Throwable e){}
		}
		
		if(args.length==3){
			SALAgentFactory.getFactory().releaseRMIAgent(agent);
		} else if(args.length==2){
			LocalAgentImpl a = (LocalAgentImpl) agent;
			a.stop();
		}
		
		System.out.println("Main exiting");
		//required for RMI client
		System.exit(0);
	}
	
	public static void printUsage(){
		System.out.println("Usage:");
		System.out.println("To create a SAL client connecting to a local agent, use the following arguments:");
		System.out.println("1: the platform config file - 2: the sensor config file");
		System.out.println("To create a SAL client connecting to an RMI SAL Agent, provide the following arguments:");
		System.out.println("1: our RMI name - 2: the IP address of the SAL agentRegistry - 3: the IP address of our Registry");
	}
}
