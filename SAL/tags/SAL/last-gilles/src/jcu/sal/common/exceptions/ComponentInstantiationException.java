/**
 * 
 */
package jcu.sal.common.exceptions;

/**
 * 
 * @author gilles
 *
 */
public class ComponentInstantiationException extends SALAgentException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2403247981073017566L;

	/**
	 * This constructor builds a simple ComponentInstantiationException
	 */
	public ComponentInstantiationException() {}

	/**
	 * This constructor builds a simple ComponentInstantiationException with a message
	 * @param message the message
	 */
	public ComponentInstantiationException(String message) {
		super(message);
	}

	/**
	 * This constructor builds a simple ComponentInstantiationException with a cause
	 * @param cause the cause
	 */
	public ComponentInstantiationException(Throwable cause) {
		super(cause);
	}

	/**
	 * This constructor builds a simple ComponentInstantiationException with a message and a cause
	 * @param message the message 
	 * @param cause the cause
	 */
	public ComponentInstantiationException(String message, Throwable cause) {
		super(message, cause);
	}
}
