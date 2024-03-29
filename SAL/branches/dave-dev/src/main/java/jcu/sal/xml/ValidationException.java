
package jcu.sal.xml;

import org.xml.sax.SAXParseException;

public class ValidationException extends XmlException {

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ValidationException (SAXParseException[] exceptions) {
		super(exceptionsToMessage(exceptions));
	}

	private static String exceptionsToMessage(SAXParseException[] exceptions) {
		String s = "";

		for (int i = 0; i < exceptions.length; ++i) {
			s += "[";
			s += String.valueOf(exceptions[i].getLineNumber());
			s += ",";
			s += String.valueOf(exceptions[i].getColumnNumber());
			s += "]: ";

			String m = exceptions[i].getMessage();
			if (m.indexOf(": ") != -1) {
				s += m.substring(m.indexOf(": ") + 2);
			} else {
				s += m;
			}

			s += "\n";
		}

		return s;
	}
}
