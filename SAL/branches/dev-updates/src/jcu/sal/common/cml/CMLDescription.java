/**
 * 
 */
package jcu.sal.common.cml;

import java.util.List;
import java.util.Vector;

import jcu.sal.common.Slog;
import jcu.sal.common.cml.xml.Argument;
import jcu.sal.common.cml.xml.CommandDescription;
import jcu.sal.common.cml.xml.ObjectFactory;
import jcu.sal.common.cml.xml.CommandDescription.Arguments;
import jcu.sal.common.cml.xml.CommandDescription.Streaming.Bounds;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SALRunTimeException;
import jcu.sal.common.utils.JaxbHelper;
import jcu.sal.common.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * This class encapsulates a CML Command Description document. This document describes a single command.
 * @author gilles
 *
 */
public class CMLDescription {
	private static Logger logger = Logger.getLogger(CMLDescription.class);
	static {Slog.setupLogger(logger);}
	
	private CommandDescription commandDescription;
	private static final ObjectFactory factory = new ObjectFactory();
	private String methodName;
	
	private CMLDescription(String m){
		methodName = m!=null ? m : "";
		commandDescription = factory.createCommandDescription();
	}
	
	/**
	 * This package-private constructor is meant to be used only by 
	 * {@link CMLDescriptions} objects
	 * @param c
	 */
	CMLDescription(CommandDescription c) {
		commandDescription = c;
		methodName="";
	}
	/**
	 * This method constructs a CML description object
	 * @param mName the name of the method which should be called when a command instance matching this description is received
	 * @param id the command id
	 * @param name the name of the command
	 * @param desc the short description of the command
	 * @param args a list of {@link CMLArgument}s representing the arguments to this command
	 * @param returnType the type of the return result
	 * @throws SALRunTimeException if any of the argument is invalid   
	 */
	public CMLDescription(String methodName, Integer id, String name, String desc, List<CMLArgument> args, ResponseType returnType){
		this(methodName);
		commandDescription.setCid(id.toString());
		commandDescription.setName(name);
		commandDescription.setLongDescription("");
		commandDescription.setShortDescription(desc);
		commandDescription.setResponse(returnType.getResponse());
		commandDescription.setArguments(factory.createCommandDescriptionArguments());
		Arguments a = commandDescription.getArguments();

		if(args!=null)
			for(CMLArgument c: args)
				a.getArgument().add(c.getArgument());
		

	}
	
	/**
	 * This method constructs a CML description object
	 * @param mName the name of the method which should be called when a command instance matching this description is received
	 * @param id the command id
	 * @param name the name of the command
	 * @param desc the short description of the command
	 * @param args a list of {@link CMLArgument}s representing the arguments to this command
	 * @param bounds the smapling frequency bounds, or null if the command cannot be streamed
	 * @param returnType the type of the return result
	 * @throws SALRunTimeException if any of the argument is invalid   
	 */
	public CMLDescription(String methodName, Integer id, String name, String desc, List<CMLArgument> args, ResponseType returnType, SamplingBounds bounds){
		this(methodName, id, name, desc, args, returnType);
		if(bounds!=null){
			commandDescription.setStreaming(factory.createCommandDescriptionStreaming());
			commandDescription.getStreaming().setBounds(bounds.getBounds());
		}
	}
	
	/**
	 * This method creates a new a CML description object re-using the same description, argument types & argNames and return type
	 * as an existing description.
	 * @param id the command id of the new description
	 * @param name the name of the new command
	 * @param existing the existing description whose description, argument types & argNames and return type will be reused
	 */
	public CMLDescription(Integer id, String name, CMLDescription existing){
		this(existing.methodName, id, name,existing.getShortDesc(), existing.getArguments(), existing.getResponseType(), existing.getSamplingBounds());
	}
	
	/**
	 * This method returns this CML description's document as a String
	 * @return the CML description document as a String
	 */
	public String getXMLString(){
		return JaxbHelper.toXmlString(commandDescription);
	}
	
	/**
	 * This method returns this CML description's document
	 * @return the CML description document
	 */
	public Document getXML(){
		try {
			return XMLhelper.createDocument(getXMLString());
		} catch (SALDocumentException e) {
			logger.error("error creating XML CML doc");
			throw new SALRunTimeException("Cant creat the CML document",e);
		}
	}
	
