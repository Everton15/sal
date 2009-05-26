package au.edu.jcu.haldbus.match;


import au.edu.jcu.haldbus.exceptions.InvalidArgumentsException;
import au.edu.jcu.haldbus.exceptions.MatchNotFoundException;

/**
 * This class represents an HAL match object that will match a given an integer value.
 * @author gilles
 *
 */
public class IntMatch extends AbstractMatch {
	private static String NAME="IntMatch";
	private int valMin;
	private int valMax;
	
	/**
	 * This method builds an {@link IntMatch} object that will match the value of property
	 * <code>prop</code> against <code>val</code>. The result is negated if <code>negate</code>
	 * is set to <sode>true</code>.
	 * @param prop the name of the property whose value is to be checked
	 * @param val the expected value of the property 
	 * @param negate whether or not to negate the result
	 * @throws InvalidArgumentsException if the property name is null
	 */
	public IntMatch(String prop, int val, boolean negate){
		super(prop, NAME, negate);
		valMax = valMin = val;	
	}
	
	
	/**
	 * This method builds an {@link IntMatch} object that will match the value of property
	 * <code>prop</code> against <code>val</code>. 
	 * @param prop the name of the property whose value is to be checked
	 * @param val the expected value of the property 
	 * @throws InvalidArgumentsException if the property name is null
	 */
	public IntMatch(String prop, int val) {
		super(prop, NAME);
		valMax = valMin = val;	
	}
	
	/**
	 * This method builds an {@link IntMatch} object that will match the value of property
	 * <code>prop</code> if it is between <code>min</code> and <code>max</code>. The result is negated if <code>negate</code>
	 * is set to <sode>true</code>.
	 * @param prop the name of the property whose value is to be checked
	 * @param min the minimum value
	 * @param max the maximum value   
	 * @param negate whether or not to negate the result
	 * @throws InvalidArgumentsException if the property name is null
	 */
	public IntMatch(String prop, int min, int max, boolean negate) {
		super(prop, NAME, negate);
		if(max<min)
			throw new InvalidArgumentsException("The maximum is below the minimum value");
		valMax = max; 
		valMin = min;		
	}
	
	/**
	 * This method builds an {@link IntMatch} object that will match the value of property
	 * <code>prop</code> if it is between <code>min</code> and <code>max</code>.
	 * @param prop the name of the property whose value is to be checked
	 * @param min the minimum value
	 * @param max the maximum value   
	 * @throws InvalidArgumentsException if the property name is null
	 */
	public IntMatch(String prop, int min, int max) {
		super(prop, NAME);
		if(max<min)
			throw new InvalidArgumentsException("The maximum value is below the minimum");
		valMax = max; 
		valMin = min;		
	}

	@Override
	protected String matchObject(Object o) throws MatchNotFoundException {
		int v = ((Integer) o).intValue();
		if(valMin<=v && v<=valMax)
			return String.valueOf(v);
		
		throw new MatchNotFoundException();
	}

}
