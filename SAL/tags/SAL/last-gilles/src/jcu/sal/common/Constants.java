package jcu.sal.common;

public interface Constants {
	
	/**
	 * the type returned by local SAL agent
	 */
	public static String Local_Agent_type = "local";

	/**
	 * the type returned by RMI SAL agent
	 */
	public static String RMI_Agent_type = "rmi";
	
	/**
	 * The prefix found in all local SAL agent IDs
	 */
	public static String Local_Agent_ID_Prefix = Local_Agent_type+"://";
	
	/**
	 * The prefix found in all RMI SAL agent IDs
	 */
	public static String RMI_Agent_ID_Prefix = RMI_Agent_type+"://";

	/**
	 * The event producer identifier used by the sensor manager
	 */
	public static String SENSOR_MANAGER_PRODUCER_ID = "SensorManager";
	/**
	 * The event producer identifier used by the protocol manager
	 */
	public static String PROTOCOL_MANAGER_PRODUCER_ID = "ProtocolManager";
	/**
	 * The event producer identifier used by sensor state obejcts
	 */
	public static final String SENSOR_STATE_PRODUCER_ID = "SensorState";

}
