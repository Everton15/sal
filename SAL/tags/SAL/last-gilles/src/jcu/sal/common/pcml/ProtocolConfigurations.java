package jcu.sal.common.pcml;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jcu.sal.common.Slog;
import jcu.sal.common.exceptions.AlreadyPresentException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.common.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Objects of this class are used to group configuration information about a platform. They convey the same
 * information as PCML documents.
 * @author gilles
 *
 */
public class ProtocolConfigurations {
	private static Logger logger = Logger.getLogger(ProtocolConfigurations.class);
	static {
		Slog.setupLogger(logger);
	}
	
	private HashSet<ProtocolConfiguration> configs;
	
	private static String XPATH_CONFIG = "/"+PCMLConstants.PLATFORM_CONFIGURATION_NODE;
	private static String XPATH_PROTOCOL_DESC = XPATH_CONFIG+"/"+PCMLConstants.PROTOCOL_NODE;
	
	private ProtocolConfigurations(){
		configs = new HashSet<ProtocolConfiguration>();
	}
	
	/**
	 * This constructor creates a platform configuration object from individual protocol configuration objects
	 * @param c a collection of protocol configuration objects
	 * @throws AlreadyPresentException if two or more protocol configuration object have the same ID
	 */
	public ProtocolConfigurations(Collection<ProtocolConfiguration> c) throws AlreadyPresentException {
		this();
		for(ProtocolConfiguration p: c)
			addProtocolConfiguration(p);
	}
	
	/**
	 * This constructor creates a platform configuration object from individual protocol configuration objects
	 * @param c a set of protocol configuration objects
	 */
	public ProtocolConfigurations(Set<ProtocolConfiguration> c) {
		configs = new HashSet<ProtocolConfiguration>(c);
	}
	
	/**
	 * This constructor creates a platform configuration object from a valid PCML document
	 * @param d a valid PCML document
	 * @throws SALDocumentException if the given pcml document isnt valid
	 */
	public ProtocolConfigurations(Document d) throws SALDocumentException{
		this();
		checkDocument(d);
		try {
			parseDocument(d);
		} catch (AlreadyPresentException e) {
			throw new SALDocumentException("Cant parse the given PCML document",e);
		}
	}
	
	/**
	 * This method checks the given XML document to see if it is a valid PCML document
	 * @param d the document to be checked
	 * @throws SALDocumentException if the check fails
	 */
	private void checkDocument(Document d) throws SALDocumentException{
		int nb;
		try {
			nb = Integer.parseInt(XMLhelper.getTextValue("count("+XPATH_CONFIG+")", d));
		} catch (Throwable t) {
			logger.error("Cant check how many platform config section are in this document");
			throw new SALDocumentException("Malformed PCML document",t);
		}
		if(nb!=1){
			logger.error("Found "+nb+" platform config section in this document, there should be 1");
			logger.error(XMLhelper.toString(d));
			throw new SALDocumentException("Malformed PCML document - "+nb+" config sections");
		}
	}
	
	/**
	 * This method parses the given PCML document and exctracts the individual protocol configuration section
	 * and creates the matching protocol configuration objects
	 * @param d the PCML document
	 * @throws SALDocumentException if the PCML document is malformed
	 * @throws AlreadyPresentException if there are duplicate protocol configuration sections 
	 */
	private void parseDocument(Document d) throws SALDocumentException, AlreadyPresentException{
		NodeList n;
		try {
			n = XMLhelper.getNodeList(XPATH_PROTOCOL_DESC, d);
		} catch (NotFoundException e) {
			logger.error("No individual protocol configuration sections in this platform config document:");
			logger.error(XMLhelper.toString(d));
			throw new SALDocumentException("Malformed platform config document - no protocol config sections");
		}
		for (int i = 0; i < n.getLength(); i++)
			addProtocolConfiguration(new ProtocolConfiguration(XMLhelper.createDocument(n.item(i))));
	}
	
