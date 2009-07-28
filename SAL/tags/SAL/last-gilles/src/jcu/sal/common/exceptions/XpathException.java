package jcu.sal.common.exceptions;

/**
 * Exceptions of this type are thrown when an XPATH exception has been caught, most likely
 * because of a programming error 
 * @author gilles
 *
 */
public class XpathException extends SALRunTimeException {

	/**
	 * This constructor builds a simple XpathException
	 */
	public XpathException() {}

	/**
	 * This constructor builds a simple XpathException with a message
	 * @param message the message
	 */
	public XpathException(String message) {
		super(message);
	}

	/**
	 * This constructor builds a simple XpathException with a cause
	 * @param cause the cause
	 */
	public XpathException(Throwable cause) {
		super(cause);
	}

	/**
	 * This constructor builds a simple XpathException with a message and a cause
	 * @param message the message
	 * @param cause the cause
	 */
	public XpathException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 806047643418375831L;

}
