package jcu.sal.client.gui.view;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import jcu.sal.client.gui.ClientController;
import jcu.sal.common.CommandFactory;
import jcu.sal.common.Response;
import jcu.sal.common.StreamID;
import jcu.sal.common.agents.SALAgent;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.CMLDescription;
import jcu.sal.common.cml.ResponseType;
import jcu.sal.common.events.ClientEventHandler;
import jcu.sal.common.events.Event;
import jcu.sal.common.events.ProtocolListEvent;
import jcu.sal.common.events.SensorNodeEvent;
import jcu.sal.common.events.SensorStateEvent;
import jcu.sal.common.exceptions.ClosedStreamException;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SensorControlException;
import jcu.sal.common.pcml.ProtocolConfigurations;
import jcu.sal.common.sml.SMLDescription;

public abstract class AbstractClientView implements ClientEventHandler, ClientView, ResponseHandler {
	
	protected JFrame frame;
	protected SensorTree tree;
	protected JTextArea log;
	protected ActionPanel actionPane;
	protected ClientController controller;
	
	/**
	 * This method initialises all the attributes
	 * @param c the {@link ClientController} object
	 */
	protected AbstractClientView(ClientController c){
		controller = c;
		actionPane = new ActionPanel(this);
		tree = new SensorTree(this);
		log = new JTextArea(10,80);
		frame = new JFrame("SAL Client");
	}
	
