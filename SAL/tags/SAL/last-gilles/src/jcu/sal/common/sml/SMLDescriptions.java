package jcu.sal.common.sml;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import jcu.sal.common.Slog;
import jcu.sal.common.exceptions.AlreadyPresentException;
import jcu.sal.common.exceptions.ArgumentNotFoundException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.common.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class SMLDescriptions {
	private static Logger logger = Logger.getLogger(SMLDescriptions.class);
	static {
		Slog.setupLogger(logger);
	}
	
	private HashSet<SMLDescription> smls;
	
	/**
	 * the following table contains a mapping between a protocol ID and all the SML description
	 * objects belonging to that protocol.
	 */
	private Hashtable<String,List<SMLDescription>> smlsByPID;
	
	private static String XPATH_SENSORS_DESC = "/"+SMLConstants.SENSOR_CONF_NODE+"/"+SMLConstants.SENSOR_TAG;
	
	private SMLDescriptions() {
		smls = new HashSet<SMLDescription>();
	}
	
	/**
	 * This constructor creates a SML descriptions object from a collection of SMLDescription objects
	 * @param c the collection of SMLDescription objects
	 * @throws AlreadyPresentException if two or more SMLDescription objects in the collection share the same ID
	 */
	public SMLDescriptions(Collection<SMLDescription> c) throws AlreadyPresentException{
		this();
		for(SMLDescription s: c)
			addSMLDescription(s);
		
		sortByPID();
	}
	
	/**
	 * This constructor create an SMLDescriptions object from multiple SML description objects
	 * @param sml a map of sensor IDs and associated SML description objects
	 */
	public SMLDescriptions(Set<SMLDescription> m){
		smls = new HashSet<SMLDescription>(m);
		sortByPID();
	}
	
	/**
	 * This constructor create an SMLDescriptions object from an SML descriptions XML document given as a string.
	 * @param sml an SML descriptions XML document
	 * @throws SALDocumentException if the XML document is not a valid SML document
	 */
	public SMLDescriptions(String sml) throws SALDocumentException{
		this(XMLhelper.createDocument(sml));
	}
	
	/**
	 * This constructor creates an SMLDescriptions object from an SML descriptions XML document.
	 * @param d the SML descriptions XML document
	 * @throws SALDocumentException if the document cant be parsed / is malformed
	 */
	public SMLDescriptions(Document d) throws SALDocumentException {
		this();
		NodeList n;
		try {
			n = XMLhelper.getNodeList(XPATH_SENSORS_DESC, d);
		} catch (NotFoundException e) {
			logger.error("No individual sml descriptions in this SML descriptions document:");
			logger.error(XMLhelper.toString(d));
			throw new SALDocumentException("Malformed SML descriptions document - no sensor config found");
		}
		for (int i = 0; i < n.getLength(); i++)
			try {
				addSMLDescription(new SMLDescription(XMLhelper.createDocument(n.item(i))));
			} catch (AlreadyPresentException e) {
				logger.error("Duplicate sensor config sections");
				throw new SALDocumentException("Not a valid SML docuemnt", e);
			}
			
		sortByPID();
	}
	
	private void sortByPID(){
		smlsByPID = new Hashtable<String, List<SMLDescription>>();
		for(SMLDescription s: smls){
			if(!smlsByPID.containsKey(s.getProtocolName()))
				smlsByPID.put(s.getProtocolName(), new Vector<SMLDescription>());
			
			smlsByPID.get(s.getProtocolName()).add(s);
		}
	}
	
	private void addSMLDescription(SMLDescription s) throws AlreadyPresentException {
		if(!smls.add(s)){
			logger.error("found two sensor configuration objects with the same ID '"+s.getID()+"'");
			throw new AlreadyPresentException("Two sensor configuration objects with the same ID '"+s.getID()+"'");
		}		
	}
	
	/**
	 * This methods returns a set of sensor IDs present in this SML descriptions object
	 * @return a set of sensor IDs present in this SML descriptions object
	 */
	public Set<Integer> getSIDs(){
		HashSet<Integer> h = new HashSet<Integer>();
		
		for(SMLDescription s: smls)
			h.add(new Integer(s.getID()));

		return h;
	}
	
	/**
	 * This method returns a set of individual SML description objects
	 * @return a set of SML description objects
	 */
	public Set<SMLDescription> getDescriptions(){
		return new HashSet<SMLDescription>(smls);
	}
	
	/**
	 * This method returns an SMLDescription object for a single sensor, given it sensor ID
	 * @param sid the sensor ID
	 * @return an SMLDescription object
	 * @throws ArgumentNotFoundException if the sensor ID cant be found
	 */
	public SMLDescription getDescription(int sid) {
		for(SMLDescription s: smls)
			if(s.getID().equals(String.valueOf(sid)))
				return s;
			
		throw new ArgumentNotFoundException("no such sensor ID");
	}
	
	/**
	 * This methods returns a set of protocol IDs present in this SML descriptions object
	 * @return a set of protocol IDs present in this SML descriptions object
	 */
	public Set<String> getPIDs(){
		return new HashSet<String>(smlsByPID.keySet());
	}
	
	/**
	 * This method returns a list of SMLDescription objects belonging to a protocol
	 * @param pid the protocol ID for which the list is to be returned
	 * @return a list of SMLDescription objects having the same protocol ID as given in argument
	 * @throws ArgumentNotFoundException if the protocol ID cant be found
	 */
	public List<SMLDescription> getDescriptions(String pid) {
		if(!smlsByPID.containsKey(pid))
			throw new ArgumentNotFoundException("no such protocol ID "+pid);
		
		return new Vector<SMLDescription>(smlsByPID.get(pid));
	}
	
	
	/**
	 * This method returns the number of SML description objects in this object
	 * @return SML description objects in this object
	 */
	public int getSize(){
		return smls.size();
	}
	
	/**
	 * This method returns the XML document associated with this SMLDescriptions object as a string
	 * @return the XML document associated with this object
	 */
	public String getXMLString() {
		StringBuffer sb = new StringBuffer();
				
		sb.append("<"+SMLConstants.SENSOR_CONF_NODE+">");
		for(SMLDescription s: smls)
			sb.append(s.getXMLString());
		sb.append("</"+SMLConstants.SENSOR_CONF_NODE+">");
		
		return sb.toString();
	}
	
	/**
	 * This method returns the XML document associated with this SMLDescriptions object as a DOM document
	 * @return the XML document associated with this SMLDescriptions object 
	 */
	public Document getXML() {
		try {
			return XMLhelper.createDocument(getXMLString());
		} catch (SALDocumentException e) {
			logger.error("error creating XML SML doc");
			logger.error(getXMLString());
			throw new SALRunTimeException("Cant create the SML document",e);
		}
	}
	
	
	/**
	 * This method returns an empty SML descriptions document
	 * @return an empty SML descriptions document
	 */
	public static Document createEmptySML(){
	try {
			return XMLhelper.createDocument("<"+SMLConstants.SENSOR_CONF_NODE+" />\n");
		} catch (SALDocumentException e) {
			logger.error("We shouldnt be here - cant create SML descriptions document");
			throw new SALRunTimeException("Cant create SML document",e);
		}
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((smls == null) ? 0 : smls.hashCode());
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
		final SMLDescriptions other = (SMLDescriptions) obj;
		if (smls == null) {
			if (other.smls != null)
				return false;
		} else if (!smls.equals(other.smls))
			return false;
		return true;
	}
}
