package jcu.sal.common;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * This class encapsulates configuration parameters associated with protocols, endpoints and sensors. A parameter instance
 * is a single key,value pair represented by a Parameter object. Parameter objects relating to the same protocol, endpoint or
 * sensor are encapsulated in Parameters objects (this class).
 * @author gilles
 *
 */
public class Parameters {
	private static Logger logger = Logger.getLogger(Parameters.class);
	static {
		Slog.setupLogger(logger);
	}
	public static String PARAMETERS_NODE = "parameters";
	public static String PARAMETER_NODE = "Parameter";
	public static String NAME_ATTRIBUTE_NODE = "name";
	public static String VALUE_ATTRIBUTE_NODE = "value";
	
	private static String XPATH_PARAMS = "//"+PARAMETERS_NODE;
	private static String XPATH_PARAM = XPATH_PARAMS+"/"+PARAMETER_NODE;
	
	
	private Map<String,Parameter> params;
	
	/**
	 * This constructor creates a new empty Parameters object 
	 */
	public Parameters() {
		params = new Hashtable<String,Parameter>();		
	}
	
	/**
	 * This constructor creates a new Parameters object with the list of Parameter objects given in argument
	 * @param p the list of Parameter objects to be encapsulated in a new Parameters object
	 */
	public Parameters(List<Parameter> p) {
		params = new Hashtable<String,Parameter>();
		for(Parameter param: p)
			params.put(param.getName(), param);		
	}
	
	/**
	 * This constructor behaves eaxctly as <code>Parameters(Document d)</code> except that the DOM document is passed as a String
	 * instead of a org.w3.DOM.Document object.
	 * @param p the string representation of the DOM document
	 * @throws SALDocumentException if there isnt exactly one parameters section or if it is malformed
	 */
	public Parameters(String p) throws SALDocumentException{
		this(XMLhelper.createDocument(p));
	}
	
	/**
	 * This constructor creates a Parameters object from the given DOM document. The document is parsed and up to one parameters
	 * section can be present.<br>Examples of valid documents:<br>
	 * <code>
	 * <EndPoint name="eth0" type="ethernet">
	 *   <parameters>
	 *    <Param name="EthernetDevice" value="eth0" />
	 * 	</parameters>
	 * </EndPoint>
	 * </code>
	 * <code>
	 * <EndPoint name="eth0" type="ethernet" />
	 * </code>
	 * <code>
	 * <Protocol name="mySNMP" type="SSNMP">
	 *   <parameters>
	 *    <Param name="CommunityString" value="PUBLIC" />
	 *    <Param name="AgentIP" value="10.0.0.1" />
	 * 	</parameters>
	 *  <EndPoint name="eth0" type="ethernet" />
	 * </Protocol>
	 * </code>
	 * 
	 * An exception is thrown if there is more than parameters section. The parameters section can be
	 * located anywhere in the document. This constructor doesnt make any assumption with respect to the position of the parameters
	 * section in the node hierarchy, as long as there is up to one section, and that it is properly formatted.   
	 * @param d the DOM document containing a parameters section
	 * @throws SALDocumentException if there is more than one parameteres section, of if it is malformed.
	 */
	public Parameters(Document d) throws SALDocumentException{
		params = new Hashtable<String,Parameter>();
		parseParams(d);
	}
	
	/**
	 * This method parses the given document and creates a Parameters object from the parameters section found in the document
	 * @param d the DOM document containing up to one parameters section 
	 * @throws SALDocumentException if the parameters section cant be parsed, or if there is more than one
	 */
	private void parseParams(Document d) throws SALDocumentException{
		//check that we have one and only one <parameters> tag
		int nb;
		try {
			nb = Integer.parseInt(XMLhelper.getTextValue("count("+XPATH_PARAMS+")", d));
		} catch (Throwable t) {
			logger.error("Cant check how many parameters sections are in this document");
			logger.error(XMLhelper.toString(d));
			t.printStackTrace();
			throw new SALDocumentException("Cant find/parse parameters section", t);
		}
		
		if(nb>1){
			logger.error("There should be only one parameters section in this document, found "+nb+" ");
			logger.error(XMLhelper.toString(d));
			throw new SALDocumentException("Document doesnt have exactly one parameters section");
		} else if (nb==0){
			logger.error("No parameters section in this document");
		} else {
			//creates individual parameter
			try {
				List<String> l = XMLhelper.getAttributeListFromElements(XPATH_PARAM, d);
				for(nb = 0; nb<l.size(); nb+=4)
					params.put(l.get(nb+1), new Parameter(l.get(nb+1), l.get(nb+3)));
			} catch (NotFoundException e) {
				logger.error("cant find/parse the parameter list");
				throw new SALDocumentException("Cant find parameter list", e);
			}
		}
	}
	
