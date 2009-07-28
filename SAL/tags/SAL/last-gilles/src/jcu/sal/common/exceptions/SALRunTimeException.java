/**
 * 
 */
package jcu.sal.common.exceptions;

/**
 * Exception extending this class are unchecked exceptions, thrown because of a programming error
 * @author gilles
 *
 */
public class SALRunTimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7347243698698993627L;

	/**
	 * This constructor builds a simple SAL run time exception
	 */
	public SALRunTimeException() {}

	/**
	 * This constructor builds a simple SAL run time exception with a message
	 * @param message the message
	 */
	public SALRunTimeException(String message) {
		super(message);
	}

	/**
	 * This constructor builds a simple SAL run time exception with a cause
	 * @param cause the cause
	 */
	public SALRunTimeException(Throwable cause) {
		super(cause);
	}

	/**
	 * This constructor builds a simple SAL run time exception with a message and a cause
	 * @param message the message
	 * @param cause the cause
	 */
	public SALRunTimeException(String message, Throwable cause) {
		super(message, cause);
	}

}