	/**
	 * This method initialises the GUI objects. It must be called by the AWT event dispatcher thread.
	 */
	public void initGUI(){
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(),BoxLayout.PAGE_AXIS));

		actionPane.init();
		
		JSplitPane leftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		leftRight.setLeftComponent(tree.getPanel());
		leftRight.setRightComponent(actionPane.getPanel());
		
		log.setEditable(false);
		JScrollPane scroll = new JScrollPane(log);
		
		JSplitPane upDown = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		upDown.setOneTouchExpandable(true);
		upDown.setTopComponent(leftRight);
		upDown.setBottomComponent(scroll);		
		
		frame.setJMenuBar(createMenu());
		frame.getContentPane().add(upDown);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			   /**
		     * Catch window closing event so we can free up resources before exiting
		     * @param e
		     */
			public void windowClosing(WindowEvent e) {
				TextResponseFrame.getFrame().close();
		    	frame.dispose();
		    	release();
				System.exit(0);
			}
		});
		
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * This method is invoked from {@link #initGUI()} to initialise the menu bar.
	 * It must be implemented by subclasses
	 * @return a fully initialised JMenuBar
	 */
	protected abstract JMenuBar createMenu();
	
	@Override
	public void terminateStream(SALAgent a, StreamID s){
		try {
			controller.terminateStream(a, s);
		} catch (NotFoundException e) {
			System.out.println("cant stop stream - stream id not found");
		}
	}
	
	@Override
	public void sendCommand(Hashtable<String, String> values,
			final CMLDescription cml, final Context c) {
		final ResponseHandler handler = createResponseHandler(cml, c);
		final CommandFactory cf;
		
		if(handler==null)
			return;

		cf = new CommandFactory(cml, handler);
		
		if(values.containsKey(CommandDataPane.INTERVAL_NAME))
			cf.setInterval(Integer.parseInt(values.remove(CommandDataPane.INTERVAL_NAME)));
			
		//put all arg-values in 
		for(String name: values.keySet())
			cf.addArgumentValue(name, values.get(name));


		//Must be sent in a separate thread. Otherwise, race condition occur
		//with v4l sensors which stream video handled by the AWT event dispatcher thread
		//it also makes the GUI more responsive for slow sensor, aka 1-wire
		new Thread(new Runnable(){
			public void run(){
				StreamID id;
				try {
					id = controller.setupStream(c.getAgent(), cf.getCommand(), c.getSMLDescription().getID());
					if(id!=null) {
						controller.startStream(c.getAgent(), id);
						handler.setStreamID(id);
					}
				} catch (SensorControlException e) {
					addLog("Error executing command:\n"+e.getMessage()+"("+e.getCause().getMessage()+")");
					handler.close();
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					handler.close();
				} catch (ConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					handler.close();
				}
			}
		}).start();
	
	}
	
	private ResponseHandler createResponseHandler(CMLDescription cml, Context c){
		ResponseType t = cml.getResponseType();
		if(!t.getType().equals(CMLConstants.RET_TYPE_VOID)){
			if(t.getContentType().equals(CMLConstants.CONTENT_TYPE_JPEG))
				return new JPEGViewer(c, this);
			if(t.getContentType().equals(CMLConstants.CONTENT_TYPE_TEXT_PLAIN))
				return new TextResponseHandler(c,this);
			else{
				addLog("Cannot send the command - unhandled content type '"+cml.getResponseType().getContentType()+"'");
				return null;
			}
		} else
			return this;
		
	}


	@Override
	public void handle(Event e, SALAgent a) throws RemoteException {
		addLog("Received event:\n"+e.toString());
		if(e instanceof ProtocolListEvent){
			ProtocolListEvent ple = (ProtocolListEvent) e;
			if(ple.getType()==ProtocolListEvent.PROTOCOL_ADDED){
				//add new protocol to tree
				try {
					ProtocolConfigurations ps = new ProtocolConfigurations(controller.listProtocols(a));
					tree.addProtocol(a, ps.getDescription(ple.getSourceID()));
				} catch (SALDocumentException e1) {
					e1.printStackTrace();
				} catch (NotFoundException e2) {
					e2.printStackTrace();
				}
			} else if(ple.getType()==ProtocolListEvent.PROTOCOL_REMOVED)
				tree.removeProtocol(a,ple.getSourceID());
			
		} else if (e instanceof SensorNodeEvent){
			SensorNodeEvent sne = (SensorNodeEvent) e;
			if(sne.getType()==SensorNodeEvent.SENSOR_NODE_ADDED){
				//add new sensor to tree
				try {
					SMLDescription sml = new SMLDescription(controller.listSensor(a, sne.getSourceID()));
					tree.addSensor(a,sml);
				} catch (SALDocumentException e1) {
					e1.printStackTrace();
				} catch (NotFoundException e2) {
					e2.printStackTrace();
				}
			} else if(sne.getType()==SensorNodeEvent.SENSOR_NODE_REMOVED){
				actionPane.displaySensor(null);
				tree.removeSensor(a,sne.getSourceID());
			}

		} else if(e instanceof SensorStateEvent){
			SensorStateEvent sse = (SensorStateEvent) e;
			tree.toggleSensor(a,sse.getSourceID(), sse.getType());
		} else {
			addLog("Inknown event type");
		}
		
	}
	
	@Override
	public final ClientController getController(){
		return controller;
	}	
	
	@Override
	public void addLog(String l){
		if(!l.endsWith("\n"))
			l += "\n";
		log.append(l);
		log.setCaretPosition(log.getDocument().getLength());
	}
	
	@Override
	public void collect(Response r){
		try {
			r.getBytes();
		} catch (SensorControlException e) {
			if(!e.getClass().equals(ClosedStreamException.class))
				addLog("Error: "+e.getMessage());
		}
	}
	
		
	/**
	 * This method is called after the frame has been disposed, when the application exits.
	 * It gives subclasses a chance to do some cleanup before exiting.
	 */
	public void release(){}
	
	/*
	 * method of the ResponseHandler supertype
	 */
	/**
	 * This method is invoked on the handler whenever the handler must close
	 */
	public void close(){}
	
	/**
	 * This method is invoked on the handler whenever the stream ID
	 * for a stream is known.
	 * @param sid
	 */
	public void setStreamID(StreamID sid){}
	
	/**
	 * This method returns the StreamID or null if there is none
	 * @return the StreamID or null if there is none 
	 */
	public StreamID getStreamID(){return null;}

}
