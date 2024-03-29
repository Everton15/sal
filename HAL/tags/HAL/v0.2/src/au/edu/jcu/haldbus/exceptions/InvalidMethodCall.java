package au.edu.jcu.haldbus.exceptions;

public class InvalidMethodCall extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidMethodCall() {
		super();
	}

	public InvalidMethodCall(String message) {
		super(message);
	}

	public InvalidMethodCall(Throwable cause) {
		super(cause);
	}

	public InvalidMethodCall(String message, Throwable cause) {
		super(message, cause);
	}

}
