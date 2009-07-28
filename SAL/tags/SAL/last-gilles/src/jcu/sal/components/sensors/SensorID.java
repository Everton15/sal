/**
 * 
 */
package jcu.sal.components.sensors;

import jcu.sal.components.AbstractIdentifier;
import jcu.sal.components.protocols.ProtocolID;


/**
 * this class represents Endpoint names
 * @author gilles
 *
 */
public class SensorID extends AbstractIdentifier{
	
	/** The AbstractProtocol ID associated with this sensor **/
	private ProtocolID pid;
	
	/**
	 * Creates a new SensorID with an initial name
	 * @param id the name
	 * @param type the type
	 * @param pid the protocolID
	 */
	public SensorID(String id, ProtocolID pid) {
		super(id);
		this.pid = pid; 
	}
	
	/**
	 * Creates a new SensorID with an initial name. THe protocolID member is left empty 
	 * @param id the name
	 */
	public SensorID(String id) {	this(id,null); } 
	
	/**
	 * Return the name of the ProtocolID associated with this Sensor 
	 * @return the protocolID
	 */
	public String getPIDName() {
		return getPid().getName();
	}
	
	/**
	 * Return the type of the ProtocolID associated with this Sensor 
	 * @return the protocolID
	 */
	public String getPIDtype() {
		return getPid().getName();
	}

	/**
	 * Return the ProtocolID associated with this Sensor 
	 * @return the protocolID
	 */
	private ProtocolID getPid() {
		if(pid == null) {
			//System.out.println("************ TRYING TO ACCESS AN EMPTY PROTOCOL ID ***********");
			return new ProtocolID("");
		}
		else
			return pid;
	}

	/**
	 * Sets the ProtocolID associated with this Sensor 
	 * @param pid the protocolID
	 */
	void setPid(ProtocolID pid) {
		if(pid == null || (pid.getName().length()== 0)) {
			//System.out.println("************ TRYING TO SET AN EMPTY PROTOCOL ID ON AN SENSOR***********");
			pid = null;
		}
		else
			this.pid = pid;
	}
}
