/**
 * 
 */
package jcu.sal.common.exceptions;

/**
 * Exceptions of this type are raised when an unsupported command (ie a command the sensor
 * cant execute/doesnt understand) is sent to a sensor.
 * @author gilles
 *
 */
public class UnsupportedCommandException extends SensorControlException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2337063735198938651L;

	/**
	 * This constructor builds a simple UnsupportedCommandException
	 */
	public UnsupportedCommandException() {}

	/**
	 * This constructor builds a simple UnsupportedCommandException with a message
	 * @param message the message
	 */
	public UnsupportedCommandException(String message) {
		super(message);
	}

	/**
	 * This constructor builds a simple UnsupportedCommandException with a cause
	 * @param cause the cause
	 */
	public UnsupportedCommandException(Throwable cause) {
		super(cause);
	}

	/**
	 * This constructor builds a simple UnsupportedCommandException with a message and a cause
	 * @param message the message 
	 * @param cause the cause
	 */
	public UnsupportedCommandException(String message, Throwable cause) {
		super(message, cause);
	}
}
