/**
 * 
 */
package jcu.sal.managers;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jcu.sal.common.Parameters;
import jcu.sal.common.Slog;
import jcu.sal.common.exceptions.ComponentInstantiationException;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.pcml.HWComponentConfiguration;
import jcu.sal.components.HWComponent;
import jcu.sal.components.Identifier;
import jcu.sal.components.componentRemovalListener;

import org.apache.log4j.Logger;

/**
 * Creates manager classes, which create, delete and manage components (Endpoints, AbstractProtocol, ...)
 * @author gilles
 *
 */
public abstract class AbstractManager<T extends HWComponent, U extends HWComponentConfiguration> implements componentRemovalListener {
	private static Logger logger = Logger.getLogger(AbstractManager.class);
	static { Slog.setupLogger(logger); }
	
	/**
	 * This table stores mappings of Identifiers and associated components
	 */
	protected Map<Identifier, T> ctable;
	
	/**
	 * This table stores mappings between component types and identifiers
	 */
	private Map<String, List<Identifier>> typeMap;
	
	public AbstractManager() {
		ctable = new Hashtable<Identifier, T>();
		typeMap = new Hashtable<String, List<Identifier>>();
	}

	/**
	 * returns the configuration directives for this component
	 * @param c the configuration object for this component
	 * @return the Parameters associated with this component
	 */
	protected Parameters getComponentConfig(U c){
		return c.getParameters();
	}
	
	/**
	 * Create a new instance of a fully configured component from its DOM document
	 * @param c the configuration object for the component
	 * @return an instance of the component
	 * @throws ConfigurationException if the component can not be created
	 */
	public T createComponent(U c) throws ConfigurationException {
		T newc = null;
		String type = c.getType();
		Identifier id;
		try {
			synchronized(ctable) {
				id = getComponentID(c);
				if(!ctable.containsKey(id)) {
					newc = build(c, id);
					if(newc!=null) {
							//store new component
							ctable.put(newc.getID(), newc);
							//store new type
							if(typeMap.get(type)==null)
								typeMap.put(type, new LinkedList<Identifier>());
							typeMap.get(type).add(id);
							
							return newc;						
					} else {
							logger.error("Couldnt create component");
							throw new ConfigurationException();
					}
				}
			}
			//if we re here the table already has a component with this name 
			logger.error("A component '"+type+"' named " + id.toString()+" is already present");
			throw new ConfigurationException("A identical component is already instanciated");
		} catch (ComponentInstantiationException e) {
			logger.error("Couldnt instanciate component '"+type+"' named " + c.getID()+" from XML doc");
			throw new ConfigurationException("Error instanciating the component '"+type+"' named " + c.getID(),e );
		}
	}
	
	/** 
	 * Removes a previously created component
	 * @param type the component type
	 * @throws NotFoundException if the identifier doesnt match any component
	 */
	public void destroyComponent(Identifier i) throws NotFoundException {
		synchronized(ctable) {
			T t = ctable.get(i);
			if(t != null) {
				typeMap.get(t.getType()).remove(i);
				remove(t);
			} else {
				logger.error("Component " + i.toString()+ " doesnt exist and can NOT be removed");
				throw new NotFoundException("Component " + i.toString()+ " doesnt exist and can NOT be removed");
			}
		}
	}
	
	/** 
	 * Removes previously created components
	 * @param l the list of components to be deleted
	 */
	public void destroyComponents(List<T> l){
		synchronized(ctable) {
			for(T t: l)
				try{destroyComponent(t.getID());}
				catch (NotFoundException e) {
					logger.error("Cant destroy "+t.toString());
				}
		}
	}
	
	/** 
	 * Removes all previously created components
	 *
	 */
	public void destroyAllComponents() {
		synchronized(ctable){
			/**
			 * the copy of ctable is required here since calling destroyComponent() will remove the component from the table
			 * and this iterator wont like it...
			 */
			for(T t: new Vector<T>(ctable.values()))
				try { destroyComponent(t.getID());}
				catch (NotFoundException e1) {}
		}
	}
	
	/** 
	 * Get a component based on its identifier
	 * @param i the identifier
	 * @return the component or null if the identifier does not map to anything
	 *
	 */
	public T getComponent(Identifier i) {
		return ctable.get(i);
	}
	

	/**
	 * Removes the specified component
	 */
	public void componentRemovable(Identifier i){
		synchronized(ctable){
			if(ctable.remove(i) == null)
				logger.error("Cant remove element with key " + i.toString() +  ": No such element");
			//else
				//logger.debug("Element " + i.toString()+ " Removed");
		}
	}
	
	/**
	 * This method returns a list of components Identifiers of the given type
	 * @param t the type
	 * @return the list of Identifiers
	 * @throws NotFoundException if the given type is not found
	 */
	public List<Identifier> getComponentsOfType(String t) throws NotFoundException{
		List<Identifier> l;
		synchronized(ctable){
			if((l=typeMap.get(t))==null)
				throw new NotFoundException("No components of type '"+t+"' found");

			return new LinkedList<Identifier>(l);
		}
	}
	
	/**
	 * Instantiates a component
	 * @param c the configuration object
	 * @param id the identifier for the new component
	 * @return the component
	 * @throws ComponentInstantiationException 
	 */
	protected abstract T build(U c, Identifier id) throws ComponentInstantiationException;
	
	/**
	 * Deletes the component and give the subclass a chance to turn things off properly
	 * @param component the component
	 */
	protected abstract void remove(T component);
	
	/**
	 * returns the ID of a component from its configuration object
	 * @param c the configuration object
	 * @return the ID of the component
	 */
	protected abstract Identifier getComponentID(U c);
}
