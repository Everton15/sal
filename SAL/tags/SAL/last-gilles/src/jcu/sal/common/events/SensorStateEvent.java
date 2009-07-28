package jcu.sal.common.events;


public class SensorStateEvent extends Event {

	private static final long serialVersionUID = 6308401724433999891L;
	public static final int SENSOR_STATE_IDLE_CONNECTED = 1;
	public static final int SENSOR_STATE_DISCONNECTED = 2;
	public static final int SENSOR_STATE_STREAMING = 3;
	public static final int SENSOR_STATE_DISABLED = 4;
	
	public SensorStateEvent(int t, String sid, String p) {
		super(t, sid, p);
	}

	public SensorStateEvent(int t, String sid, String doc, String p) {
		super(t, sid, doc, p);
	}
	
	public String toString(){
		if(type==SENSOR_STATE_IDLE_CONNECTED) 
			return super.toString() + " Sensor connected & idle";
		else if(type==SENSOR_STATE_STREAMING) 
			return super.toString() + " Sensor streaming";
		else if(type==SENSOR_STATE_DISABLED) 
			return super.toString() + " Sensor disabled";
		else
			return super.toString() + " Sensor disconnected";
	}

}
