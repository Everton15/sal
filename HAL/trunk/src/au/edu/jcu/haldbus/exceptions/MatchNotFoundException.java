package au.edu.jcu.haldbus.exceptions;

/**
 * This type of exception is internal to the HAL java package.
 * @author gilles
 *
 */
public class MatchNotFoundException extends HalDBusException {

	private static final long serialVersionUID = 1L;

	public MatchNotFoundException() {
		super();
	}

	public MatchNotFoundException(String message) {
		super(message);
	}

	public MatchNotFoundException(Throwable cause) {
		super(cause);
	}

	public MatchNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
