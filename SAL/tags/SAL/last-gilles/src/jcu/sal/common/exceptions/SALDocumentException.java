/**
 * 
 */
package jcu.sal.common.exceptions;

/**
 * Exceptions of this type indicate a problem when parsing an XML document, either SML, CML or PCML.
 *  Most likely, parsing a malformed document raised this exception
 * @author gilles
 *
 */
public class SALDocumentException extends SALRunTimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6752578611654576317L;

	/**
	 * This constructor builds a simple SALDocumentException
	 */
	public SALDocumentException() {}

	/**
	 * This constructor builds a simple SALDocumentException with a message
	 * @param message the message
	 */
	public SALDocumentException(String message) {
		super(message);
	}

	/**
	 * This constructor builds a simple SALDocumentException with a cause
	 * @param cause the cause
	 */
	public SALDocumentException(Throwable cause) {
		super(cause);
	}

	/**
	 * This constructor builds a simple SALDocumentException with a message and a cause
	 * @param message the message 
	 * @param cause the cause
	 */
	public SALDocumentException(String message, Throwable cause) {
		super(message, cause);
	}
}
