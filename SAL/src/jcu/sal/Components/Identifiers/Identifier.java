package jcu.sal.Components.Identifiers;

public interface Identifier {
	
	/**
	 * Returns the name of a component from its identifier
	 * @return a string representation of the identifier
	 */
	public String getName();
	
	/**
	 * Sets the name of a component from a string
	 * @param name the name of the component
	 */
	public void setName(String name);

	/**
	 * Test whether two identifiers are the same
	 * @param id the identifier to be tested
	 * @return ture or false
	 */
	public boolean equals(Identifier id);
	
	/**
	 * returns a string representation of the identifier
	 * @return the string representation of the identifier
	 */
	public String toString();
}
