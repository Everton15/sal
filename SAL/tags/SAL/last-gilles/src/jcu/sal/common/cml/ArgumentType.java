package jcu.sal.common.cml;

import jcu.sal.common.Slog;
import jcu.sal.common.exceptions.SALRunTimeException;

import org.apache.log4j.Logger;

/**
 * This class encapsulates the different supported argument types in CML descriptors
 * @author gilles
 *
 */
public class ArgumentType {
	private static Logger logger = Logger.getLogger(ArgumentType.class);
	static {Slog.setupLogger(logger);}
	
	public static ArgumentType StringArgument = new ArgumentType(CMLConstants.ARG_TYPE_STRING);
	public static ArgumentType IntegerArgument = new ArgumentType(CMLConstants.ARG_TYPE_INT);
	public static ArgumentType FloatArgument = new ArgumentType(CMLConstants.ARG_TYPE_FLOAT);
	public static ArgumentType ListArgument = new ArgumentType(CMLConstants.ARG_TYPE_LIST);
	
	/**
	 * The type itself
	 */
	private String t;
	
	/**
	 * Construct a new argument type. Supported types are listed in CMLConstants.ARG_TYPE_*
	 * @param s the type (CMLConstants.ARG_TYPE_*)
	 * @throws SALRunTimeExceptions if the type is invalid
	 */
	public ArgumentType(String s){
		if(!s.equals(CMLConstants.ARG_TYPE_STRING) && !s.equals(CMLConstants.ARG_TYPE_INT) && 
				!s.equals(CMLConstants.ARG_TYPE_FLOAT) && !s.equals(CMLConstants.ARG_TYPE_LIST)
				) {
			logger.error("Unknown argument type: "+s);
			throw new SALRunTimeException("Wrong argument type '"+s+"'");
		}
		t = s;
	}
	/**
	 * Returns the argument type (CMLConstants.ARG_TYPE_*)
	 * @return the argument type (CMLConstants.ARG_TYPE_*)
	 */
	public String getType() {
		return t; 
	}
	
	public String toString(){
		return t; 
	}
	@Override
	public int hashCode() {
		final int PRIME = 73;
		int result = 3;
		result = PRIME * result + ((t == null) ? 0 : t.hashCode());
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
		final ArgumentType other = (ArgumentType) obj;
		if (t == null) {
			if (other.t != null)
				return false;
		} else if (!t.equals(other.t))
			return false;
		return true;
	}

	
}