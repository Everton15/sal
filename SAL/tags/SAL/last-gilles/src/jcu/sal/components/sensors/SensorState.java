/**
 * 
 */
package jcu.sal.components.sensors;

import jcu.sal.common.Constants;
import jcu.sal.common.Slog;
import jcu.sal.common.events.SensorStateEvent;
import jcu.sal.components.componentRemovalListener;
import jcu.sal.events.EventDispatcher;

import org.apache.log4j.Logger;

/**
 * @author gilles
 *
 */
public class SensorState {
	
	private static Logger logger = Logger.getLogger(SensorState.class);
	static {Slog.setupLogger(logger);}
	
	private componentRemovalListener l;
	private SensorID i;

	private static final EventDispatcher ev = EventDispatcher.getInstance();
	private int state;
	private int streamingCount;

	public SensorState(SensorID i) { 
		state=SensorConstants.UNASSOCIATED;
		streamingCount=0;
		this.i = i;
	}
	
	public boolean isStarted() {
		return (state!=SensorConstants.UNASSOCIATED);
	}
	
	public boolean isStreaming() {
		return (state==SensorConstants.STREAMING);
	}
	
	public boolean isDisconnectedDisabled() {
		return (state==SensorConstants.DISCONNECTED || state==SensorConstants.DISABLED);
	}
	
	public boolean isAvailableForCommand() {
		return (state==SensorConstants.IDLE || state==SensorConstants.STREAMING);
	}
	
	public boolean remove(componentRemovalListener c){
		synchronized (this) {
			if(state==SensorConstants.STREAMING) { state=SensorConstants.STOPPED; l =c;} 
			else { state=SensorConstants.REMOVED; c.componentRemovable(i);}
			return true;
		}
	}
	

	public boolean startStream(){
		synchronized (this) {
			switch(state){
			case SensorConstants.IDLE:
				state=SensorConstants.STREAMING;
				ev.queueEvent(new SensorStateEvent(SensorStateEvent.SENSOR_STATE_STREAMING,i.getName(),Constants.SENSOR_STATE_PRODUCER_ID));
			case SensorConstants.STREAMING:
				streamingCount++;
				return true;
			default:
				return false;
			}
		}
	}
	
	public boolean stopStream(){
		synchronized (this) {
			switch(state){
				case SensorConstants.STREAMING:
					if(--streamingCount==0){
						state=SensorConstants.IDLE;
						ev.queueEvent(new SensorStateEvent(SensorStateEvent.SENSOR_STATE_IDLE_CONNECTED,i.getName(),Constants.SENSOR_STATE_PRODUCER_ID));
					}
					return true;
				case SensorConstants.STOPPED:
					state = SensorConstants.REMOVED; 
					l.componentRemovable(i);
					return true;
				default:
					return false;
			}
		}
	}

	public boolean disable(){
		synchronized (this) {
			switch(state){
			case SensorConstants.IDLE:
			case SensorConstants.DISCONNECTED:
			case SensorConstants.STREAMING:
				state=SensorConstants.DISABLED;
				ev.queueEvent(new SensorStateEvent(SensorStateEvent.SENSOR_STATE_DISABLED,i.getName(),Constants.SENSOR_STATE_PRODUCER_ID));
				return true;
			default:
				return false;
			}
		}
	}
	
	public boolean enable(){
		synchronized (this) {
			switch(state){
			case SensorConstants.DISABLED:
				ev.queueEvent(new SensorStateEvent(SensorStateEvent.SENSOR_STATE_IDLE_CONNECTED,i.getName(),Constants.SENSOR_STATE_PRODUCER_ID));
			case SensorConstants.UNASSOCIATED:
				state=SensorConstants.IDLE;
				return true;
			default:
				return false;
			}
		}
	}
	
	public boolean disconnect(){
		synchronized (this) {
			switch(state){
			case SensorConstants.IDLE:
			case SensorConstants.STREAMING:
			case SensorConstants.UNASSOCIATED:
				state=SensorConstants.DISCONNECTED;
				ev.queueEvent(new SensorStateEvent(SensorStateEvent.SENSOR_STATE_DISCONNECTED,i.getName(),Constants.SENSOR_STATE_PRODUCER_ID));
				return true;
			default:
				return false;
			}
		}
	}

	public boolean reconnect(){
		synchronized (this) {
			switch(state){
			case SensorConstants.DISCONNECTED:
				state=SensorConstants.IDLE;
				ev.queueEvent(new SensorStateEvent(SensorStateEvent.SENSOR_STATE_IDLE_CONNECTED,i.getName(),Constants.SENSOR_STATE_PRODUCER_ID));
				return true;
			case SensorConstants.IDLE:
			case SensorConstants.DISABLED:
			case SensorConstants.STREAMING:
				return true; 
			default:
				return false;
			}
		}
	}
	
	public String toString() {
		synchronized(this) {
			switch(state) {
			case SensorConstants.UNASSOCIATED:
				return "UNASSOCIATED";
			case SensorConstants.IDLE:
				return "IDLE";
			case SensorConstants.DISABLED:
				return "DISABLED";
			case SensorConstants.DISCONNECTED:
				return "DISCONNECTED";
			case SensorConstants.STOPPED:
				return "STOPPED";
			case SensorConstants.REMOVED:
				return "REMOVED";
			}
		}
		return "State: unknown state...";
	}
	
	public void dumpState() {
			logger.debug("Current sensor state: "+toString() );
	}
}
