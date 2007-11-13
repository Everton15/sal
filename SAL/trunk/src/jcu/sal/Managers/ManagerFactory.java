/**
 * 
 */
package jcu.sal.Managers;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.naming.ConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jcu.sal.Components.HWComponent;
import jcu.sal.Components.Identifiers.Identifier;
import jcu.sal.utils.Slog;
import jcu.sal.utils.XMLhelper;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * Creates manager classes, which create, delete and manage components (Endpoints, Protocol, ...)
 * @author gilles
 *
 */
public abstract class ManagerFactory<T extends HWComponent> {
	
	public static String COMPONENTPARAM_TAG = "Param";
	
	private Logger logger = Logger.getLogger(ManagerFactory.class);
	protected Hashtable<Identifier, T> ctable;
	
	public ManagerFactory() {
		Slog.setupLogger(this.logger);
		ctable = new Hashtable<Identifier, T>();
	}
	/**
	 * Creates the component from a DOM document
	 * @param n the DOM document
	 * @return the component
	 * @throws InstantiationException 
	 */
	protected abstract T build(Node n) throws InstantiationException;
	
	/**
	 * Deletes the component and give the subclass a chance to turn things off properly
	 * @param component the component
	 */
	protected abstract void remove(T component);
	
	/**
	 * returns the name of a component from its DOM document
	 * @param n the DOM document
	 * @return the ID of the component
	 */
	protected abstract Identifier getComponentID(Node n) throws ParseException;
	
	/**
	 * returns the configuration directives for this component
	 * @param n the DOM document
	 * @return the config directives in a hastable
	 */
	protected Hashtable<String, String> getComponentConfig(Node n){
		ArrayList<String> xml = null;
		Hashtable<String, String> config = new Hashtable<String, String>();
		String name = null, value = null;
		
		try {
			xml = XMLhelper.getAttributeListFromElements("//" + COMPONENTPARAM_TAG, n);			
			Iterator<String> iter = xml.iterator();
			while(iter.hasNext()) {
				iter.next();
				name = iter.next();
				iter.next();
				value = iter.next();
				config.put(name,value);
			}
		} catch (XPathExpressionException e) {
			this.logger.error("Did not find any parameters for this Sensor");
		}
		return config;
	}
	
	/**
	 * Create a new instance of a fully configured component from its DOM document
	 * @param n the XML configuration node
	 * @return an instance of the component
	 * @throws ConfigurationException if the component's XML configuration is invalid and the component can not be created
	 */
	public T createComponent(Node n) throws ConfigurationException {
		T newc = null;
		Identifier id= null;
		try {
			id = getComponentID(n);
			this.logger.debug("About to create a component of type " + id.getType() + " named " + id.getName());
			if(!ctable.containsKey(id)) {
				newc = build(n);
				if(newc!=null) ctable.put(id, newc);
				else this.logger.error("Couldnt create component "+ id.getType());
			}
			else {
				this.logger.error("Couldnt create component "+ id.getType()+", it already exist");
				return null;
			}
		} catch (ParseException e) {
			this.logger.error("Couldnt parse component "+ id.getType()+"'s XML doc");
			throw new ConfigurationException();
		} catch (InstantiationException e) {
			this.logger.error("Couldnt instanciate component "+ id.getType()+" from XML doc");
			throw new ConfigurationException();
		}

		return newc; 
	}
	
	/** 
	 * Removes a previoulsy creatd component
	 * @param type the component type
	 */
	public void destroyComponent(Identifier i) {
		this.logger.debug("About to remove element " + i.toString());
		if(ctable.containsKey(i)) {
			dumpTable();
			remove(ctable.get(i));
			if(ctable.remove(i) == null)
				this.logger.error("Cant remove element with key " + i.toString() +  ": No such element");
			else
				this.logger.debug("Element " + i.toString()+ " Removed");
		} else
			this.logger.error("Element " + i.toString()+ " doesnt exist and can NOT be removed");
	}
	
	/** 
	 * Removes a previoulsy creatd component
	 * @param component the component to be removed
	 */
	public void destroyComponent(T component) {
		Identifier i = null;
		if(ctable.containsValue(component)) {
			Enumeration<Identifier> keys = ctable.keys();
			Collection<T> cvalues = ctable.values();
			Iterator<T> iter = cvalues.iterator();
			while ( keys.hasMoreElements() &&  iter.hasNext()) {
				i = keys.nextElement();
				if(iter.next() == component) {
					destroyComponent(i);
					break;
				}
			}
		} else 
			this.logger.debug("Cant remove element "+ component.toString() + ": No such element");
	}
	
	/** 
	 * Removes all previoulsy creatd components
	 *
	 */
	public void destroyAllComponents() {
		this.logger.debug("removing all components" );
		Collection<T> cvalues = ctable.values();
		Iterator<T> iter = cvalues.iterator();
		while (iter.hasNext())
			destroyComponent(iter.next());
	}
	
	/** 
	 * Get a component based on its identifer
	 * @param i the identifier
	 * @return the component or null if the identifier does not map to anything
	 *
	 */
	public T getComponent(Identifier i) {
		return ctable.get(i);
	}
	
	/** 
	 * Get an iterator on all components
	 * @return the iterator
	 *
	 */
	public Iterator<T> getIterator() {
		return ctable.values().iterator();
	}
	
	/**
	 * Prints the content of this manager's component table
	 *
	 */	
	public void dumpTable() {
		this.logger.debug("current table contents:" );
		Enumeration<Identifier> keys = ctable.keys();
		Collection<T> cvalues = ctable.values();
		Iterator<T> iter = cvalues.iterator();
		while ( keys.hasMoreElements() &&  iter.hasNext())
		   this.logger.debug("key: " + keys.nextElement().toString() + " - "+iter.next().toString());
	}

}
