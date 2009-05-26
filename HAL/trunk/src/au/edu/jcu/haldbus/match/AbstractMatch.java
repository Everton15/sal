package au.edu.jcu.haldbus.match;

import au.edu.jcu.haldbus.exceptions.InvalidArgumentsException;
import au.edu.jcu.haldbus.exceptions.InvalidMethodCall;
import au.edu.jcu.haldbus.exceptions.MatchNotFoundException;

/**
 * This class implements the barebones structure of a Hal Match object. It mostly handles construction of the object
 * and makes sure the right attributes are initialised. It then declares some get() methods. Subclasses need only
 * implement a proper constructor and a matchObject() method.<br>HAL match object have some form of recursivity: they can
 * hold a reference to another HAL match object (referred to as the <b>"next HAL match object"</b>). Two scenarios can happen:<ul>
 * <li>The simplest one: an HAL match object is to be matched against a property found in the current HAL object. In this
 * case, the match object contains the name of the property to be checked, and the caller invokes the <code>match()</code>
 * method with the value of the property as the sole argument.<li>
 * </li>The more complicated one: an HAL match object is to be matched against a property found in another HAL object
 * (referred to as the <b>"next HAL object"</b>), and the UDI of the "next HAL object" is either known in advance (and stored in
 * this HAL match) or can be found in the value of a property in the current HAL object. In this case, the caller invokes
 * either <code>getNextObjectValue()</code> or <code>getNextObjectLink()</code> in order to find the next HAL object's UDI.
 * Once the properties of the next HAL object have been retrieved, they will be matched against the next HAL match object, whose
 * reference is found in this HAL match object (recursivity)</li></ul>
 * Classes using HalMatchInterface objects to find a match follow this example:<br>
 * <code>public String findMatch(HalMatchInterface m, Map&lt;String,Object&gt; props)</code> 
 * <ul><li>if <code>m.matchThisObject()</code> then
 * 	<ul><li>Get the name of the property of interest: <code>p = m.getPropName()</code></li>
 * 	<li><code>return match(props.get(p))</code></li></ul>
 * <li>else if <code>m.matchNextObjectValue()</code> then
 * 	<ul><li>Get the name of the next HAL object: <code>m.getNextObjectValue()</code></li>
 * 	<li>Get its list of properties: <code>nextProps</code></li>
 *  <li>Get the next HAL match object: <code>nextMatch = getNextMatch()</code></li>
 *  <li><code>return findMatch(nextMatch, nextProps)</code>
 * 	</ul>
 * <li>else if <code>m.matchNextObjectLink()</code> then
 * 	<ul><li>Get the link to the next HAL object: <code>m.getNextObjectLink()</code></li>
 * 	<li>Get its name and list of properties <code>nextProps</code></li>
 *  <li>Get the next HAL match object: <code>nextMatch = getNextMatch()</code></li>
 *  <li><code>return findMatch(nextMatch, nextProps)</code>
 * 	</ul>
 * </ul>
 * @author gilles
 *
 */
public abstract class AbstractMatch implements HalMatchInterface {
	private static String THIS_OBJECT = "`#ShouldbeFine@@#";
	private String name;
	private String object;
	private String propName;
	private HalMatchInterface nextMatch;
	private boolean negate;

	private AbstractMatch(String obj, String pName, HalMatchInterface n, String name, boolean negate) throws InvalidArgumentsException {
		if(name==null)
			throw new InvalidArgumentsException("the property name cannot be null");
		this.name = name;
		if (obj != null && n != null) {
			// check that the object name is valid
			char c = obj.charAt(0);
			if (c == THIS_OBJECT.charAt(0) || c == '/' || c == '@') {
				object = obj;
				propName = null;
				nextMatch = n;
			} else 
				throw new InvalidArgumentsException("The next HAL object name or link is invalid");

		} else if (pName != null) {
			object = THIS_OBJECT;
			propName = pName;
			nextMatch = null;
		} else 
			throw new InvalidArgumentsException("Either the property name or the next HAL object name and match must be valid");

		this.negate = negate;
	}
	
