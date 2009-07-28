package jcu.sal.common.events;

import java.io.Serializable;

public abstract class Event implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1860045513950384541L;
	final protected int type;
	final protected String sourceID;
	final protected String sourceDocument;
	final protected String producer;
	
	protected Event(int t, String sid, String p){
		this(t, sid,  null, p);
	}
	
	protected Event(int t, String sid, String doc, String p){
		type = t;
		sourceID = sid;
		producer = p;
		sourceDocument = doc;
	}

	/**
	 * This method returns the identifier of the producer of this event, which
	 * is different from the source of the event. 
	 * @return the identifier of the producer of this event
	 */
	public final String getProducer() {
		return producer;
	}

	/**
	 * Dont use yet.
	 * @return
	 */
	public final String getSourceDocument() {
		return sourceDocument;
	}
	
	/**
	 * This method returns the identifier of the source of this event.
	 * If a new sensor has been added, the source identifier is the sensor ID.
	 * @return
	 */
	public final String getSourceID() {
		return sourceID;
	}

	public final int getType() {
		return type;
	}
	
	public String toString(){
		return "Event from producer '"+producer+"' about source '"+sourceID+"':";
	}
}
