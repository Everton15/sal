package jcu.sal.common.sml;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import jcu.sal.common.Parameters;
import jcu.sal.common.Slog;
import jcu.sal.common.Parameters.Parameter;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.common.pcml.HWComponentConfiguration;
import jcu.sal.common.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * This class encapsulates an SML sensor description XML document. It provides information about a single SAL-managed sensor.
 * @author gilles
 *
 */
public class SMLDescription implements HWComponentConfiguration{
	private static Logger logger = Logger.getLogger(SMLDescription.class);
	static {
		Slog.setupLogger(logger);
	}

	private Integer sid;
	private Parameters parameters;
	
	private static String XPATH_SENSOR_DESC="/"+SMLConstants.SENSOR_TAG;
	private static String XPATH_SENSOR_PARAMETER=XPATH_SENSOR_DESC+"/"+Parameters.PARAMETERS_NODE+"/"+Parameters.PARAMETER_NODE;
	
	/**
	 * This method constructs a new SML description object given a sensor ID and a table of key,value pairs representing
	 * the sensor parameters. These parameters will be checked to make sure all required parameters are there. This constructor will
	 * check that there are at least <code>SMLConstants.NB_REQUIRED_PARAMETERS</code> parameters whose names are in the
	 * <code>SMLConstants.PARAM_NAMES</code> list. More parameters may exist though, but the required ones must be present.
	 * @param s the sensor ID
	 * @param p the parameter list
	 * @throws SALDocumentException if either arguments are invalid
	 */
	public SMLDescription(Integer s, Parameters p) throws SALDocumentException {
		//Check the nb of args
		if(s.intValue()<0 || s.intValue()>SMLConstants.SENSOR_ID_MAX || p.getSize() < SMLConstants.NB_REQUIRED_PARAMETERS)
			throw new SALDocumentException("Invalid sensor ID or parameter list");
		sid = new Integer(s);
		
		//check the param list
		for(String tmp: SMLConstants.PARAM_NAMES)
			if(!p.hasParameter(tmp)) throw new SALDocumentException("Couldnt find required parameter '"+tmp+"'");

		parameters = p;
	}
	
	/**
	 * This construcotr is identical to <code>SMLDescription(Document)</code> except that the XML document
	 * is passed as a string here.
	 * @param xml a string representation of a valid SML description of a sensor
	 * @throws SALDocumentException if the given XML document is not a valid SML description
	 */
	public SMLDescription(String xml) throws SALDocumentException{
		this(XMLhelper.createDocument(xml));
	}
	
	/**
	 * This constructor creates an SML description object from the supplied XML document containing a valid
	 * SML description of a sensor
	 * @param doc a XML document containing a valid SMl description of a sensor
	 * @throws SALDocumentException the the given document is not a valid SML description
	 */ 
	public SMLDescription(Document doc) throws SALDocumentException {
		checkDocument(doc);
		parseSensorID(doc);
		parseParameters(doc);		
	}
	
	/**
	 * This method checks that the given document is a valid SML description document.
	 * It checks that there is only one SMLDescription instance, and that the required number of parameters are present.
	 * @param d the SML description document to be validated
	 * @throws SALDocumentException if the document is not a valid SMLdescription
	 */
	private void checkDocument(Document d) throws SALDocumentException {
		int nb;
		try {
			nb = Integer.parseInt(XMLhelper.getTextValue("count("+XPATH_SENSOR_DESC+")", d));
		} catch (Throwable t) {
			logger.error("Cant check how many SML descriptions are in this document");
			throw new SALDocumentException("Malformed SML document",t);
		}
		if(nb!=1){
			logger.error("There is more than one SML description (found "+nb+") in this document");
			logger.error(XMLhelper.toString(d));
			throw new SALDocumentException(nb+" SML description sections found instead of 1");
		}
		
		try {
			nb = Integer.parseInt(XMLhelper.getTextValue("count("+XPATH_SENSOR_PARAMETER+")", d));
			if(nb!=SMLConstants.NB_REQUIRED_PARAMETERS){
				logger.error("The number of parameters found in this document ("+nb+") is different from the required number ("+
							SMLConstants.NB_REQUIRED_PARAMETERS+")");
				logger.error(XMLhelper.toString(d));
				throw new SALDocumentException("The number of parameters found in this document ("+nb+") is different from the required number ("+
						SMLConstants.NB_REQUIRED_PARAMETERS+")");
			}
		} catch (Throwable t) {
			logger.error("Cant check how many parameters there are in this document");
			throw new SALDocumentException("Cant check how many parameters there are in this document", t);
		}		
	}
	
	/**
	 * This method parses the given SML description document and extracts the sensorID
	 * @param d the SML description document
	 * @throws SALDocumentException if the sensor ID cant be found
	 */
	private void parseSensorID(Document d) throws SALDocumentException {
		try {
			sid = Integer.parseInt(XMLhelper.getAttributeFromName(XPATH_SENSOR_DESC, SMLConstants.SENSOR_ID_ATTRIBUTE_NODE, d));
		} catch (Exception e) {
			logger.error("Cant find attr '"+SMLConstants.SENSOR_ID_ATTRIBUTE_NODE+"' in SMLdescriptor XML doc");
			throw new SALDocumentException("Cant find the sensor ID", e);
		}
	}
	
