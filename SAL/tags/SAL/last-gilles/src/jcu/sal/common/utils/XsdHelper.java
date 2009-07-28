
package jcu.sal.common.utils;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import javax.xml.transform.stream.StreamSource;

import jcu.sal.common.exceptions.ValidationException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XsdHelper {

	public static void validateString(Schema schema, String xmlString) throws ValidationException {
		ErrorCollector ec = new ErrorCollector();

		Validator validator = schema.newValidator();
		validator.setErrorHandler(ec);

		try {
			validator.validate(new StreamSource(new StringReader(xmlString)));
		} catch (SAXParseException se) {
		} catch (SAXException se) {
			throw new ValidationException("Validation failed: " + se.getMessage(), se);
		} catch (IOException ioe) {
			throw new ValidationException("Validation failed: " + ioe.getMessage(), ioe);
		}

		ec.throwErrors();
	}
}