	/**
	 * This constructor builds a HAL Match object against a property found in another HAL object, not the current one.
	 * This HAL object is referred to either by its name or by a link pointing to its name:
	 * <ul><li>In the former case, the <code>obj</code> argument contains the name (UDI) of the next HAL object to be
	 * checked.</li>
	 * <li>In the latter case, the link is simply the name of a property found preceded by a '@' sign in the
	 * current HAL object that contains the name of the next HAL object object to be checked.</li></ul>
	 * In both cases, when the next HAL object's properties have been retrieved, they will be matched against the
	 * given <code>HalMatchInterface n</code> instance.  
	 * @param obj either the UDI of the next HAL object to be checked, or a link to it ('@' followed by the name
	 * of a property found in the current HAL object which contains the name of the next HAL object).
	 * @param n the HalMatchInterface object against which the next HAL object will be matched 
	 * @param name the name of this Hal match
	 * @param negate whether or not to negate the match (if the match is successful, throw MatchNotFound exception and vice-versa)
	 * @throws InvalidArgumentsException if either of the arguments have invalid values
	 */
	protected AbstractMatch(String obj, HalMatchInterface n, String name, boolean negate) {
		this(obj, null, n, name, negate);
	}
	
	/**
	 * @see {@link AbstractMatch#AbstractMatch(String, HalMatchInterface, String, boolean)}
	 * @param obj
	 * @param n
	 * @param name
	 */
	protected AbstractMatch(String obj, HalMatchInterface n, String name) {
		this(obj, null, n, name, false);
	}

	/**
	 * This constructor builds an HAL match object against a property in the current HAL object. The property to be checked
	 * is referred to by its name (<code>prop</code> argument). 
	 * @param prop the name of the property to be checked 
	 * @param name the name of the HAL match object
	 * @param negate whether or not to negate the match (if the match is successful, throw MatchNotFound exception and vice-versa)
	 * @throws InvalidArgumentsException if either of the arguments have invalid values
	 */
	protected AbstractMatch(String prop, String name, boolean negate) {
		this(null, prop, null, name, negate);
	}
	
	/**
	 * @see {@link AbstractMatch#AbstractMatch(String, String, boolean)}
	 * @param prop
	 * @param name
	 */
	protected AbstractMatch(String prop, String name)  {
		this(null, prop, null, name, false);
	}

	/**
	 * This method is called when the a property in the current HAL object is to be matched. The value of this property
	 * is the sole argument. If the property has the right value, the method returns a string representation of the object o.
	 * Otherwise the methods throws a MatchNotFound exception.
	 * @param o the value of the property
	 * @return a string representation of the object o
	 * @throws MatchNotFoundException if the property doesnt have the expected value.
	 */
	public final String match(Object o) throws MatchNotFoundException {
		if (o == null)
			throw new MatchNotFoundException();

		String ret;
		try { ret =  matchObject(o); }
		catch (MatchNotFoundException e) {
			if(negate)
				return "";
			//else
			throw e;
		}
		if(negate)
			throw new MatchNotFoundException();
		//else
		return ret;
		
	}

	/**
	 * This method must be implemented by subclasses. It is up to the subclasses to decide when a match is found, based on 
	 * the value of the property. If the match is found, this method must return a string (which really should be 
	 * <code>o.toString()</code> or similar)
	 * @param o the value of the property.
	 * @return the string representation of o 
	 * @throws MatchNotFoundException if the property doesnt have the expected value.
	 */
	protected abstract String matchObject(Object o)	throws MatchNotFoundException;

	/**
	 * This method allows the caller to determine whether this Hal match is against a property found in the current
	 * HAL object.
	 * @return whether this Hal match is against a property found in the current HAL object.
	 */
	public final boolean matchThisObject() {
		return object.equals(THIS_OBJECT);
	}

