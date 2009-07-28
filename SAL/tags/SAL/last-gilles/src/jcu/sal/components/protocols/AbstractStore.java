package jcu.sal.components.protocols;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import jcu.sal.common.Slog;
import jcu.sal.common.cml.CMLArgument;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.CMLDescription;
import jcu.sal.common.cml.CMLDescriptions;
import jcu.sal.common.cml.ResponseType;
import jcu.sal.common.cml.CMLDescription.SamplingBounds;
import jcu.sal.common.exceptions.AlreadyPresentException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALRunTimeException;

import org.apache.log4j.Logger;

/**
 * Defines the base behaviour of AbstractStore objects
 * Each CML store (class which stores tables of CML docs for each valid command 
 * of a sensor managed by a protocol) must extend this class. CMLstores should be 
 * instantiated by protocols in order to avoid duplicates, and save memory
 * @author gilles
 *
 */
public abstract class AbstractStore {
	
	private static Logger logger = Logger.getLogger(AbstractStore.class);
	static{Slog.setupLogger(logger);}
	

	public static final String GENERIC_ENABLE="enable";			//10
	public static final String GENERIC_DISABLE="disable";		//11
	public static final String GENERIC_GETREADING="getReading";	//100

	public static final String GENERIC_GETTEMP="getTemperature";	//110
	public static final String GENERIC_GETHUM="getHumidity";		//111
	public static final String GENERIC_GETACCEL="getAccel";		//112
	public static final String GENERIC_GETLUX="getLux";		//113
	
	public static final int GENERIC_ENABLE_CID=10;
	public static final int GENERIC_DISABLE_CID=11;
	public static final int GENERIC_GETREADING_CID=100;
	public static final int GENERIC_GETTEMP_CID=110;
	public static final int GENERIC_GETHUM_CID=111;
	public static final int GENERIC_GETACCEL_CID=112;
	public static final int GENERIC_GETLUX_CID=113;
	
	private static int PRIVATE_CID_START = 1000;
	
	/**
	 * the table where CML docs are stored. Which key is used is up to the protocol.
	 * Native address, sensor families do the job, as long as they re unique !
	 * The table must be filled by the subclass constructor.
	 */
	private Hashtable<String, Hashtable<Integer, CMLDescription>> cmls;
	/**
	 * This table contains the next available private cid
	 */
	private Hashtable<String,Integer> priv_cid;
	
	
	/**
	 * No arg constructor
	 */
	protected AbstractStore() {
		cmls = new Hashtable<String, Hashtable<Integer, CMLDescription>>();
		priv_cid = new Hashtable<String, Integer>();
	}

	/**
	 * Retrieves the CML descriptions document for the given key f (native address, sensor family, ...)
	 * @param k the key
	 * @return the CML command descriptions doc 
	 * @throws NotFoundException if the key can not be found 
	 */
	public final CMLDescriptions getCMLDescriptions(String k) throws NotFoundException{
		if(!cmls.containsKey(k)) {
			logger.error("Cant find key "+k);
			throw new NotFoundException("Cant find key "+k);
		}
				
		try {
			return new CMLDescriptions(cmls.get(k).values());
		} catch (AlreadyPresentException e) {
			logger.error("We shouldnt be here - duplicate CML descriptions in the CML store");
			e.printStackTrace();
			throw new SALRunTimeException("Duplicate CML descriptions in CML store", e);
		}
	}
	
	/**
	 * Retrieves the method name associated with a given command
	 * @param k the key 
	 * @param cid the command id
	 * @return the method name
	 * @throws NotFoundException if the method can not be found 
	 */
	public final String getMethodName(String k, int cid) throws NotFoundException{
		Hashtable<Integer, CMLDescription> t = cmls.get(k);
		if(t==null) {
			logger.error("Cant find key "+k);
			throw new NotFoundException("Cant find key "+k);
		}
		CMLDescription d = t.get(new Integer(cid));
		if(d==null){
			logger.error("Cant find the cid "+cid);
			throw new NotFoundException("Cant find the cid "+cid);
		}
		return d.getMethodName();
	}
	
	/**
	 * This method adds a new private command to the CML doc for a given key.
	 * @param k the key with which the new command is to be associated
	 * @param mName the name of the method to be called when this command is received
	 * @param name the name of the command
	 * @param desc the short description of the command
	 * @param args the list of {@link CMLArgument} for this command description, can be <code>null</code>
	 * @param b the sampling frequency bounds, or null if this command cannot be streamed
	 * @return the cid associated with this command
	 * @throws AlreadyPresentException if the given key already has a CML table
	 */
	public final int addPrivateCommand(String k, String mName, String name, 
			String desc, List<CMLArgument> args, ResponseType returnType, SamplingBounds b) throws AlreadyPresentException {
		//computes the CID
		Integer cid = priv_cid.get(k);
		if(cid==null)
			cid = new Integer(PRIVATE_CID_START);
		//builds the CML desc doc
		
		//logger.debug("Adding private CML for key "+k+", method: "+mName+", CID: "+cid.intValue());
		addCML(k, new CMLDescription(mName,cid, name, desc, args, returnType, b));
		priv_cid.put(k, new Integer(cid.intValue()+1));
				
		return cid.intValue();
	}
	
