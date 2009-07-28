package jcu.sal.common.cml;

import jcu.sal.common.Slog;
import jcu.sal.common.cml.xml.ObjectFactory;
import jcu.sal.common.cml.xml.Response;
import jcu.sal.common.exceptions.SALRunTimeException;

import org.apache.log4j.Logger;

/**
 * This class encapsulates information about the type of a response to
 * a command
 * @author gilles
 *
 */
public class ResponseType {
	private static Logger logger = Logger.getLogger(ResponseType.class);
	static {
		Slog.setupLogger(logger);
	}
	
	private static final ObjectFactory factory = new ObjectFactory();
	public static final ResponseType Integer = new ResponseType(CMLConstants.RET_TYPE_INT);
	public static final ResponseType ByteArray = new ResponseType(CMLConstants.RET_TYPE_BYTE_ARRAY);
	public static final ResponseType String = new ResponseType(CMLConstants.RET_TYPE_STRING);
	public static final ResponseType Float = new ResponseType(CMLConstants.RET_TYPE_FLOAT);
	public static final ResponseType Void = new ResponseType(CMLConstants.RET_TYPE_VOID);
	
	private Response response;

	
	
	/**
	 * this package-private constructor is meant to be used only by {@link CMLDescription}
	 * object
	 * @param r a {@link Response} form which to build this object
	 */
	ResponseType(Response r){
		response = r;
	}
	
	/**
	 * Construct a new return type with a {@link CMLConstants#RET_TYPE_VOID} type,
	 * the content type set to 
	 * {@link CMLConstants#CONTENT_TYPE_TEXT_PLAIN} and 
	 * the unit set to {@link CMLConstants#UNIT_NONE}.
	 * Supported types are listed in CMLConstants.RET_TYPE_*
	 * @param t the type (CMLConstants.RET_TYPE_*)
	 * @throws SALRunTimeException if the type is invalid
	 */
	public ResponseType() {
		this(CMLConstants.RET_TYPE_VOID,CMLConstants.CONTENT_TYPE_TEXT_PLAIN,CMLConstants.UNIT_NONE);
	}
	
	/**
	 * Construct a new return type without content type (set to 
	 * {@link CMLConstants#CONTENT_TYPE_TEXT_PLAIN} or 
	 * unit (set to {@link CMLConstants#UNIT_NONE}).
	 * Supported types are listed in CMLConstants.RET_TYPE_*
	 * @param t the type (CMLConstants.RET_TYPE_*)
	 * @throws SALRunTimeException if the type is invalid
	 */
	public ResponseType(String t) {
		this(t,CMLConstants.CONTENT_TYPE_TEXT_PLAIN,CMLConstants.UNIT_NONE);
	}
	
	/**
	 * Construct a new return type without  unit (set to {@link CMLConstants#UNIT_NONE}).
	 * Supported types are listed in CMLConstants.RET_TYPE_*
	 * 	 * Supported content types are listed in CMLConstants.CONTENT_TYPE_*
	 * @param t the type (CMLConstants.RET_TYPE_*)
	 * @param c the content type (CMLConstants.CONTENT_TYPE_*)
	 * @throws SALRunTimeException if the type is invalid
	 */
	public ResponseType(String t, String c) {
		this(t,c,CMLConstants.UNIT_NONE);
	}
	
	/**
	 * Construct a new return type.
	 * Supported types are listed in CMLConstants.RET_TYPE_*
	 * Supported content types are listed in CMLConstants.CONTENT_TYPE_*
	 * Supported units are listed in CMLConstants.UNIT_*
	 * @param t the type (CMLConstants.RET_TYPE_*)
	 * @param c the content type (CMLConstants.CONTENT_TYPE_*)
	 * @param u the unit(CMLConstants.UNIT_*)
	 * @throws SALRunTimeException if the type, content type or unit is invalid
	 */
	public ResponseType(String t, String c, String u) {
		response = factory.createResponse();
		checkArgs(t,c,u);
		response.setType(t);
		response.setContentType(c);
		response.setUnit(u);
	}
	/**
	 * Returns the return type (CMLConstants.RET_TYPE_*)
	 * @return the return type (CMLConstants.RET_TYPE_*)
	 */
	public String getType() {
		return response.getType(); 
	}
	
	/**
	 * Returns the content type
	 * @return the content type
	 */
	public String getContentType() {
		return response.getContentType(); 
	}
	
	/**
	 * Returns the unit
	 * @return the unit
	 */
	public String getUnit() {
		return response.getUnit();
	}
	
	/**
	 * This package-private method is meant to be used by {@link CMLDescription}
	 * objects.
	 * @return the internal {@link Response} object
	 */
	Response getResponse(){
		return response;
	}
	
	/**
	 * This method checks the given type, content type and unit
	 * are valid, ie they exist in {@link CMLConstants}.
	 * @param t the type (CMLConstants.RET_TYPE_*)
	 * @param c the content type (CMLConstants.CONTENT_TYPE_*)
	 * @param u the unit(CMLConstants.UNIT_*)
	 * @throws SALRunTimeException if any of the argument is invalid
	 */
	private void checkArgs(String t, String c, String u){
		if(!checkArray(CMLConstants.RET_TYPES, t)) {
			logger.error("Unknown return type: "+t);
			throw new SALRunTimeException("Invalid return type '"+t+"'");
		}
		
		if(!checkArray(CMLConstants.CONTENT_TYPES, c)) {
			logger.error("Unknown content type: "+c);
			throw new SALRunTimeException("Invalid content type '"+c+"'");
		}
		
		if(!checkArray(CMLConstants.UNITS, u)) {
			logger.error("Unknown unit: "+u);
			throw new SALRunTimeException("Invalid unit '"+u+"'");
		}
	}
	
	/**
	 * This method checks that the given value is present in the array.
	 * @param array the array of values to be checked
	 * @param value the value to be searched in the array
	 * @return whether or not the value is in the array
	 */
	private boolean checkArray(String[] array, String value){
		for(String s: array)
			if(s.equals(value))
				return true;
		
		return false;
	}
	
	public String toString(){
		String t;
		if(getType().equals(CMLConstants.RET_TYPE_INT))
			t =  "integer";
		else if(getType().equals(CMLConstants.RET_TYPE_BYTE_ARRAY))
			t = "byte array";
		else if(getType().equals(CMLConstants.RET_TYPE_FLOAT))
			t = "float";
		else if(getType().equals(CMLConstants.RET_TYPE_STRING))
			t = "string";
		else
			t = "none";
		return t;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((response == null) ? 0 : response.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResponseType other = (ResponseType) obj;
		if (response == null) {
			if (other.response != null)
				return false;
		} else if (!response.equals(other.response))
			return false;
		return true;
	}

}