package au.edu.jcu.haldbus.match;

import au.edu.jcu.haldbus.exceptions.InvalidArgumentsException;
import au.edu.jcu.haldbus.exceptions.MatchNotFoundException;

/**
 * This class builds an HAL match object that will match the value of a property
 * against a given value of a specific type T.
 * @author gilles
 *
 * @param <T> the type of the value that must be matched
 */
public class GenericMatch<T> extends AbstractMatch {
	private T propValue;
	private boolean subString, noCase;
	
	/**
	 * This constructor builds a HAL match object against a property of type T. If T is a String, the comparison will be
	 * case sensitive and against the entire string.
	 * @param prop the property name 
	 * @param val the expected value
	 * @param negate whether the result of the comparison must be negated or not (match will be returned as mismatch)
	 * @throws InvalidArgumentsException if either of the arguments are invalid (for example, an empty property name) 
	 */
	public GenericMatch(String prop, T val, boolean negate) {
		super(prop, "GenericMatch<"+val.getClass().getName()+">", negate);
		propValue = val;
		subString = false;
		noCase = false;
	}
	
	/**
	 * This constructor builds a HAL match object against a property of type T. If T is a String, the comparison will be
	 * case sensitive and against the entire string.
	 * @param prop the property name 
	 * @param val the expected value
	 * @throws InvalidArgumentsException if either of the arguments are invalid (for example, an empty property name) 
	 */
	public GenericMatch(String prop, T val) {
		super(prop, "GenericMatch<"+val.getClass().getName()+">");
		propValue = val;
		subString = false;
		noCase = false;
	}
	
	/**
	 * This constructor builds a HAL match object against a property of type String. The comparison can be made
	 * case insensitive and against a substring of the property by setting <code>substring</code> and <code>nocase</code. to true.
	 * @param prop the property name 
	 * @param val the expected value (Must be of type String, otherwise an {@link InvalidArgumentsException} exception
	 * will be thrown).
	 * @param substring if set, match if <code>val</code> is a substring tof the property value
	 * @param nocase if set, comparison is case insensitive
	 * @param negate whether to negate the match, ie if the value is found return {@link MatchNotFoundException} and vice-versa.
	 * @throws InvalidArgumentsException if one of the arguments is invalid (for example, val is not a string) 
	 */
	public GenericMatch(String prop, T val, boolean substring, boolean nocase, boolean negate) {
		super(prop, "GenericMatch<"+val.getClass().getName()+">", negate);
		if(!(val instanceof String))
			throw new InvalidArgumentsException("The given value is not of type String");

		propValue = val;
		subString = substring;
		noCase = nocase;
	}
	
	/**
	 * This constructor builds a HAL match object against a property of type String. The comparison can be made
	 * case insensitive and against a substring of the property by setting <code>substring</code> and <code>nocase</code. to true.
	 * @param prop the property name 
	 * @param val the expected value (Must be of type String, otherwise an {@link InvalidArgumentsException} exception
	 * will be thrown).
	 * @param substring if set, match if <code>val</code> is a substring tof the property value
	 * @param nocase if set, comparison is case insensitive
	 * @throws InvalidArgumentsException if one of the arguments is invalid (for example, val is not a string) 
	 */
	public GenericMatch(String prop, T val, boolean substring, boolean nocase){
		super(prop, "GenericMatch<"+val.getClass().getName()+">");
		if(!(val instanceof String))
			throw new InvalidArgumentsException("The given value is not of type String");

		propValue = val;
		subString = substring;
		noCase = nocase;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected String matchObject(Object o) throws MatchNotFoundException {
		if(!(propValue instanceof String)) {
			if ( ((T) o).equals(propValue) )
				return propValue.toString();
		} else if(!subString && !noCase){
			if ( ((String) o).equals(propValue) )
				return propValue.toString();
		} else if(!subString && noCase){
			if ( ((String) o).equalsIgnoreCase((String) propValue) )
				return propValue.toString();
		} else if(subString && !noCase){
			if ( ((String) o).indexOf((String) propValue)!=-1 )
				return propValue.toString();
		} else {
			if ( ((String) o).toLowerCase().indexOf(((String) propValue).toLowerCase())!=-1 )
				return propValue.toString();
		}
		throw new MatchNotFoundException();	
	}

}
