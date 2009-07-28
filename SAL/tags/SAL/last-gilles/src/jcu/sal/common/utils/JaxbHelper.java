package jcu.sal.common.utils;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.ValidationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class JaxbHelper {

	/**
	 * List of packages with JAXB interfaces
	 */
	private static final String[] XML_PACKAGES = {
		"jcu.sal.config.plugins.xml",
		"jcu.sal.common.cml.xml"
	};

	private static Map<String,JAXBContext> contexts = null;
	private static Map<String,Marshaller> marshallers = null;
	private static Map<String,Unmarshaller> unmarshallers = null;

	private static synchronized void prepareJAXB() throws JAXBException {
		if (contexts == null) {
			contexts = new Hashtable<String, JAXBContext>();
			marshallers= new Hashtable<String, Marshaller>();
			unmarshallers= new Hashtable<String, Unmarshaller>();
			
			for(String pkg: XML_PACKAGES){
				try {
					contexts.put(pkg, JAXBContext.newInstance(pkg));
					marshallers.put(pkg, contexts.get(pkg).createMarshaller());				
					unmarshallers.put(pkg, contexts.get(pkg).createUnmarshaller());
				} catch (Exception e){}
				//the previous try /catch is used because we are trying to create
				//context/marshaller/... for different packages. Some exists in both
				//clients and agents, and some exist only in agents, and unless
				//we have the try catch, clients will simply fails.
				
				
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Unmarshaller getUnmarshaller(Class c){
		Unmarshaller m = unmarshallers.get(c.getPackage().getName()); 
		if(m==null)
			throw new SALDocumentException("Cannot find package for class "+c.getName() +" package: "+c.getPackage().getName());
		return m;
	}
	
	public static Marshaller getMarshaller(Object o){
		Marshaller m = marshallers.get(o.getClass().getPackage().getName()); 
		if(m==null)
			throw new SALDocumentException("Cannot find package for class "+o.getClass().getName() +" package: "+o.getClass().getPackage().getName());
		return m;
	}


	public static String toXmlString(Object o) throws SALDocumentException {
		Marshaller m;
		try {
			if (o == null) {
				throw new SALDocumentException("Cannot serialize null to an xml string.");
			}
			
			prepareJAXB();

			StringWriter writer = new StringWriter();
			m = getMarshaller(o);
			m.marshal(o, new StreamResult(writer));
			String xmlString = writer.toString();

			validateString(getSchema(o.getClass()), xmlString);
			
			return XMLhelper.toString(xmlString);
		} catch (Exception je) {
			throw new SALDocumentException(je);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T fromXmlString(Class<T> c, String xmlString) throws SALDocumentException {
		Unmarshaller m;
		try {
			if (xmlString == null) {
				throw new SALDocumentException("Cannot deserialize null to an object.");
			}

			if (xmlString.length() == 0) {
				throw new SALDocumentException("Cannot deserialize an empty string.");
			}

			validateString(xmlString);
			prepareJAXB();
			
			m = getUnmarshaller(c);

			T o = (T) m.unmarshal(new StreamSource(new StringReader(xmlString)));
			XsdHelper.validateString(getSchema(c), xmlString);

			return o;
		} catch (Exception je) {
			throw new SALDocumentException(je);
		}
	}

	public static void toFile(Object o, File f) throws SALDocumentException {
		stringToFile(toXmlString(o), f);
	}

	public static <T> T fromFile(Class<T> c,File f) throws SALDocumentException {
		return fromXmlString(c, fileToString(f));
	}

	private static void stringToFile(String s, File f) throws SALDocumentException {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(f));
			out.write(s);
			out.close();
		} catch (IOException ioe) {
			throw new SALDocumentException(ioe);
		}
	}

	private static String fileToString(File f) throws SALDocumentException {
		String result = "";

		try {
			BufferedReader in = new BufferedReader(new FileReader(f));
			String str;

			while ((str = in.readLine()) != null) {
				result += str;
			}

			in.close();
		} catch (IOException ioe) {
			throw new SALDocumentException(ioe);
		}

		return result;
	}

	public static void validate(Object o) throws ValidationException {
		validateString(getSchema(o.getClass()), toXmlString(o));
	}

	public static void validateString(Schema schema, String xmlString) throws ValidationException {
		validateString(xmlString);
		XsdHelper.validateString(schema, xmlString);
	}

	public static void validateString(String xmlString) throws ValidationException {
		ErrorCollector ec = new ErrorCollector();

		try {
			XMLReader r = XMLReaderFactory.createXMLReader();
			r.setErrorHandler(ec);
			r.parse(new InputSource(new StringReader(xmlString)));
		} catch (SAXParseException se) {
		} catch (SAXException se) {
			throw new ValidationException("Validation failed: " + se.getMessage(), se);
		} catch (IOException ioe) {
			throw new ValidationException("Validation failed: " + ioe.getMessage(), ioe);
		}

		ec.throwErrors();
	}

	@SuppressWarnings("unchecked")
	public static Schema getSchema(Class c) throws SALDocumentException {
		try {
			JAXBContext tmpContext = JAXBContext.newInstance(c);
			SchemaCollector sc = new SchemaCollector();
			tmpContext.generateSchema(sc);
			return sc.getSchema();
		} catch (Exception e) {
			throw new SALDocumentException(e);
		}
	}

	private static class SchemaCollector extends SchemaOutputResolver {

		private StringWriter writer;

		public SchemaCollector() {
			writer = new StringWriter();
		}

		public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
			StreamResult sr = new StreamResult(writer);
			sr.setSystemId("memory");
			return sr;
		}

		public Schema getSchema() throws SAXException {
			SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
			return sf.newSchema(new StreamSource(new StringReader(writer.toString())));
		}
	}
}
