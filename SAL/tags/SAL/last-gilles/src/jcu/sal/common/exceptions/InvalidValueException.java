package jcu.sal.common.exceptions;

public class InvalidValueException extends SALRunTimeException{

	private static final long serialVersionUID = -8402451349505328822L;

	public InvalidValueException() {}

	public InvalidValueException(String message) {
		super(message);
	}

	public InvalidValueException(Throwable cause) {
		super(cause);
	}

	public InvalidValueException(String message, Throwable cause) {
		super(message, cause);
	}
}
