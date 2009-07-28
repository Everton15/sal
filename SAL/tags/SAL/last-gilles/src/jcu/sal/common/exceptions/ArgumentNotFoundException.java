package jcu.sal.common.exceptions;

import jcu.sal.common.CommandFactory;

/**
 * This type of exception is thrown by a {@link CommandFactory} object when looking
 * up an argument using its name, which does not exist.
 * @author gilles
 *
 */
public class ArgumentNotFoundException extends SALRunTimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2825845878814559491L;

	public ArgumentNotFoundException() {
		// TODO Auto-generated constructor stub
	}

	public ArgumentNotFoundException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ArgumentNotFoundException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public ArgumentNotFoundException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
