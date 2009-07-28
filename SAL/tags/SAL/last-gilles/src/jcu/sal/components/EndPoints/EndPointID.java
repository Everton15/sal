/**
 * 
 */
package jcu.sal.components.EndPoints;

import jcu.sal.components.AbstractIdentifier;
import jcu.sal.components.protocols.ProtocolID;

/**
 * this class represents Endpoint IDs. Two EndPointIDs are equal if their name is equal 
 * @author gilles
 *
 */
public class EndPointID extends AbstractIdentifier{
	
	/** The AbstractProtocol ID associated with this EndPoint **/
	private ProtocolID pid;
	
	/**
	 * Creates a new EndPointID with an initial name, type and ProtocolID
	 * @param id the name
	 * @param type the type
	 * @param pid the protocolID
	 */
	public EndPointID(String id, ProtocolID pid) {
		super(id);
		this.pid = pid; 
	}
	
	/**
	 * Creates a new EndPointID with an initial name and type. THe protocolID member is left empty 
	 * @param id the name
	 * @param type the type
	 */
	public EndPointID(String id) {	this(id, null); } 
	
	/**
	 * Return the name of the ProtocolID associated with this EndPoint 
	 * @return the protocolID
	 */
	public String getPIDName() {
		return getPid().getName();
	}
	
	/**
	 * Return the type of the ProtocolID associated with this EndPoint 
	 * @return the protocolID
	 */
	public String getPIDtype() {
		return getPid().getName();
	}

	/**
	 * Sets the ProtocolID associated with this EndPoint 
	 * @param pid the protocolID
	 */
	void setPid(ProtocolID pid) {
		if(pid == null || (pid.getName().length()== 0)) {
			System.out.println("************ TRYING TO SET AN EMPTY PROTOCOL ID ON AN ENDPOINT***********");
			pid = null;
		}
		else
			this.pid = pid;
	}
	
	/**
	 * Return the ProtocolID associated with this EndPoint 
	 * @return the protocolID
	 */
	private ProtocolID getPid() {
		if(pid == null) {
			System.out.println("************ TRYING TO ACCESS AN EMPTY PROTOCOL ID ***********");
			return new ProtocolID("");
		}
		else
			return pid;
	}
}
