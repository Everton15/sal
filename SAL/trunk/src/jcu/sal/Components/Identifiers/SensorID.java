/**
 * 
 */
package jcu.sal.Components.Identifiers;

/**
 * this class represents Endpoint names
 * @author gilles
 *
 */
public class SensorID extends AbstractIdentifier{
	
	/** The Protocol ID associated with this sensor **/
	private ProtocolID pid;
	
	/**
	 * Creates a new SensorID with an initial name
	 * @param id the name
	 * @param type the type
	 * @param pid the protocolID
	 */
	public SensorID(String id, String type, ProtocolID pid) {
		super(id, type);
		this.pid = pid; 
	}

	/**
	 * Creates a new SensorID with an initial name and type. THe protocolID member is left empty 
	 * @param id the name
	 * @param type the type
	 */
	public SensorID(String id, String type) {	this(id, type, null); } 
	
	/** (non-Javadoc)
	 * @see jcu.sal.Components.Identifiers.Identifier#toString(java.lang.String)
	 */
	public String toString() {
		return name + "/" + type;
	}
	
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
	public ProtocolID getPid() {
		if(pid == null) {
			System.out.println("************ TRYING TO ACCESS AN EMPTY SENSOR ID ***********");
			return new ProtocolID("","");
		}
		else
			return pid;
	}

	/**
	 * Sets the ProtocolID associated with this Sensor 
	 * @param pid the protocolID
	 */
	public void setPid(ProtocolID pid) {
		if(pid == null || (pid.getName().length()== 0 && pid.getType().length() == 0)) {
			System.out.println("************ TRYING TO SET AN EMPTY PROTOCOL ID ON AN SENSOR***********");
			pid = null;
		}
		else
			this.pid = pid;
	}
}