	/**
	 * This method parses the parameter list is the SML description document and extracts each of them. It assumes the SML
	 * document contains all the required parameters and only those.
	 * @param d the SML description document
	 * @throws SALDocumentException if there is an error finding the required arguments or parsing their values
	 */
	private void parseParameters(Document d) throws SALDocumentException {
		List<Parameter> l = new Vector<Parameter>();
		String paramValue;
		
		for(String paramName: SMLConstants.PARAM_NAMES){
			try {
				paramValue = XMLhelper.getAttributeFromName(XPATH_SENSOR_PARAMETER+"[@"+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+
															"=\""+paramName+"\"]", SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE, d);
				l.add(new Parameter(paramName, paramValue));
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Cant find value for parameter '"+paramName+"' in SMLdescriptor XML doc");
				throw new SALDocumentException("Cant find value for parameter '"+paramName+"'", e);
			}
		}
		parameters = new Parameters(l);
	}
	
	/**
	 * This method returns the sensor ID
	 * @return the sensor ID
	 */
	public int getSID() {
		return sid.intValue();
	}
	
	/**
	 * This method returns the sensor ID
	 * @return the sensor ID
	 */
	public String getID() {
		return sid.toString();
	}
	
	/**
	 * This method returns the type of the component: {@link SMLConstants#SENSOR_TYPE}
	 */
	public String getType() {
		return SMLConstants.SENSOR_TYPE;
	}
	
	/**
	 * This method returns the value of a parameter given its name.
	 * @param n the name of the parameter
	 * @return the value of the parameter, which can be null if no parameter with this name is found
	 * @throws NotFoundException if there is no parameter matching the given name
	 */
	public String getParameter(String n) throws NotFoundException{
		return parameters.getParameter(n).getStringValue();
	}
	
	/**
	 * This method returns a set of parameter names associated with this sensor description document.
	 * @return a set of parameter names
	 */
	public Set<String> getParameterNames(){
		return parameters.getNames();
	}
	
	/**
	 * This method returns the parameters associated with this sensor
	 * @return the parameters associated with this sensor
	 */
	public Parameters getParameters(){
		return parameters;
	}
	
	/**
	 * This method returns the name of the protocol associated with the sensor listed in this SML description
	 * @return the name of the protocol associated with the sensor listed in this SML description
	 */
	public String getProtocolName(){
		try {
			return parameters.getParameter(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE).getStringValue();
		} catch (NotFoundException e) {
			logger.error("we shouldnt be here. It seems the sensor has been created without a protocol name");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * This method returns the type of the protocol associated with the sensor listed in this SML description
	 * @return the type of the protocol associated with the sensor listed in this SML description
	 */
	public String getProtocolType(){
		try {
			return parameters.getParameter(SMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE).getStringValue();
		} catch (NotFoundException e) {
			logger.error("we shouldnt be here. It seems the sensor has been created without a protocol type");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * This method returns the address of the sensor listed in this SML description
	 * @return the address of the sensor listed in this SML description
	 */
	public String getSensorAddress(){
		try {
			return parameters.getParameter(SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE).getStringValue();
		} catch (NotFoundException e) {
			logger.error("we shouldnt be here. It seems the sensor has been created without an address");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * This method returns the XML version of this SMLDescription object as a string
	 * @return the XML version of this SMLDescription object
	 */
	public String getXMLString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<"+SMLConstants.SENSOR_TAG+" "+SMLConstants.SENSOR_ID_ATTRIBUTE_NODE+"=\""+sid.toString()+"\">\n"
			+"\t<"+Parameters.PARAMETERS_NODE+">\n");

		for(String n: parameters.getNames()){
			try {
				sb.append("\t\t<"+Parameters.PARAMETER_NODE+" "+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+"=\""+n+
						"\" "+SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE+"=\""+parameters.getParameter(n).getStringValue()+"\" />\n");
			} catch (NotFoundException e) {
				logger.error("we shouldnt be here. looking for parameter name "+n+ " but cant find it");
			}
		}

		sb.append("\t</"+Parameters.PARAMETERS_NODE+">\n</"+SMLConstants.SENSOR_TAG+">\n");
		return sb.toString();		
	}
	
	/**
	 * This method compares the given SMLDescription object with this one and returns whether they are semantically the same.
	 * Two SML descriptions object are semantically the same if their parameters are the same.
	 * @param s the SML description to be compared with this one
	 * @return whether they are semantically the same.
	 */
	public boolean isSame(SMLDescription s){
		return s.parameters.equals(parameters);
	}
	
	/**
	 * This method returns the XML version of this SMLDescription object as a DOM document
	 * @return the XML version of this SMLDescription object 
	 */
	public Document getXML(){
		try {
			return XMLhelper.createDocument(getXMLString());
		} catch (SALDocumentException e) {
			logger.error("error creating XML SML doc");
			throw new SALRunTimeException("Error creating SML document",e);
		}
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = PRIME * result + ((sid == null) ? 0 : sid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SMLDescription other = (SMLDescription) obj;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		if (sid == null) {
			if (other.sid != null)
				return false;
		} else if (!sid.equals(other.sid))
			return false;
		return true;
	}	
}
