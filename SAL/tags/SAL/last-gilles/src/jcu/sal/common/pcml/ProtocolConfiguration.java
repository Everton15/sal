package jcu.sal.common.pcml;

import jcu.sal.common.Parameters;
import jcu.sal.common.Slog;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.common.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class encapsulate a Protocol's configuration
 * @author gilles
 *
 */
public class ProtocolConfiguration implements HWComponentConfiguration {
	private static Logger logger = Logger.getLogger(ProtocolConfiguration.class);
	static { Slog.setupLogger(logger); }
	
	private static String XPATH_PROTOCOL = "/"+PCMLConstants.PROTOCOL_NODE;
	
	private String type;
	private String name;
	private Parameters params;
	private EndPointConfiguration epConfig;	
	/**
	 * 			<Protocol name="EMS_SNMP" type="SSNMP">
				<EndPoint name="eth0" type="ethernet">
					<parameters>
						<Param name="EthernetDevice" value="eth0" />
					</parameters>
				</EndPoint>
				<parameters>
					<Param name="AgentIP" value="192.168.0.2" />
					<Param name="CommunityString" value="EMSOLUTIONS" />
					<Param name="SNMPVersion" value="1" />
					<Param name="Timeout" value="1500" />
				</parameters>
			</Protocol>
	 */
	
	/**
	 * This constructor creates a configuration object for a protocol given its name, type, and the EndPointConfiguration object.
	 * The list of parameters will be empty.
	 * @param n the name of the protocol
	 * @param t the type of the protocol
	 * @param e the endpoint associated with this protocol 
	 */
	public ProtocolConfiguration(String n, String t, EndPointConfiguration e){
		this(n,t,new Parameters(), e);
	}
	
	/**
	 * This constructor creates a configuration object for a protocol given its name, type, parameters and the EndPointConfiguration object.
	 * @param n the name of the protocol
	 * @param t the type of the protocol
	 * @param p the parameters of the protocol
	 * @param e the endpoint associated with this protocol 
	 */
	public ProtocolConfiguration(String n, String t, Parameters p, EndPointConfiguration e){
		name = n;
		type = t;
		params = p;
		epConfig = e;
	}
	
	/**
	 * This constructor creates a new configuration object for a protocol from an XML document.<br>
	 * Example of valid XML configuration:<br>
	 * <pre>
	 * <Protocol name="EMS_SNMP" type="SSNMP">
	 * <EndPoint name="eth0" type="ethernet">
	 * <parameters>
	 *  <Param name="EthernetDevice" value="eth0" />
	 * </parameters>
	 * </EndPoint>
	 *  <parameters>
	 *  <Param name="AgentIP" value="192.168.0.2" />
	 *  <Param name="CommunityString" value="EMSOLUTIONS" />
	 *  <Param name="SNMPVersion" value="1" />
	 *  <Param name="Timeout" value="1500" />
	 *	</parameters>
	 * </Protocol>
	 * </pre>
	 * @param d the XML document representing the protocol configuration
	 * @throws SALDocumentException if the XML document isnt a valid protocol configuration document
	 */
	public ProtocolConfiguration(Document d) throws SALDocumentException{
		checkDocument(d);
		parseNameType(d);
		parseParameters(d);
		parseEndPointConfiguration(d);
	}
	
	/**
	 * This constructor is identical to <code>ProtocolConfiguration(Document)</code> except that the XML configuration
	 * document is passed as a String
	 * @param xml the protocol XML configuration document
	 * @throws SALDocumentException if the supplied XML document is not a valid protocol XML configuration document
	 */
	public ProtocolConfiguration(String xml) throws SALDocumentException{
		this(XMLhelper.createDocument(xml));
	}
	
	/**
	 * This method checks the supplied document to make sure it contains only one protocol configuration section
	 * @param d the document to check
	 * @throws SALDocumentException if the document contains more than one protocol configuration section or none.
	 */
	private void checkDocument(Document d) throws SALDocumentException{
		int nb;
		try {
			nb = Integer.parseInt(XMLhelper.getTextValue("count("+XPATH_PROTOCOL+")", d));
		} catch (Throwable t) {
			logger.error("Cant check how many Protocol configuration sections are in this document");
			throw new SALDocumentException("Malformed protocol config", t);
		}
		if(nb!=1){
			logger.error("There is more than one Protocol configuration section (found "+nb+") in this document");
			logger.error(XMLhelper.toString(d));
			throw new SALDocumentException("Invalid protocol config section - "+nb+" protocol sections found");
		}
	}

