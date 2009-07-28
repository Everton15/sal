package jcu.sal.common.events;


public class SensorNodeEvent extends Event {
	private static final long serialVersionUID = -7000825364492820746L;
	public static final int SENSOR_NODE_ADDED = 1;
	public static final int SENSOR_NODE_REMOVED = 2; 

	
	public SensorNodeEvent(int t, String sid, String p){
		super(t, sid, p);
	}

	public SensorNodeEvent(int t, String sid, String doc, String p){
		super(t, sid, doc, p);
	}
	
	public String toString(){
		if(type==SENSOR_NODE_ADDED) return super.toString() + " New sensor added";
		else return super.toString() + " Existing sensor removed";
	}

}
