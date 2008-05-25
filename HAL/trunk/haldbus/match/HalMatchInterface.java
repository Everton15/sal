package haldbus.match;

import javax.naming.ConfigurationException;


public interface HalMatchInterface {
	/**
	 * This method returns the name associated with this match
	 * @return the name associated with this match
	 */
	public String getName();
	
	/**
	 * This method checks the given object o to see whether or not it matches.
	 * If it does, this method returns a string which will be associated with the name
	 * of this match (as retrieved with <code>getName()</code>)
	 * @param o the object to check
	 * @return a string to be associated with the name of this match
	 * @throws ConfigurationException if there is no match
	 */
	public String match(Object o) throws ConfigurationException;
	
	/**
	 * This method returns true when this match applies to a property found in  the current HAL
	 * object. In this case, a call to <code>getPropName()</code> returns the name of the property
	 * to check and its value can be passed directly to <code>match()</code>. If this method returns
	 * false, <code>matchNextObjectLink()</code> and <code>matchNextObjectValue()</code> must be checked,
	 * and calling <code>getPropName()</code> will return an exception.
	 * @return true if the match applies to a property found in the current HAL object.
	 */
	public boolean matchThisObject();
	
	/**
	 * This method returns true when this match applies to a property found in another HAL
	 * object. This object's name  is stored in a property in the current HAL object. The name of this
	 * property is retrieved by calling <code>getNextObjectLink()</code>. When the properties of the other
	 * HAL objects are retrieved, they must be matched against the HalMatchInterface instance returned
	 * by <code>getNextMatch()</code>.If this method returns false, <code>getNextObjectLink()</code> 
	 * will return an exception.
	 * @return true if the match applies to properties found in the anothor HAL object. 
	 */
	public boolean matchNextObjectLink();
	
	/**
	 * This method returns true when this match applies to a property found in another HAL
	 * object. This object's name  is retrieved by calling <code>getNextObjectValue()</code>. When the
	 * properties of the other HAL objects are retrieved, they must be matched against the 
	 * HalMatchInterface instance returned by <code>getNextMatch()</code>.If this method returns
	 * false, <code>getNextObjectValue()</code> will return an exception.
	 * @return true if the match applies to properties found in the anothor HAL object. 
	 */
	public boolean matchNextObjectValue();
	
	/**
	 * This method returns the name of the property in the current HAL object which contains the name
	 * of the new HAL object whose properties must be checked (against the HalMatchInterface returned
	 * by <code>getNextMatch()</code>).
	 * @return the name of the property in the current HAL object which contains the name of the new
	 * HAL object whose properties must be checked.
	 * @throws ConfigurationException if this match doesnt apply to another HAL object accessible through a link
	 */	
	public String getNextObjectLink() throws ConfigurationException;
	
	/**
	 * This method returns the name a new HAL object whose properties must be checked (against the
	 * HalMatchInterface returned by <code>getNextMatch()</code>).
	 * @return the name a new HAL object whose properties must be checked
	 * @throws ConfigurationException if this match doesnt apply to another HAL object accessible through its name
	 */	
	public String getNextObjectValue() throws ConfigurationException;
	
	/**
	 * If this match applies to another HAL object accessible either through its name or a link, this method
	 * returns the HalMatchInterface against which the properties of the other HAL object must be checked 
	 * @return the HalMatchInterface against which the properties of the other HAL object must be checked
	 * @throws ConfigurationException it this match doesnt apply to another HAL object
	 */	
	public HalMatchInterface getNextMatch()throws ConfigurationException;
	
	/**
	 * If this match applies to the current HAL object, this method returns the name of a property whose value
	 * must be passed to <code>match(Object o)</code> to check for a possible match.
	 * @return the name of a property whose value must be checked
	 * @throws ConfigurationException it this match doesnt apply to the current HAL object
	 */
	public String getPropName() throws ConfigurationException;

}