	/**
	 * This method parses the supplid protocol configuration document and extracts the protocol name and type.
	 * @param d the protocol configuration document
	 * @throws SALDocumentException if the name or type cant be extracted
	 */
	private void parseNameType(Document d) throws SALDocumentException{
		try {
			name = XMLhelper.getAttributeFromName(XPATH_PROTOCOL, PCMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE, d);
		} catch (Exception e) {
			logger.error("Cant find attr '"+PCMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"' in protocol config XML doc");
			logger.error(XMLhelper.toString(d));
			throw new SALDocumentException("Cant find the name of the protocol",e);
		}
		
		try {
			type = XMLhelper.getAttributeFromName(XPATH_PROTOCOL, PCMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE, d);
		} catch (Exception e) {
			logger.error("Cant find attr '"+PCMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"' in protocol config XML doc");
			logger.error(XMLhelper.toString(d));
			throw new SALDocumentException("cant find the type of the protocol",e);
		}
	}
	
	/**
	 * This method parses the supplied protocol configuartion document and extracts the parameters
	 * @param d the XML document
	 * @throws SALDocumentException if the parameters cant be found
	 */
	private void parseParameters(Document d) throws SALDocumentException{
		try {
			Node n;
			if((n = XMLhelper.getNode(XPATH_PROTOCOL+"/"+Parameters.PARAMETERS_NODE, d, true))!=null){
				params = new Parameters(n.getOwnerDocument());
			} else{
				params = new Parameters();
			}
		} catch (Exception e) {
			logger.error("Cant find the protocol parameters section in the protocol configuration doc");
			logger.error(XMLhelper.toString(d));
			e.printStackTrace();
			throw new SALDocumentException("Cant extract the protocol parameters section", e);
		}
	}
	
	/**
	 * This method extracts the endpoint configuration section from the given document and builds a new
	 * EndPointConfiguration object from it
	 * @param d the protocol configuration XML document
	 * @throws SALDocumentException if the EndPointConfiguration cant be instanciated
	 */
	private void parseEndPointConfiguration(Document d) throws SALDocumentException{
		Node n;
		try {
			if((n = XMLhelper.getNode(XPATH_PROTOCOL+"/"+PCMLConstants.ENDPOINT_NODE, d, true))!=null) {
				epConfig = new EndPointConfiguration(n.getOwnerDocument());
				return;
			}
		} catch (Exception e) {e.printStackTrace();}
		
		logger.error("Cant find the endpoint configuration document in the protocol configuration doc");
		logger.error(XMLhelper.toString(d));
		throw new SALDocumentException("Cant extract the Endpoint config");		
	}
	
	
	/**
	 * This method returns the ID of the protocol
	 * @return the ID of the protocol
	 */
	public String getID(){
		return name;
	}
	
	/**
	 * This method returns the type of the protocol
	 * @return the type of the protocol
	 */
	public String getType(){
		return type;
	}
	
	/**
	 * This method returns the parameters object of the protocol
	 * @return the parameters object of the protocol
	 */
	public Parameters getParameters(){
		return params;
	}
	
	/**
	 * This method returns the EndPointConfiguration object of the protocol
	 * @return the EndPointConfiguration object of the protocol
	 */
	public EndPointConfiguration getEPConfig(){
		return epConfig;
	}
	
	public String getXMLString(){
		StringBuffer sb = new StringBuffer();
		sb.append("<"+PCMLConstants.PROTOCOL_NODE+" "
					+PCMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"=\""+name+"\" "
					+PCMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"=\""+type+"\">\n"
					+params.getXMLString()
					+epConfig.getXMLString()
					+"</"+PCMLConstants.PROTOCOL_NODE+">\n");
		return sb.toString();
	}
	
	/**
	 * This method returns an XML document containing the protocol configuration
	 * @return an XML document containing the protocol configuration
	 */
	public Document getXML() {
		try {
			return XMLhelper.createDocument(getXMLString());
		} catch (SALDocumentException e) {
			logger.error("We shouldnt be here");
			throw new SALRunTimeException("cant create the PCML document",e);
		}
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((epConfig == null) ? 0 : epConfig.hashCode());
		result = PRIME * result + ((name == null) ? 0 : name.hashCode());
		result = PRIME * result + ((params == null) ? 0 : params.hashCode());
		result = PRIME * result + ((type == null) ? 0 : type.hashCode());
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
		final ProtocolConfiguration other = (ProtocolConfiguration) obj;
		if (epConfig == null) {
			if (other.epConfig != null)
				return false;
		} else if (!epConfig.equals(other.epConfig))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
