/**
 * 
 */
package jcu.sal.components;



/**
 * @author gilles
 *
 */
public abstract class AbstractIdentifier implements Identifier {
	
	/** the name of the component */
	protected String name;

	/**
	 * Construct a new Identifier with the specified name and type 
	 * @param name the name
	 * @param type the type
	 */
	public AbstractIdentifier(String name) { 
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.Identifiers.Identifier#getName()
	 */
	public final String getName() { return name; }
	
	/* (non-Javadoc)
	 * @see jcu.sal.components.Identifiers.Identifier#equals(java.lang.String)
	 */
	public boolean equals(Object id) {
		/* sanity check */
		if(id == null) return false;
		else if(id == this) return true;
		else 
			if(id.getClass().getSuperclass().getName().equals("jcu.sal.components.AbstractIdentifier"))
				return (id.toString().equals(this.toString()));
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.components.Identifiers.Identifier#hashCode(java.lang.String)
	 */
	public int hashCode(){
		// TODO improve this !
		return name.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.components.Identifiers.Identifier#toString(java.lang.String)
	 */
	public String toString(){
		return name;
	}

}
