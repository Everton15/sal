package jcu.sal.components.sensors;

public interface SensorConstants {

	
	/**
	 * Removed, waiting for agent to exit
	 */
	public static final int REMOVED=6;
	
	/**
	 * about to be removed, waiting for the last command/stream to finish
	 */
	public static final int STOPPED=5;
	
	/**
	 * Unplugged
	 */
	public static final int DISCONNECTED=4;
	
	/**
	 * Currently  streaming)
	 */
	public static final int STREAMING=3;
	
	/**
	 * Disabled by user
	 */
	public static final int DISABLED=2;
	
	/**
	 * Associated, plugged-in and not doing anything
	 */
	public static final int IDLE=1;
	
	/**
	 * Not associated with any protocol
	 */
	public static final int UNASSOCIATED=0;

}
