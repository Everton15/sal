package au.edu.jcu.haldbus.exceptions;

/**
 * This type of exception is thrown when there is an error communicating with
 * the HAL daemon over DBus.
 * @author gilles
 *
 */
public class DBusException extends HalDBusException {
	private static final long serialVersionUID = 1L;
	
	public DBusException() {
		super();
	}
	
	public DBusException(String message) {
		super(message);
	}

	public DBusException(Throwable cause) {
		super(cause);
	}

	public DBusException(String message, Throwable cause) {
		super(message, cause);
	}

}
