package jcu.sal.common.exceptions;

/**
 * Exceptions of this type are raised whenever a lookup for an object fails because
 * the object cant be found. Situations where this kind of exceptions will be raised:<br>
 * A lookup for an attribute in an XML document given its name fails.<br>
 * A lookup for an object in a table fails.
 * @author gilles
 *
 */
public class NotFoundException extends SALAgentException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7614752850659593217L;

	/**
	 * This constructor builds a simple NotFoundException
	 */
	public NotFoundException() {}

	/**
	 * This constructor builds a simple NotFoundException with a message
	 * @param message the message
	 */
	public NotFoundException(String message) {
		super(message);
	}

	/**
	 * This constructor builds a simple NotFoundException with a cause
	 * @param cause the cause
	 */
	public NotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * This constructor builds a simple NotFoundException with a message and a cause
	 * @param message the message 
	 * @param cause the cause
	 */
	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
