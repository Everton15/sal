
package jcu.sal.xml;

import java.util.ArrayList;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class ErrorCollector implements ErrorHandler {

	private ArrayList<SAXParseException> errors;

	public ErrorCollector() {
		errors = new ArrayList<SAXParseException>();
	}

	public void throwErrors() throws ValidationException {
		if (errors.size() != 0) {
			throw new ValidationException(errors.toArray(new SAXParseException[0]));
		}
	}

	public void error(SAXParseException e) {
		errors.add(e);
	}

	public void fatalError(SAXParseException e) {
		errors.add(e);
	}

	public void warning(SAXParseException e) {
		errors.add(e);
	}
}
