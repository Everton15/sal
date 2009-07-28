/**
 * 
 */
package jcu.sal.common.exceptions;

/**
 * Exceptions of this type indicate a problem with some configuration directives. These exceptions can be thrown
 * for instance when parsing the XML configuration files, or when parsing an XML document with configuration
 * information.
 * @author gilles
 *
 */
public class ConfigurationException extends SALAgentException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3735357315090803288L;

	/**
	 * This constructor builds a simple ConfigurationException
	 */
	public ConfigurationException() {}

	/**
	 * This constructor builds a simple ConfigurationException with a message
	 * @param message the message
	 */
	public ConfigurationException(String message) {
		super(message);
	}

	/**
	 * This constructor builds a simple ConfigurationException with a cause
	 * @param cause the cause
	 */
	public ConfigurationException(Throwable cause) {
		super(cause);
	}

	/**
	 * This constructor builds a simple ConfigurationException with a message and a cause
	 * @param message the message 
	 * @param cause the cause
	 */
	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}
