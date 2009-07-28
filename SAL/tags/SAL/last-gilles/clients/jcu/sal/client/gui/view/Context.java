package jcu.sal.client.gui.view;

import javax.swing.ImageIcon;

import jcu.sal.common.agents.SALAgent;
import jcu.sal.common.cml.CMLDescriptions;
import jcu.sal.common.events.SensorStateEvent;
import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.common.sml.SMLDescription;

/**
 * Context objects are organised in a tree-like fashion and are 
 * used to identify the context associated with an object in the GUI. A GUI object here refers
 * to either a SAL agent, a protocol or a sensor.
 * There are four types of elements, each can be associated with a specific object type (referred to
 * as its context):
 * root node -> no context
 * agent -> context of type {@link Object}
 * protocol -> context of type {@link ProtocolConfiguration}
 * sensor -> context of type  {@link SMLDescription} and {@link CMLDescriptions}
 * 
 * The Context class store the context object (as per above) and a reference to its parent context.
 * That way, a single Context can be used to retrieve data associated in higher levels. Example:
 * 
 * A Context object for a sensor holds a reference to the associated {@link SMLDescription} for that sensor.
 * This Context object can also be used to retrieve the {@link ProtocolConfiguration} object associated with 
 * the protocol, as well as the agent object.
 * 
 * Context objects also define how matching objects are displayed in the tree.
 *   
 * @author gilles
 *
 */
public class Context {
	public static final int ROOT_NODE= 0;
	public static final int AGENT_TYPE = 1;
	public static final int PROTOCOL_TYPE = 2;
	public static final int SENSOR_TYPE = 3;
	
	public static ImageIcon sensorEnabledIcon;
	public static ImageIcon sensorStreamingIcon;
	public static ImageIcon sensorDisabledIcon;
	public static ImageIcon sensorDisconnectedIcon;
	public static ImageIcon protocolIcon;
	public static ImageIcon localAgentIcon;
	public static ImageIcon remoteAgentIcon;
	
	static{
		//load icons
		sensorEnabledIcon = createImageIcon("resources/icons/Symbols-Tips-15x15.png");
		sensorDisabledIcon = createImageIcon("resources/icons/Symbols-Error-15x15.png");
		sensorDisconnectedIcon = createImageIcon("resources/icons/eject-15x15.png");
		sensorStreamingIcon = createImageIcon("resources/icons/record-15x15.png");
		protocolIcon = createImageIcon("resources/icons/folder-orange-scanners-cameras-26x26.png");
		localAgentIcon = createImageIcon("resources/icons/Hardware-My-Computer-1-26x26.png");
		remoteAgentIcon = createImageIcon("resources/icons/Network-Remote-Desktop-26x26.png");
	}
	
	
	
	/**
	 * the type of object this context refers to, see constants above
	 */
	private int type;
	
	/**
	 * The object associated with this context
	 */
	private Object obj;
	
	/**
	 * The {@link CMLDescriptions} object
	 */
	private CMLDescriptions cmls;
	
	
	/**
	 * The string representation shown in the Tree
	 */
	private String stringVal;
	
	/**
	 * the parent context of this context
	 */
	private Context parent;
	
	private int state;
	
	
	/**
	 * This method builds a Context object.
	 * If o is of type {@link SALagent}, this context refers to an agent and <code>p</code> must be 
	 * of type {@link #{ROOT_NODE}, 
	 * If o is of type {@link String}, this context refers to the root node and <code>p</code> must be <code>null</code>.
	 * If o is of type {@link ProtocolConfiguration}, this context refers to a protocol and <code>p</code> must be 
	 * of type {@link #AGENT_TYPE}, 
	 * If o is of type {@link SMLDescription}, this context refers to a sensor and <code>p</code> must be 
	 * of type {@link #PROTOCOL_TYPE}, 
	 * Otherwise, a {@link SALRunTimeException} is thrown. 
	 * @param o a {@link ProtocolConfiguration}, {@link SMLDescription}, {@link String}
	 * or {@link SALagent} to build the context from.
	 * @param p the parent {@link Context} of this {@link Context}.
	 * @throws SALRunTimeException if the given parent is not of the right type for this Context.
	 */
	public Context(Object o, Context p){
		if(o instanceof String){
			//root node
			stringVal = (String) o;
			type = ROOT_NODE;
			parent = null;
		} else if(o instanceof ProtocolConfiguration){
			//protocol context
			if(p.getType()!=AGENT_TYPE)
				throw new SALRunTimeException("Inavlid parent context (not an agent) for protocol context");
			
			type = PROTOCOL_TYPE;
			stringVal = new String(((ProtocolConfiguration) o).getID());
			parent = p;
		} else if (o instanceof SMLDescription){
			//sensor context
			if(p.getType()!=PROTOCOL_TYPE)
				throw new SALRunTimeException("Inavlid parent context (not a protocol) for sensor context");
			
			type = SENSOR_TYPE;
			stringVal = new String(((SMLDescription) o).getSensorAddress()+
					" - "+((SMLDescription) o).getID());
			parent = p;
			state = SensorStateEvent.SENSOR_STATE_IDLE_CONNECTED;
		} else if (o instanceof SALAgent){
			//agent context
			if(p.getType()!=ROOT_NODE)
				throw new SALRunTimeException("Inavlid parent context (not the root node) for agent context");
			stringVal = ((SALAgent) o).getID();
			type = AGENT_TYPE;
			parent = p;
		} else
			throw new SALRunTimeException("Inavlid context object");
		obj = o;
		//dump();
	}
	