	/**
	 * This method adds a protocol configuration to this object 
	 * @param p the protocol configuration to be added
	 * @throw AlreadyPresentException if the element already exists
	 */
	private void addProtocolConfiguration(ProtocolConfiguration p) throws AlreadyPresentException {
		if(!configs.add(p)){
			logger.error("The platform configuration document contains protocols with the same name '"+p.getID()+"'");
			throw new AlreadyPresentException("2 or more individual protocols share the same name '"+p.getID()+"'");
		}
	}
	
	/**
	 * This constructor is identical to <code>ProtocolConfigurations(Document)</code> except that the XML document
	 * is passed as a String.
	 * @param xml a valid PCML platform configuration document
	 * @throws SALDocumentException if the XML document is not a valid PCML document
	 */
	public ProtocolConfigurations(String xml) throws SALDocumentException{
		this(XMLhelper.createDocument(xml));
	}
	
	/**
	 * This methods returns a set of protocol IDs present in this platform configuration object
	 * @return a set of protocol IDs present in this platform configuration object
	 */
	public Set<String> getPIDs(){
		HashSet<String> h = new HashSet<String>();
		
		for(ProtocolConfiguration p: configs)
			h.add(p.getID());
		
		return h;
	}
	
	/**
	 * This method returns a set of individual protocol configuration objects
	 * @return a set of protocol configuration objects
	 */
	public Set<ProtocolConfiguration> getConfigurations(){
		return new HashSet<ProtocolConfiguration>(configs);
	}
	
	/**
	 * This method returns an protocol configuration object, given its protocol ID
	 * @param pid the protcol ID
	 * @return a ProtocolConfiguration object
	 * @throws NotFoundException if the protocol ID cant be found
	 */
	public ProtocolConfiguration getDescription(String pid) throws NotFoundException {
		for(ProtocolConfiguration p: configs)
			if(p.getID().equals(pid))
				return p;
			
		throw new NotFoundException("no such protocol ID");
	}
	
	/**
	 * This method returns the number of Protocol configuration objects in this objects
	 * @return number of Protocol configuration objects in this objects
	 */
	public int getSize(){
		return configs.size();
	}
	
	/**
	 * This method returns the PCML document associated with this platform config object as a string
	 * @return the PCML document associated with this object
	 */
	public String getXMLString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<"+PCMLConstants.PLATFORM_CONFIGURATION_NODE+">\n");
		for(ProtocolConfiguration p: configs)
			sb.append(p.getXMLString());
		sb.append("</"+PCMLConstants.PLATFORM_CONFIGURATION_NODE+">\n");
		
		return sb.toString();
	}
	
	/**
	 * This method returns the XML document associated with this platform config object as a DOM document
	 * @return the XML document associated with this platform config object 
	 */
	public Document getXML() {
		try {
			return XMLhelper.createDocument(getXMLString());
		} catch (SALDocumentException e) {
			e.printStackTrace();
			logger.error("We shouldnt be here - error creating PCML doc");
			logger.error(getXMLString());
			throw new SALRunTimeException("Cant create PCML document",e); 
		}
	}
	
	
	/**
	 * This method returns an empty SML descriptions document
	 * @return an empty SML descriptions document
	 */
	public static Document createEmptyXML(){
	try {
			return XMLhelper.createDocument("<"+PCMLConstants.PLATFORM_CONFIGURATION_NODE+" />\n");
		} catch (SALDocumentException e) {
			e.printStackTrace();
			logger.error("We shouldnt be here - cant create PCML document");
			throw new SALRunTimeException("Cant create PCML document",e);
		}
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((configs == null) ? 0 : configs.hashCode());
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
		final ProtocolConfigurations other = (ProtocolConfigurations) obj;
		if (configs == null) {
			if (other.configs != null)
				return false;
		} else if (!configs.equals(other.configs))
			return false;
		return true;
	}
}