	/**
	 * This method returns this CML description's command identifier
	 * @return this CML description's CID
	 */
	public Integer getCID(){
		return new Integer(commandDescription.getCid());
	}
	
	/**
	 * This method returns this CML description's name, ie the name of the command
	 * @return this CML description's name
	 */
	public String getName(){
		return commandDescription.getName();
	}
	
	/**
	 * This method returns the name of the method associated with this CML description
	 * @return the method associated with this CML description
	 */
	public String getMethodName(){
		return methodName;
	}
	
	/**
	 * This method returns this CML description's short description
	 * @return an this CML description's short description
	 */
	public String getShortDesc() {
		return commandDescription.getShortDescription();
	}
	
	/**
	 * This method returns this CML description's long description
	 * @return an this CML description's long description
	 */
	public String getLongDesc() {
		return commandDescription.getLongDescription();
	}

	/**
	 * This method returns a list of {@link CMLArgument} for all
	 * arguments in this description. 
	 * @return a list of {@link CMLArgument}.
	 */
	public List<CMLArgument> getArguments(){
		Vector<CMLArgument> v = new Vector<CMLArgument>();
		if(commandDescription.getArguments()!=null)
			for(Argument a: commandDescription.getArguments().getArgument())
				v.add(new CMLArgument(a));
		return v;
	}
		
	/**
	 * This method returns the return type for this  CML description
	 * @return the return type for this  CML description
	 */
	public ResponseType getResponseType() {
		return new ResponseType(commandDescription.getResponse());
	}
	
	/**
	 * This method specifies whether this command can be run multiple
	 * times to create a stream of results.
	 * @return whether or not this command can be streamed.
	 */
	public boolean isStreamable(){
		return commandDescription.getStreaming()!=null;
	}
	
	/**
	 * This method returns the sampling frequency bounds, if this command
	 * can be streamed.
	 * @return the sampling frequency bounds or null if this command cannot be streamed
	 */
	public SamplingBounds getSamplingBounds(){
		if(!isStreamable())
			return null;
		
		return new SamplingBounds(commandDescription.getStreaming().getBounds());
	}
	
	/**
	 * This package-private methods is meant to be used
	 * only internally.
	 * @return
	 */
	CommandDescription getCommandDescription(){
		return commandDescription;
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
				+ ((commandDescription == null) ? 0 : commandDescription
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
		CMLDescription other = (CMLDescription) obj;
		if (commandDescription == null) {
			if (other.commandDescription != null)
				return false;
		} else if (!commandDescription.equals(other.commandDescription))
			return false;
		return true;
	}
	

	/**
	 * This class encapsulates sampling frequency bounds
	 * @author gilles
	 *
	 */
	public static class SamplingBounds{
		private static final ObjectFactory factory = new ObjectFactory();
		private Bounds bounds;
		
		private SamplingBounds(Bounds b){
			bounds = b;
		}
		
		/**
		 * This method build new sampling frequency bounds with
		 * the given minimum, maximum & step values.
		 * @param min the minimum sampling frequency
		 * @param max the maximum sampling frequency
		 * @param step the step
		 * @param cont whether or not continuous sampling is permitted
		 */
		public SamplingBounds(int min, int max, int step, boolean cont){
			bounds = factory.createCommandDescriptionStreamingBounds();
			bounds.setMin(min);
			bounds.setMax(max);
			bounds.setStep(0);
			bounds.setContinuous(cont);
		}
		
		private Bounds getBounds(){
			return bounds;
		}
		
		/**
		 * This method build new sampling frequency bounds with
		 * the given minimum & maximum values. The step value is set to 1.
		 * @param min the minimum sampling frequency
		 * @param max the maximum sampling frequency
		 * @param cont whether or not continuous sampling is permitted
		 */
		public SamplingBounds(int min, int max, boolean cont){
			this(min,max,1,cont);
		}
		
		/**
		 * @return the min
		 */
		public int getMin() {
			return bounds.getMin();
		}
		/**
		 * @return the max
		 */
		public int getMax() {
			return bounds.getMax();
		}
		/**
		 * @return the step
		 */
		public int getStep() {
			return bounds.getStep();
		}
		/**
		 * @return the continuous
		 */
		public boolean isContinuous() {
			return bounds.isContinuous();
		}
		
	}
	
}
