/**
 * 
 */
package jcu.sal.common.exceptions;

/**
 * Exceptions of this type are raised when an invalid command sequence is sent to a sensor. For example, such an exception
 * is raised if a stopStream command is sent when the sensor is not streaming data or when two startStream commands are issued
 * in a row 
 * @author gilles
 *
 */
public class InvalidCommandException extends SensorControlException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9153605705934464964L;

	/**
	 * This constructor builds a simple InvalidCommandException
	 */
	public InvalidCommandException() {}

	/**
	 * This constructor builds a simple InvalidCommandException with a message
	 * @param message the message
	 */
	public InvalidCommandException(String message) {
		super(message);
	}

	/**
	 * This constructor builds a simple InvalidCommandException with a cause
	 * @param cause the cause
	 */
	public InvalidCommandException(Throwable cause) {
		super(cause);
	}

	/**
	 * This constructor builds a simple InvalidCommandException with a message and a cause
	 * @param message the message 
	 * @param cause the cause
	 */
	public InvalidCommandException(String message, Throwable cause) {
		super(message, cause);
	}
}
