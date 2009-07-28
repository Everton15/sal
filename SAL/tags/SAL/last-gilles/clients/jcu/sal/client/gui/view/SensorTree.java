package jcu.sal.client.gui.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import jcu.sal.client.gui.ClientController;
import jcu.sal.common.Constants;
import jcu.sal.common.agents.SALAgent;
import jcu.sal.common.events.SensorStateEvent;
import jcu.sal.common.exceptions.ArgumentNotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.common.pcml.ProtocolConfigurations;
import jcu.sal.common.sml.SMLDescription;
import jcu.sal.common.sml.SMLDescriptions;

public class SensorTree implements TreeSelectionListener{

	private static final long serialVersionUID = -1407856342401386714L;
	private ClientView view;
	private ClientController controller;
	
	private JTree tree;
	private JPanel mainPane;
	private DefaultTreeModel treeModel;
	private DefaultMutableTreeNode rootNode;
	//private Hashtable<String,ProtocolConfiguration> protocols;
	//private Hashtable<String,SMLDescription> smlDescriptions;
	private List<SALAgent> agents;
	
	
	public SensorTree(ClientView v){

		view = v;
		controller = v.getController();
		mainPane = new JPanel(new GridLayout(1,0));
		rootNode = new DefaultMutableTreeNode(new Context("SAL Client", null));
        treeModel = new DefaultTreeModel(rootNode);
		tree = new JTree(treeModel);
		agents = new Vector<SALAgent>();
		//protocols = new Hashtable<String,ProtocolConfiguration>();
		//smlDescriptions = new Hashtable<String,SMLDescription>();
		
		
		tree.setShowsRootHandles(true);
		tree.addTreeSelectionListener(this);
		tree.setRootVisible(false);
		ToolTipManager.sharedInstance().registerComponent(tree);
		tree.setCellRenderer(new MyRenderer());
		mainPane.add(new JScrollPane(tree));
		mainPane.setMinimumSize(new Dimension(300,500));
	}
	
	public JPanel getPanel(){
		return mainPane;
	}
	
	/**
	 * This method returns a copy of the agents list maintained
	 * in this tree. 
	 * @return a copy of the agents list 
	 */
	public List<SALAgent> getAgents(){
		return new Vector<SALAgent>(agents);
	}
	
	
	/**
	 * This method must be called to add a new agent to this tree
	 * @param a the {@link SALAgent} to be added
	 */
	public void addAgent(SALAgent a){
		DefaultMutableTreeNode n;
		synchronized(rootNode){
			n = addNode(a, rootNode);
			updateTree(n,a);
			agents.add(a);
		}
	}
	
	/**
	 * This method must be called to remove an agent from this tree.
	 * @param a the {@link SALAgent} to be removed.
	 */
	public void removeAgent(SALAgent a){
		synchronized(rootNode){
			removeNode(findAgentNode(a));
			agents.remove(a);
		}
	}

	/**
	 * This method must be called when a new protocol must be added to this tree.
	 * @param a the SAL agent where the protocol should be added.
	 * @param p the {@link ProtocolConfiguration} of the protocol to be added.
	 */
	public void addProtocol(SALAgent a, ProtocolConfiguration p){
		synchronized(rootNode){
			addNode(p, findAgentNode(a));
//			protocols.put(p.getID(),p);
		}
	}
	
	/**
	 * This method must be called when a new protocol must be removed from this tree.
	 * @param a the SAL agent where the protocol should be removed
	 * @param p the protocol ID of the protocol to be removed
	 */
	public void removeProtocol(SALAgent a, String pid){
		synchronized(rootNode){
			removeNode(findProtocolNode(a, pid));
//				protocols.remove(pid);
		}
	}
	
	/**
	 * This method must be called when a new sensor must be added to this tree
	 * @param s the {@link SMLDescription} of the sensor to be added
	 */
	public void addSensor(SALAgent a, SMLDescription s){
		synchronized(rootNode){
			addNode(s, findProtocolNode(a, s.getProtocolName()));
//				smlDescriptions.put(s.getID(),s);
		}
	}
	
	/**
	 * This method must be called when a new sensor must be removed from this tree
	 * @param s the sensor ID of the sensor to be removed
	 */
	public void removeSensor(SALAgent a, String sid){
		synchronized(rootNode){
			removeNode(findSensorNode(a, sid));
//				smlDescriptions.remove(sid);
		}
	}
	
