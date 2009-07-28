package jcu.sal.common.cml;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jcu.sal.common.Slog;
import jcu.sal.common.cml.xml.CommandDescription;
import jcu.sal.common.cml.xml.CommandDescriptions;
import jcu.sal.common.cml.xml.ObjectFactory;
import jcu.sal.common.exceptions.AlreadyPresentException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.common.utils.JaxbHelper;
import jcu.sal.common.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * This class encapsulate multiple CML description objects (jcu.sal.common.cml.CMLDescription)
 * @author gilles
 *
 */
public class CMLDescriptions {
	private static Logger logger = Logger.getLogger(CMLDescriptions.class);
	static {Slog.setupLogger(logger);}
	
	private CommandDescriptions commandDescriptions;
	private static final ObjectFactory factory = new ObjectFactory();
	
	/**
	 * This constructor creates a new CML descriptions document from a CML descriptions XML document given as a string.
	 * @param cml the CML descriptions XML document 
	 * @throws SALDocumentException if the XML document is not a valid CML document
	 */
	public CMLDescriptions(String cml){
		try {
			commandDescriptions = JaxbHelper.fromXmlString(CommandDescriptions.class, cml);
		} catch (SALDocumentException e) {
			logger.error("Unable to parse the CML document");
			throw e;
		}
	}
	
	/**
	 * This constructor builds a new CML descriptions object from the given set
	 * @param c a set of CML description objects to be grouped in a CML descriptions object
	 * @throws AlreadyPresentException if there are duplicate CML descriptions in the collection
	 */
	public CMLDescriptions(Collection<CMLDescription> c) throws AlreadyPresentException{
		commandDescriptions = factory.createCommandDescriptions();
		for(CMLDescription cd: c)
			commandDescriptions.getCommandDescription().add(cd.getCommandDescription());
	}
	

	/**
	 * This method returns a set of individual CML description objects
	 * @return a set of CML description objects
	 */
	public Set<CMLDescription> getDescriptions(){
		HashSet<CMLDescription> d = new HashSet<CMLDescription>();
		for(CommandDescription cd : commandDescriptions.getCommandDescription())
			d.add(new CMLDescription(cd));
		return d;
	}
	
	/**
	 * This method return a CMLDescription object for a given CID
	 * @param cid the command ID
	 * @return the CML description associated with the given CID
	 * @throws NotFoundException the the CID is not found
	 */
	public CMLDescription getDescription(int cid) throws NotFoundException{
		for(CMLDescription c: getDescriptions())
			if(c.getCID().intValue()==cid)
				return c;
		
		throw new NotFoundException("no such CID "+cid);
	}

	/**
	 * This method returns the CML descriptions document as a string
	 * @return the CML descriptions document as a string
	 */
	public String getXMLString(){
		return JaxbHelper.toXmlString(commandDescriptions);
	}
	
	/**
	 * This method returns the CML descriptions document as a DOM document
	 * @return the CML descriptions document as a DOM document
	 */
	public Document getXML(){
		try {
			return XMLhelper.createDocument(getXMLString());
		} catch (SALDocumentException e) {
			logger.error("cant create CML descriptions document");
			throw new SALRunTimeException("Cant create the cml descriptions document",e);
		}		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((commandDescriptions == null) ? 0 : commandDescriptions
						.hashCode());
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
		CMLDescriptions other = (CMLDescriptions) obj;
		if (commandDescriptions == null) {
			if (other.commandDescriptions != null)
				return false;
		} else if (!commandDescriptions.equals(other.commandDescriptions))
			return false;
		return true;
	}

}
