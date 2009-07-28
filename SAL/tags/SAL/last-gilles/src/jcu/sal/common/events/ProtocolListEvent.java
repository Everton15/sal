package jcu.sal.common.events;


public class ProtocolListEvent extends Event {

	private static final long serialVersionUID = 4394564002895838509L;
	public static final int PROTOCOL_ADDED = 1;
	public static final int PROTOCOL_REMOVED = 2;
	
	public ProtocolListEvent(int t, String sid, String p){
		super(t, sid, p);
	}

	public ProtocolListEvent(int t, String sid, String doc, String p) {
		super(t, sid, doc, p);
	}
	
	public String toString(){
		if(type==PROTOCOL_ADDED) return super.toString() + " New protocol added";
		else return super.toString() + " Existing protocol removed";
	}

}