	/**
	 * This method allows the caller to determine whether this Hal match is against a property found in another
	 * HAL object, referred to by a link.
	 * @return whether this Hal match is against a property found in another HAL object, referred to by a link.
	 */
	public final boolean matchNextObjectLink() {
		return object.charAt(0) == '@';
	}

	/**
	 * This method allows the caller to determine whether this Hal match is against a property found in another
	 * HAL object, referred to by its UDI.
	 * @return whether this Hal match is against a property found in another HAL object, referred to by its UDI.
	 */
	public final boolean matchNextObjectValue() {
		return object.charAt(0) == '/';
	}
	
	/**
	 * In the case where this HAL match object is against a property found in another HAL object, this method will
	 * return the HAL match object or throw an exception if this HAL match object is against the current HAL object.
	 * @return the next HAL match object
	 * @throws InvalidMethodCall if this HAL match object is against the current HAL object (there is no "next
	 * HAL match object").
	 */
	public final HalMatchInterface getNextMatch() throws  InvalidMethodCall{
		if(matchNextObjectLink() || matchNextObjectValue())
			return nextMatch;
		throw new InvalidMethodCall();
	}

	/**
	 * In the case where this HAL match object is against a property found in another HAL object referred to by a link, 
	 * this method will return the name of the property (in the current HAL object) where the UDI of the next HAL object
	 * is found. This method throws an exception if this HAL match object is against the current HAL object or another
	 * HAL object referred to using its name.
	 * @return the name of the property (in the current HAL object) where the UDI of the next HAL object is found.
	 * @throws InvalidMethodCall if this HAL match object is against the current HAL object (there is no "next
	 * HAL match object"), or the next HAL object is referred to using its name (not a link).
	 */
	public final String getNextObjectLink() throws InvalidMethodCall {
		if (matchNextObjectLink())
			return object.substring(1);
		throw new InvalidMethodCall();
	}

	/**
	 * In the case where this HAL match object is against a property found in another HAL object referred to by its UDI,
	 * This method will return the UDI of the next HAL object. This method throws an exception if this HAL match object
	 * is against the current HAL object or another HAL object referred to using a link.
	 * @return the UDI of the next HAL object.
	 * @throws InvalidMethodCall if this HAL match object is against the current HAL object (there is no "next HAL match
	 * object"), or the next HAL object is referred to using a link (not its UDI).
	 */
	public final String getNextObjectValue() throws InvalidMethodCall {
		if (matchNextObjectValue())
			return object;
		throw new InvalidMethodCall();
	}

	/**
	 * In the case where this HAL match object is against the current HAL object,this method will return the name of the
	 * property to be matched. This method throws an exception if this HAL match object is against another HAL object.
	 * @return the name of the property to be matched.
	 * @throws InvalidMethodCall if this HAL match object is against against another HAL object.
	 */
	public final String getPropName() throws InvalidMethodCall {
		if (matchThisObject())
			return propName;
		throw new InvalidMethodCall();
	}

	/**
	 * This method returns the name of this HAl match object.
	 */
	public final String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((name == null) ? 0 : name.hashCode());
		result = PRIME * result + (negate ? 1231 : 1237);
		result = PRIME * result + ((nextMatch == null) ? 0 : nextMatch.hashCode());
		result = PRIME * result + ((object == null) ? 0 : object.hashCode());
		result = PRIME * result + ((propName == null) ? 0 : propName.hashCode());
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
		final AbstractMatch other = (AbstractMatch) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (negate != other.negate)
			return false;
		if (nextMatch == null) {
			if (other.nextMatch != null)
				return false;
		} else if (!nextMatch.equals(other.nextMatch))
			return false;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		if (propName == null) {
			if (other.propName != null)
				return false;
		} else if (!propName.equals(other.propName))
			return false;
		return true;
	}
}
