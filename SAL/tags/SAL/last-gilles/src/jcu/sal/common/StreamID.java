package jcu.sal.common;

import java.io.Serializable;

public class StreamID implements Serializable{
	private static final long serialVersionUID = 6136196943298834985L;
	//the sensor id, command id, protocol id & agent id
	private String sid, cid, pid, aid;
	
	public StreamID(String sid, String cid, String pid){
		this.sid = sid;
		this.cid = cid;
		this.pid = pid;
		aid=null;
	}
	/* ############################################
	 * Any modification to this class must also be done in
	 * jcu.sal.componenents.protocol.LocalStreamID
	 */
	public String getID(){
		return aid+"/"+pid+"/"+sid+"/"+cid;
	}
	
	/* ############################################
	 * Any modification to this class must also be done in
	 * jcu.sal.componenents.protocol.LocalStreamID
	 */
	public String getSID(){
		return sid;
	}
	
	/* ############################################
	 * Any modification to this class must also be done in
	 * jcu.sal.componenents.protocol.LocalStreamID
	 */
	public String getCID(){
		return cid;
	}
	
	/* ############################################
	 * Any modification to this class must also be done in
	 * jcu.sal.componenents.protocol.LocalStreamID
	 */
	public String getPID(){
		return pid;
	}
	
	/* ############################################
	 * Any modification to this class must also be done in
	 * jcu.sal.componenents.protocol.LocalStreamID
	 */
	public String getAgentID(){
		return aid;
	}
	
	/* ############################################
	 * Any modification to this class must also be done in
	 * jcu.sal.componenents.protocol.LocalStreamID
	 */
	public StreamID setAgentID(String id){
		aid= id;
		return this;
	}
	
	public String toString(){
		return getID();
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aid == null) ? 0 : aid.hashCode());
		result = prime * result + ((cid == null) ? 0 : cid.hashCode());
		result = prime * result + ((pid == null) ? 0 : pid.hashCode());
		result = prime * result + ((sid == null) ? 0 : sid.hashCode());
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
		StreamID other = (StreamID) obj;
		if (aid == null) {
			if (other.aid != null)
				return false;
		} else if (!aid.equals(other.aid))
			return false;
		if (cid == null) {
			if (other.cid != null)
				return false;
		} else if (!cid.equals(other.cid))
			return false;
		if (pid == null) {
			if (other.pid != null)
				return false;
		} else if (!pid.equals(other.pid))
			return false;
		if (sid == null) {
			if (other.sid != null)
				return false;
		} else if (!sid.equals(other.sid))
			return false;
		return true;
	}
}