	/**
	 * This method returns a Parameter object given its name.
	 * @param name the name of the parameter to be returned
	 * @return the Paramter object matching the name passed as argument
	 * @throws NotFoundException if there are no parameters matching the given name
	 */
	public Parameter getParameter(String name) throws NotFoundException{
		if(params.containsKey(name))
			return params.get(name);
		
		//logger.debug("No parameter named "+name );
		throw new NotFoundException("Parameter '"+name+"' not found");
	}
	
	/**
	 * This method returns the number of parameters in this object.
	 * @return the number of parameters in this object.
	 */
	public int getSize(){
		return params.size();
	}
	
	/**
	 * This method returns whether there is a parameter named 'n' in this object.
	 * @param n the name of the parameter to look for
	 * @return whether there is a parameter named 'n' in this object.
	 */
	public boolean hasParameter(String n){
		return params.containsKey(n);
	}
	
	/**
	 * This method returns whether there is a parameter named 'name' in this object AND its value is equal to 'value'
	 * @param name the name of the parameter to look for
	 * @param value the value of the parameter
	 * @return whether there is a parameter named 'name' in this object and its value is set to 'value'
	 */
	public boolean hasValue(String name, String value){
		return params.containsKey(name) && params.get(name).getStringValue().equals(value);
	}
	
	/**
	 * This method returns a set of all parameter names in this object. 
	 * @return a set of all parameter names in this object.
	 */
	public Set<String> getNames(){
		return new HashSet<String>(params.keySet());
	}
	
	/**
	 * This method returns a string version of the XML document describing the parameters 
	 * @return the XML document describing the parameters
	 */
	public String getXMLString(){
		if(params.values().size()==0)
			return "<"+PARAMETERS_NODE+" />\n";
		else {
			StringBuffer sb = new StringBuffer("<"+PARAMETERS_NODE+">\n");
			for(Parameter p: params.values())
				sb.append(p.getXMLString());
			sb.append("</"+PARAMETERS_NODE+">\n");
			return sb.toString();
		}
	}
	
	/**
	 * The Parameter class encapsulate data related to a single parameter: a name, a value and a type (ParamType)
	 * @author gilles
	 */
	public static class Parameter{
		
		public static String STRING_TYPE = "t";
		
		private String name;
		private String value;
		private String type;
		
		/**
		 * This constructor creates a Parameter object which encpsualtes a name, a value and a type
		 * @param n the name of this parameter
		 * @param v the value associated with this parameter
		 * @param t the type of this parameter
		 */
		public Parameter(String n, String v){
			name = n;
			value = v;
			type = STRING_TYPE;
		}
		
		/**
		 * This method returns the name of this parameter
		 * @return the name of this parameter
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * This method returns the value of this parameter
		 * @return the value of this parameter
		 */
		public String getStringValue() {
			return value;
		}
		
		/**
		 * This method returns the type of this parameter
		 * @return the type of this parameter
		 */
		public String getType() {
			return type;
		}
		
		public String getXMLString() {
			return "\t<"+PARAMETER_NODE+" "+NAME_ATTRIBUTE_NODE+"=\""+name+"\" "+VALUE_ATTRIBUTE_NODE+"=\""+value+"\" />\n";
		}

		@Override
		public int hashCode() {
			final int PRIME = 31;
			int result = 1;
			result = PRIME * result + ((name == null) ? 0 : name.hashCode());
			result = PRIME * result + ((type == null) ? 0 : type.hashCode());
			result = PRIME * result + ((value == null) ? 0 : value.hashCode());
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
			final Parameter other = (Parameter) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((params == null) ? 0 : params.hashCode());
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
		final Parameters other = (Parameters) obj;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		return true;
	}

}
