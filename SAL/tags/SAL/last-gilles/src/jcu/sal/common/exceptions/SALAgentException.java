/**
 * 
 */
package jcu.sal.common.exceptions;

/**
 * All checked exceptions thrown by a SAL Agent are subclasses of this class
 * @author gilles
 *
 */
public class SALAgentException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2525840761230988505L;

	/**
	 * This constructor builds a simple SAL Agent Exception
	 */
	public SALAgentException() {}

	/**
	 * This constructor builds a simple SAL Agent Exception with a message
	 * @param message the message
	 */
	public SALAgentException(String message) {
		super(message);
	}

	/**
	 * This constructor builds a simple SAL Agent Exception with a cause
	 * @param cause the cause
	 */
	public SALAgentException(Throwable cause) {
		super(cause);
	}

	/**
	 * This constructor builds a simple SAL Agent Exception with a message and a cause
	 * @param message the message
	 * @param cause the cause
	 */
	public SALAgentException(String message, Throwable cause) {
		super(message, cause);
	}

}
