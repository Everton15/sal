/**
 * 
 */
package jcu.sal.common.exceptions;

/**
 * Exceptions of this type are raised when communication with a sensor failed at some point. It does not imply
 * that the sensor has become unavailable. It simply means there was an error while talking to the sensor.  
 * @author gilles
 *
 */
public class SensorIOException extends SensorControlException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -303593879124378509L;

	/**
	 * This constructor builds a simple SensorIOException
	 */
	public SensorIOException() {}

	/**
	 * This constructor builds a simple SensorIOException with a message
	 * @param message the message
	 */
	public SensorIOException(String message) {
		super(message);
	}

	/**
	 * This constructor builds a simple SensorIOException with a cause
	 * @param cause the cause
	 */
	public SensorIOException(Throwable cause) {
		super(cause);
	}

	/**
	 * This constructor builds a simple SensorIOException with a message and a cause
	 * @param message the message 
	 * @param cause the cause
	 */
	public SensorIOException(String message, Throwable cause) {
		super(message, cause);
	}
}
