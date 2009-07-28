/**
 * 
 */
package jcu.sal.common.exceptions;

/**
 * Exceptions of this class are raised when trying to add a duplicate element to a set.
 * @author gilles
 *
 */
public class AlreadyPresentException extends SALAgentException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6793815822682679178L;

	/**
	 * This constructor builds a simple AlreadyPresent
	 */
	public AlreadyPresentException() {}

	/**
	 * This constructor builds a simple AlreadyPresent with a message
	 * @param message the message
	 */
	public AlreadyPresentException(String message) {
		super(message);
	}

	/**
	 * This constructor builds a simple AlreadyPresent with a cause
	 * @param cause the cause
	 */
	public AlreadyPresentException(Throwable cause) {
		super(cause);
	}

	/**
	 * This constructor builds a simple AlreadyPresent with a message and a cause
	 * @param message the message 
	 * @param cause the cause
	 */
	public AlreadyPresentException(String message, Throwable cause) {
		super(message, cause);
	}
}