	/**
	 * This method toggles the sensor state
	 * @param a the sal agent
	 * @param sid the sensor id
	 * @param state the new state (SensorStateEvent#SENSOR_STATE_*)
	 */
	public void toggleSensor(SALAgent a, String sid, int state){
		synchronized(rootNode){
			toggleSensor(findSensorNode(a, sid), state);
		}
	}
	
	/**
	 * This method returns the {@link Context} of the currently selected element.
	 * @return the {@link Context} of the currently selected element.
	 */
	public Context getSelectedLabel(){
		if(tree.getLastSelectedPathComponent()==null)
			return null;
		else
			return (Context) ((DefaultMutableTreeNode) tree.getLastSelectedPathComponent()).getUserObject();
	}
	
	/**
	 * This method is called whenever a node in the tree is selected
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		// Get the selected element and tell the RMIClientView to update the display accordingly
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if(node==null)
			return;
		
		view.componentSelected((Context) node.getUserObject());		
	}

	static class MyRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = -4758932006597272509L;
		private static Font enabledFont = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
		private static Font disabledFont = new Font(Font.SANS_SERIF, Font.ITALIC, 11);

		public MyRenderer() {
        	super();
        }

        public Component getTreeCellRendererComponent(
                            JTree tree,
                            Object value,
                            boolean sel,
                            boolean expanded,
                            boolean leaf,
                            int row,
                            boolean hasFocus) {

            super.getTreeCellRendererComponent(
                            tree, value, sel,
                            expanded, leaf, row,
                            hasFocus);
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) value;
            Context l = (Context) n.getUserObject();
            switch(l.getType()){
            	case Context.AGENT_TYPE:
            		if(l.getAgent().getType()==Constants.Local_Agent_type)
            			setIcon(Context.localAgentIcon);
            		else
            			setIcon(Context.remoteAgentIcon);
            		setToolTipText(null);
            		break;
            	case Context.PROTOCOL_TYPE:
            		setIcon(Context.protocolIcon);
            		setToolTipText(null);
            		break;
            	case Context.SENSOR_TYPE:
                    if(l.getSensorState()==SensorStateEvent.SENSOR_STATE_IDLE_CONNECTED) {
                    	setIcon(Context.sensorEnabledIcon);
                        setToolTipText("Sensor enabled");
                        setFont(enabledFont);
                    } else if(l.getSensorState()==SensorStateEvent.SENSOR_STATE_DISABLED) {
                    	setIcon(Context.sensorDisabledIcon);
                    	setToolTipText("Sensor disabled");
                    	setFont(disabledFont);
                    } else if(l.getSensorState()==SensorStateEvent.SENSOR_STATE_DISCONNECTED) {
                    	setIcon(Context.sensorDisconnectedIcon);
                    	setToolTipText("Sensor disconnected");
                    	setFont(disabledFont);
                    } else if(l.getSensorState()==SensorStateEvent.SENSOR_STATE_STREAMING) {
                    	setIcon(Context.sensorStreamingIcon);
                    	setToolTipText("Sensor streaming");
                    	setFont(enabledFont);
                    }
            		break;            
            }

            
            return this;
        }
    }
	
	/*
	 * 
	 * P R I V A T E   M E T H O D S
	 * 
	 */
	
	/**
	 * This method returns the node in this tree representing a protocol matching
	 * the given protocol ID. 
	 * @param pid the protocol ID of the protocol node to be looked up in this tree.
	 * @return the node representing the protocol, or <code>null</code>
	 * if no protocol matches the given ID.
	 */
	@SuppressWarnings("unchecked")
	private DefaultMutableTreeNode findAgentNode(SALAgent a){
		DefaultMutableTreeNode n;
		Enumeration<DefaultMutableTreeNode> e = rootNode.children();
		while(e.hasMoreElements()){
			n = e.nextElement();
			if(((Context) n.getUserObject()).getAgent().equals(a))
				return n;
		}
		
		//TODO: add agent id when implemented
		view.addLog("Cannot find agent node");
		return null;
	}
	
