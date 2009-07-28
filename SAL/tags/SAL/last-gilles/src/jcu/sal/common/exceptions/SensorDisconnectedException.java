/**
 * 
 */
package jcu.sal.common.exceptions;

/**
 * Exceptions of this type are raised when a command was sent to a sensor, but the sensor is unavailable to answer the
 * request. Typically, when such an exception is raised, the sensor state will be changed to disconnected.
 * @author gilles
 *
 */
public class SensorDisconnectedException extends SensorControlException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 151134249255180212L;

	/**
	 * This constructor builds a simple SensorDisconnectedException
	 */
	public SensorDisconnectedException() {}

	/**
	 * This constructor builds a simple SensorDisconnectedException with a message
	 * @param message the message
	 */
	public SensorDisconnectedException(String message) {
		super(message);
	}

	/**
	 * This constructor builds a simple SensorDisconnectedException with a cause
	 * @param cause the cause
	 */
	public SensorDisconnectedException(Throwable cause) {
		super(cause);
	}

	/**
	 * This constructor builds a simple SensorDisconnectedException with a message and a cause
	 * @param message the message 
	 * @param cause the cause
	 */
	public SensorDisconnectedException(String message, Throwable cause) {
		super(message, cause);
	}
}