	/**
	 * This method returns the object this context refers to:
	 * {@link #SENSOR_TYPE}, {@link #PROTOCOL_TYPE}, {@link #AGENT_TYPE} or {@link #ROOT_NODE}
	 * @return the type of object this context refers to
	 */
	public int getType(){
		return type;
	}
	
	/**
	 * This method returns the
	 * {@link SMLDescription} object associated with this context.
	 * @return the {@link SMLDescription} object associated with this label.
	 * @throws SALRunTimeException if this context does not refer to a Sensor
	 */
	public SMLDescription getSMLDescription(){
		if(type >= SENSOR_TYPE)
			return (SMLDescription) obj;
		
		throw new SALRunTimeException("This context does not refer to a sensor");
	}

	/**
	 * This method finds and returns the
	 * {@link ProtocolConfiguration} object associated with this context, or one of its parents.
	 * @return the {@link ProtocolConfiguration} object associated with this context or one of its parents.
	 * @throws SALRunTimeException if this context does not refer to a sensor or a protocol
	 */
	public ProtocolConfiguration getProtocolConfiguration(){
		if(type > PROTOCOL_TYPE)
			return parent.getProtocolConfiguration();
		else if (type==PROTOCOL_TYPE)
			return (ProtocolConfiguration) obj;

		throw new SALRunTimeException("This context does not refer to a protocol or a sensor");
	}
	
	/**
	 * This method finds and returns the
	 * {@link SALAgent} associated with this context, or one of its parents.
	 * @return the {@link SALAgent} associated with this context or one of its parents.
	 * @throws SALRunTimeException if this context does not refer to a sensor or a protocol
	 */
	public SALAgent getAgent(){
		if(type > AGENT_TYPE)
			return parent.getAgent();
		else if (type==AGENT_TYPE)
			return (SALAgent) obj;

		throw new SALRunTimeException("This context does not refer to an agent, a protocol or a sensor");		
	}
	
	public CMLDescriptions getCMLDescriptions(){
		if(type!=SENSOR_TYPE)
			throw new SALRunTimeException("This context does not refer to a sensor");
		
		return cmls;
	}

	public void setCMLDescriptions(CMLDescriptions  c){
		if(type!=SENSOR_TYPE)
			throw new SALRunTimeException("This context does not refer to a sensor");
		else if(cmls!=null && cmls!=c)
			throw new SALRunTimeException("Already have a CMLDescription for this sensor");
		
		cmls = c;
	}
	
	
	public void toggleSensorState(int s){
		if(type!=SENSOR_TYPE)
			throw new SALRunTimeException("This context does not refer to a sensor");
		state = s;
	}
	
	public int getSensorState(){
		if(type!=SENSOR_TYPE)
			throw new SALRunTimeException("This context does not refer to a sensor");
		return state;
	}

	public void dump(){
		dump(0);
	}
	public void dump(int rank){
		if(rank==0){
			System.out.println("===============\nDump for context "+hashCode());
		}
		for(int i = 0; i<rank;i++)
			System.out.print("\t");
		System.out.println("Type: "+(type==ROOT_NODE?"Root node":(type==AGENT_TYPE?"Agent node":(type==PROTOCOL_TYPE?"Protocol node":"Sensor node"))) 
				+"Value: "+stringVal );

		if(type!=ROOT_NODE){
			parent.dump(rank+1);
		}
		
	}
	
	/**
	 * This method load an icon given its path relative to the
	 * directory where this class is
	 * @param path its image file path relative to the directory where this class is
	 * @return an ImageIcon
	 * @throws SALRunTimeException if the image cannot be loaded
	 */
	public static ImageIcon createImageIcon(String path) {
	    java.net.URL imgURL = Context.class.getClassLoader().getResource(path);
	    if (imgURL != null)
	        return new ImageIcon(imgURL);
	  
	    throw new SALRunTimeException("Can load image "+path);
	}

		
	
	@Override
	public String toString(){
		return stringVal;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((obj == null) ? 0 : obj.hashCode());
		result = prime * result
				+ ((stringVal == null) ? 0 : stringVal.hashCode());
		result = prime * result + type;
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
		Context other = (Context) obj;
		if (this.obj == null) {
			if (other.obj != null)
				return false;
		} else if (!this.obj.equals(other.obj))
			return false;
		if (stringVal == null) {
			if (other.stringVal != null)
				return false;
		} else if (!stringVal.equals(other.stringVal))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}