	/**
	 * This method returns the node in this tree representing a protocol matching
	 * the given protocol ID. 
	 * @param pid the protocol ID of the protocol node to be looked up in this tree.
	 * @return the node representing the protocol, or <code>null</code>
	 * if no protocol matches the given ID.
	 */
	@SuppressWarnings("unchecked")
	private DefaultMutableTreeNode findProtocolNode(SALAgent a, String pid){
		ProtocolConfiguration p;
		DefaultMutableTreeNode n;
		Enumeration<DefaultMutableTreeNode> e = findAgentNode(a).children();
		while(e.hasMoreElements()){
			n = e.nextElement();
			p = ((Context) n.getUserObject()).getProtocolConfiguration();
			if(p.getID().equals(pid)){
				return n;
			}
		}
		
		view.addLog("Cannot find protocol node (pid: '"+pid+"')");
		return null;
	}
	
	/**
	 * This method returns the node in this tree representing a sensor matching
	 * the given sensor ID. 
	 * @param sid the sensor ID of the sensor node to be looked up in this tree.
	 * @return the node representing the sensor, or <code>null</code>
	 * if no sensor matches the given ID.
	 */
	@SuppressWarnings("unchecked")
	private DefaultMutableTreeNode findSensorNode(SALAgent a, String sid){
		DefaultMutableTreeNode pn, sn;
		SMLDescription s;
		Enumeration<DefaultMutableTreeNode> sensors, proto = findAgentNode(a).children();
		while(proto.hasMoreElements()){
			pn = proto.nextElement();
			sensors = pn.children();
			while(sensors.hasMoreElements()){
				sn = sensors.nextElement();
				s = ((Context) sn.getUserObject()).getSMLDescription();
				if(s.getID().equals(sid))
					return sn;				
			}
		}
		view.addLog("Cannot find sensor node (sid: '"+sid+"')");
		return null;
	}	

	/**
	 * This method adds a node represented by <code>o</code> to the parent
	 * node <code>parent</code>.
	 * @param o the node to be added {@link SMLDescription} or {@link ProtocolConfiguration} 
	 * @param parent the parent node
	 * @return the newly added node
	 */
	private DefaultMutableTreeNode addNode(Object o, final DefaultMutableTreeNode parent){
		final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new Context(o, (Context) parent.getUserObject()));
		SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				treeModel.insertNodeInto(n, parent, parent.getChildCount());
				tree.scrollPathToVisible(new TreePath(n.getPath()));
			}
		});

		return n;
	}
	
	/**
	 * This method adds a node represented by <code>o</code> to the parent
	 * node <code>parent</code>.
	 * @param o the node to be added {@link SMLDescription} or {@link ProtocolConfiguration} 
	 * @param parent the parent node
	 * @return the newly added node
	 */
	private void removeNode(final MutableTreeNode n){
		SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				treeModel.removeNodeFromParent(n);
			}
		});
	}
	
	/**
	 * This method toggles the state of a sensor node in this tree
	 * @param n the sensor node
	 * @param state the new sensor state (
	 */
	private void toggleSensor(final DefaultMutableTreeNode n, int state){
		Context stl = (Context) n.getUserObject();
		stl.toggleSensorState(state);
		SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				treeModel.reload(n);
			}
		});
	}
	
	/**
	 * This method adds a new SAL agent to the tree &
	 *  calls {@link RMIClientController#listProtocols()} and
	 * {@link RMIClientController#listSensors()}. The lock on
	 * {@link #rootNode} must be obtained prior to calling this method.
	 */
	private void updateTree(DefaultMutableTreeNode parent, SALAgent a){
		SMLDescriptions smls = null;
		ProtocolConfigurations pcml = null;
		DefaultMutableTreeNode proto;
		
		try {
			smls = new SMLDescriptions(controller.listSensors(a));
			pcml = new ProtocolConfigurations(controller.listProtocols(a));
		} catch (SALDocumentException e) {
			view.addLog("Error retrieving the XML sensor /protocol list\n"+e.getMessage());
			e.printStackTrace();
			return;
		}
		
		//add protocols & sensors
		for(ProtocolConfiguration p:pcml.getConfigurations()){
			//add protocol node
			proto = addNode(p, parent);
			//for all sensor under this protocol ...
			try{
			for(SMLDescription s : smls.getDescriptions(p.getID()))
				//add them
				addNode(s,proto);
			} catch (ArgumentNotFoundException e){}
		}
	}
}
