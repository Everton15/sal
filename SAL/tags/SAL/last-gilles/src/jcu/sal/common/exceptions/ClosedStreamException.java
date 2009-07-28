/**
 * 
 */
package jcu.sal.common.exceptions;

/**
 * Exceptions of this type are usually raised by a protocol returning a stream of results, and indicate that
 * the stream of data is now closed. 
 * @author gilles
 *
 */
public class ClosedStreamException extends SensorControlException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8518604311536055309L;

	/**
	 * This constructor builds a simple ClosedStreamException
	 */
	public ClosedStreamException() {}

	/**
	 * This constructor builds a simple ClosedStreamException with a message
	 * @param message the message
	 */
	public ClosedStreamException(String message) {
		super(message);
	}

	/**
	 * This constructor builds a simple ClosedStreamException with a cause
	 * @param cause the cause
	 */
	public ClosedStreamException(Throwable cause) {
		super(cause);
	}

	/**
	 * This constructor builds a simple ClosedStreamException with a message and a cause
	 * @param message the message 
	 * @param cause the cause
	 */
	public ClosedStreamException(String message, Throwable cause) {
		super(message, cause);
	}
}
