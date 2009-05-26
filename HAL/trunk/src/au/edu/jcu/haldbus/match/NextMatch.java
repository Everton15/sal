package au.edu.jcu.haldbus.match;


import au.edu.jcu.haldbus.exceptions.InvalidArgumentsException;
import au.edu.jcu.haldbus.exceptions.MatchNotFoundException;

/**
 * This class builds an HAL match object that will match another HAL object against
 * the a given match.
 * @author gilles
 */
public class NextMatch extends AbstractMatch {

	/**
	 * This constructor builds a HAL Match object against a property found in another HAL object, not the current one.
	 * This HAL object is referred to either by its name or by a link pointing to its name:
	 * <ul><li>In the former case, the <code>obj</code> argument contains the name (UDI) of the next HAL object to be
	 * checked.</li>
	 * <li>In the latter case, the link is simply the name of a property preceded by a '@' sign. This property is found in the
	 * current HAL object and contains the name(UDI) of the next HAL object object to be checked.</li></ul>
	 * In both cases, when the next HAL object's properties have been retrieved, they will be matched against the
	 * given <code>HalMatchInterface n</code> instance.  
	 * @param obj either the UDI of the next HAL object to be checked, or a link to it ('@' followed by the name
	 * of a property found in the current HAL object which contains the name of the next HAL object).
	 * @param n the HalMatchInterface object against which the next HAL object will be matched 
	 * @throws InvalidArgumentsException if either of the arguments have invalid values
	 */
	public NextMatch(String obj, HalMatchInterface n){
		super(obj, n, "NextMatch");
	}

	@Override
	protected String matchObject(Object o) throws MatchNotFoundException {
		 throw new MatchNotFoundException();
	}

}
