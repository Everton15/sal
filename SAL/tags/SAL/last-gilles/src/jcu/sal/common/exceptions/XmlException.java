package jcu.sal.common.exceptions;

public class XmlException extends Exception {

	private static final long serialVersionUID = -275065599576301441L;

	public XmlException() {
		super();
	}

	public XmlException(String message) {
		super(message);
	}

	public XmlException(String message, Throwable cause) {
		super(message, cause);
	}

	public XmlException(Throwable cause) {
		super(cause);
	}

}