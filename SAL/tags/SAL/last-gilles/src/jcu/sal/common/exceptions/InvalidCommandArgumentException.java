/**
 * 
 */
package jcu.sal.common.exceptions;

/**
 * Exceptions of this type are raised when one of the arguments of a command is invalid. This happens
 * for instance if a value is outside the allowed range, or if the value is incorrect.
 * @author gilles
 *
 */
public class InvalidCommandArgumentException extends SensorControlException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2903113463417953256L;

	/**
	 * This constructor builds a simple InvalidCommandArgumentException
	 */
	public InvalidCommandArgumentException() {}

	/**
	 * This constructor builds a simple InvalidCommandArgumentException with a message
	 * @param message the message
	 */
	public InvalidCommandArgumentException(String message) {
		super(message);
	}

	/**
	 * This constructor builds a simple InvalidCommandArgumentException with a cause
	 * @param cause the cause
	 */
	public InvalidCommandArgumentException(Throwable cause) {
		super(cause);
	}

	/**
	 * This constructor builds a simple InvalidCommandArgumentException with a message and a cause
	 * @param message the message 
	 * @param cause the cause
	 */
	public InvalidCommandArgumentException(String message, Throwable cause) {
		super(message, cause);
	}
}