	/**
	 * This method adds an alias to a previously created private command 
	 * identified by its cid
	 * @param k the key with which the alias is to be associated
	 * @param name the name of the alias (AbstractStore.GENERIC_*)
	 * @param cid the previously created private command
	 * @return the cid associated with this command 
	 * @throws AlreadyPresentException if a command with the same cid already exists
	 * @throws NotFoundException if the given aliasNAme doesnt exist, if it doesnt refer to an existing command or if the key is invalid
	 */
	public final int addGenericCommand(String k, String aliasName, int cid) throws NotFoundException, AlreadyPresentException{
		//computes the CID
		Integer c;
		CMLDescription cml;

		//logger.debug("Adding generic CML for key "+k+", alias: "+aliasName);
		if(aliasName.equals(GENERIC_ENABLE)){
			c = new Integer(GENERIC_ENABLE_CID);
			addCML(k, new CMLDescription(null, c, GENERIC_ENABLE, "Enables the sensor", new Vector<CMLArgument>(), new ResponseType(CMLConstants.RET_TYPE_VOID)));
			return c.intValue();
		} else if(aliasName.equals(GENERIC_DISABLE)){
			c = new Integer(GENERIC_DISABLE_CID);
			addCML(k, new CMLDescription(null, c, GENERIC_DISABLE, "Disables the sensor", new Vector<CMLArgument>(), new ResponseType(CMLConstants.RET_TYPE_VOID)));
			return c.intValue();
		} else if(aliasName.equals(GENERIC_GETREADING)){
			c = new Integer(GENERIC_GETREADING_CID);
		} else if(aliasName.equals(GENERIC_GETTEMP)){
			c = new Integer(GENERIC_GETTEMP_CID);
		} else if(aliasName.equals(GENERIC_GETHUM)){
			c = new Integer(GENERIC_GETHUM_CID);
		} else if(aliasName.equals(GENERIC_GETACCEL)){
			c = new Integer(GENERIC_GETACCEL_CID);
		} else if(aliasName.equals(GENERIC_GETLUX)){
			c = new Integer(GENERIC_GETLUX_CID);
		} else {
			logger.error("We shouldnt be here - Cant create an alias, no such name");
			throw new SALRunTimeException("No such alias name '"+aliasName+"'");
		}
		
		Hashtable<Integer, CMLDescription> table = cmls.get(k);
		if(table != null) {
			cml = table.get(new Integer(cid));
			if(cml==null){
				logger.error("Cant find any pre-existing command "+cid+" to create the alias");
				throw new NotFoundException("Cant find any pre-existing command "+cid+" to create the alias");
			}
		} else {
			logger.error("Cant find key "+k);
			throw new NotFoundException("Cant find key "+k);
		}

		//builds the CML desc doc based on the existing one
		addCML(k, new CMLDescription(c, aliasName, cml));
				
		return cid-1;
	}
	
	/**
	 * Adds a CML document to the CML table for a given sensor
	 * @param k the sensor
	 * @param v the CML doc
	 * @throws AlreadyPresentException 
	 */
	private void addCML(String k, CMLDescription v) throws AlreadyPresentException{
		Hashtable<Integer, CMLDescription> t = cmls.get(k);
		if(t==null)
			t = addSensor(k);
		if(!t.containsKey(v.getCID()))
			t.put(v.getCID(), v);
		else {
			logger.error("trying to add a CML doc (cid:"+v.getCID()+") to sensor " + k + " which already holds a CML with this id.");
			throw new AlreadyPresentException("sensor " + k + " already holds a CML with this id");
		}
		if(false) dumpCML(k);
	}

	/**
	 * Creates a new CML list for a new sensor identified by its key k
	 * @param k the sensor key
	 * @throws AlreadyPresentException if the sensor key already exists
	 */
	private Hashtable<Integer, CMLDescription> addSensor(String k) throws AlreadyPresentException {
		Hashtable<Integer, CMLDescription> t;
		if(cmls.containsKey(k)){
			logger.error("trying to add a CML table for sensor " + k + " which already has a CML table.");
			throw new AlreadyPresentException("sensor " + k + " which already has a CML table");
		}

		t = new Hashtable<Integer, CMLDescription>();
		
		/* create the table */ 
		cmls.put(k, t);

		/* Add generic CML docs to this sensor */
		/* generic enable command */
		try {
			addGenericCommand(k, GENERIC_ENABLE, 0);
		} catch (Exception e) {
			logger.error("We shouldnt be here - cant add generic ENABLE command to sensor '"+k+"'");
			throw new SALRunTimeException("Cant add generic ENABLE command to sensor '"+k+"'");
		} 
		
		/* generic disable command */
		try {
			addGenericCommand(k, GENERIC_DISABLE, 0);
		} catch (Exception e) {
			logger.error("We shouldnt be here - cant add generic DISABLE command to sensor '"+k+"'");
			throw new SALRunTimeException("Cant add generic DISABLE command to sensor '"+k+"'");
		}
		
		return t;

	}
	
	public void dumpCML(String k){
		Hashtable<Integer, CMLDescription> t = cmls.get(k);
		Integer i;
		if(t!=null){
			Enumeration<Integer> e = t.keys();
			logger.debug("CML store dump:");
			while(e.hasMoreElements()){
				i=e.nextElement();
				logger.debug("CML Doc CID: "+i.intValue());
				logger.debug("\tMethod name: "+t.get(i).getMethodName());
				logger.debug("#############################CML DOC:");
				logger.debug(t.get(i).getXMLString());
				logger.debug("#############################\n");
			}
		} else {
			logger.debug("No such key in cmlstore");
		}
	}

}
