package au.edu.jcu.haldbus.match;


import java.util.Vector;

import au.edu.jcu.haldbus.exceptions.MatchNotFoundException;

/**
 * This class builds an HAL match object that will match the list of values of an HAL property
 * against a given value of a type T. There will be a match if the property
 * values contain the given value.
 * @author gilles
 *
 * @param <T> the type of the value that must be matched
 */
public class VectorMatch<T> extends AbstractMatch {
	private T propValue;
	
	/**
	 * This method builds a match object that will check the list of values in HAL property
	 * <code>prop</code> against value <code>val</code>. The result will be negated if 
	 * <code>negate</code> is set to <code>true</code>. 
	 * @param prop the name of the HAL property
	 * @param val the value 
	 * @param negate whether or not to negate the result
	 */
	public VectorMatch(String prop, T val, boolean negate) {
		super(prop, "VectorMatch<"+val.getClass().getName()+">", negate);
		propValue = val;
	}
	
	/**
	 * This method builds a match object that will check the list of values in HAL property
	 * <code>prop</code> against value <code>val</code>. 
	 * @param prop the name of the HAL property
	 * @param val the value 
	 */
	public VectorMatch(String prop, T val){
		super(prop, "VectorMatch<"+val.getClass().getName()+">");
		propValue = val;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected String matchObject(Object o) throws MatchNotFoundException {
		Vector<T> l = (Vector<T>) o;
		if(l.contains(propValue))
			return propValue.toString();
		throw new MatchNotFoundException();
	}

}
