package jcu.sal.components.protocols;

import jcu.sal.common.StreamID;

public class LocalStreamID {
	private String cid, sid, pid;
	
	public LocalStreamID(StreamID stream){
		pid = stream.getPID();
		cid = stream.getCID();
		sid = stream.getSID();
	}
	
	public LocalStreamID(String s, String c, String p){
		pid = p;
		cid = c;
		sid = s;
	}
	
	/* ############################################
	 * Any modification to this class must also be done in
	 * jcu.sal.common.StreamID
	 */
	public String getID(){
		return pid+"/"+sid+"/"+cid;
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		LocalStreamID other = (LocalStreamID) obj;
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
