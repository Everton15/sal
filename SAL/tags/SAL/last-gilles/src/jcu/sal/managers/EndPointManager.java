/**
 * 
 */
package jcu.sal.managers;

import java.lang.reflect.Constructor;

import jcu.sal.common.Slog;
import jcu.sal.common.exceptions.ComponentInstantiationException;
import jcu.sal.common.pcml.EndPointConfiguration;
import jcu.sal.components.Identifier;
import jcu.sal.components.EndPoints.EndPoint;
import jcu.sal.components.EndPoints.EndPointID;
import jcu.sal.config.plugins.PluginList;

import org.apache.log4j.Logger;

/**
 * @author gilles
 * 
 */
public class EndPointManager extends AbstractManager<EndPoint, EndPointConfiguration> {
	private static Logger logger = Logger.getLogger(EndPointManager.class);
	static {Slog.setupLogger(logger);}
	
	private static EndPointManager e = new EndPointManager();	
	
	/**
	 * Private constructor
	 */
	private EndPointManager() {
		super();
	}
	
	/**
	 * Returns the instance of the EndPointManager 
	 * @return
	 */
	public static EndPointManager getEndPointManager() {
		return e;
	}

	/* (non-Javadoc)
	 * @see jcu.sal.managers.ManagerFactory#build(org.w3c.dom.Document)
	 */
	@Override
	protected EndPoint build(EndPointConfiguration config, Identifier id) throws ComponentInstantiationException {
		EndPoint endPoint = null;
		String type=config.getType();
		EndPointID i = (EndPointID) id;
		try {
			//logger.debug("Building EndPoint type: " +type);
			String className = PluginList.getEndPointClassName(type);
			
			Class<?>[] p = new Class<?>[2];
			p[0] = EndPointID.class;
			p[1] = EndPointConfiguration.class;
			Constructor<?> c = Class.forName(className).getConstructor(p);
			Object[] o = new Object[2];
			o[0] = i;
			o[1] = config;
			endPoint = (EndPoint) c.newInstance(o);

			//logger.debug("Done building EndPoint " + endPoint.toString());
			
		} catch (Throwable e) {
			logger.error("Error in new Endpoint instanciation. XML doc:");
			logger.error(config.getXMLString());
			e.printStackTrace();
			throw new ComponentInstantiationException("Unable to instantiate component",e);
		}
		logger.debug("Created endPoint '"+i.getName()+"' - type: " +type);
		return endPoint;
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.managers.ManagerFactory#getComponentID(org.w3c.dom.Document)
	 */
	@Override
	protected Identifier getComponentID(EndPointConfiguration epc){
		return new EndPointID(epc.getID());
	}
	
	/* (non-Javadoc)
	 * @see jcu.sal.managers.ManagerFactory#remove(java.lang.Object)
	 */
	@Override
	protected void remove(EndPoint component) {
		component.stop();
		component.remove(this);
		logger.debug("Removed endPoint '"+component.getID().getName()+"' - type: " +component.getType());
	}
}
