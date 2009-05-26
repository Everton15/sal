package au.edu.jcu.haldbus.filter;

import java.util.Map;
import java.util.TreeMap;

import au.edu.jcu.haldbus.exceptions.AddRemoveElemException;
import au.edu.jcu.haldbus.match.HalMatchInterface;

/**
 * This class implements the basic features found in all HAL clients interested in receiving specific
 * DeviceAdded/DeviceRemoved notifications. This abstract class implements mainly the management of 
 * the Match list. It also provides generic
 * <code>getMinMatches()</code> and <code>getMaxMatches()</code>, which simply return the number of
 * elements in the match list. These can be overridden by subclasses.
 * @author gilles
 *
 */
public abstract class AbstractHalFilter implements HalFilterInterface {
	/**
	 * the list of match objects
	 */
	private Map<String, HalMatchInterface> list;
	private int whenFlags;
	private String name;
	
	/**
	 * Using this flag, match objects in this HAL client will be matched only during
	 * the initial run, ie only once at startup. There will no be any notifications
	 * upon subsequent connections/disconnections.
	 */
	public final static int INITIAL_RUN_FLAG = 1;
	
	/**
	 * Using this flag, match objects in this HAL client will be matched only during
	 * the subsequent run, ie there will be no notifications at startup for existing
	 * matching objects.
	 */
	public final static int SUBSEQUENT_RUN_FLAG = 2;
	
	/**
	 * Using this flag, match objects in this HAL client will be matched both during
	 * the initial & subsequent runs.
	 */
	public final static int ALWAYS_RUN_FLAG = (INITIAL_RUN_FLAG | SUBSEQUENT_RUN_FLAG);
	

	/**
	 * This method creates the AbstractDeviceDetection, initialises the map and sets
	 * the name of this filter and the execution flags
	 * @param n the name of this filter
	 * @param when the execution flag: ({@link #INITIAL_RUN_FLAG} will match this filter
	 * only once in the initial run at startup, {@link #SUBSEQUENT_RUN_FLAG} will match this filter only after startup
	 * (ie, no notification for already connected devices at startup), {@link #ALWAYS_RUN_FLAG} for both 
	 */
	protected AbstractHalFilter(String n, int when){
		list = new TreeMap<String,HalMatchInterface>();
		whenFlags = when;
		name = n;
	}
	
	/**
	 * This method creates the AbstractDeviceDetection and initialises the map. The execution flag is set to
	 * {@link #ALWAYS_RUN_FLAG}.
	 * (both in the initial and subsequent runs)
	 * @see AbstractDeviceDetection#AbstractDeviceDetection(String, int).
	 */
	protected AbstractHalFilter(String n){
		this(n, ALWAYS_RUN_FLAG); 
	}
	
	/**
	 * This method adds a new HalMatch object to the map. The ordering of HalMatch objects is important. Matches most likely
	 * to fail should be first, while those unlikely to fail should be last. Influencing the ordering of an HalMatch object
	 * is done by adjusting the name associated with this HalMatch object. A naming convention such as the following should
	 * be applied: Start each name with a two-digit number, then a dash, followed by any name. That way changing the number
	 * will change the rank of a HalMatch object. 
	 * @param name the name associated with the HalMatch
	 * @param m the HalMatch object
	 * @throws AddRemoveElemException if the given name is already associated with a match 
	 */
	protected final void addMatch(String name, HalMatchInterface m){
		if(list.containsKey(name))
			throw new AddRemoveElemException("Match '"+name+"' already present");
		list.put(name, m);
	}

	/**
	 * This method removes a HalMatch object from the map.
	 * @param name the name associated with the HalMatch to be removed
	 * @throws AddRemoveElemException if the given name is doesnt exist 
	 */
	protected final void removeMatch(String name){
		if(!list.containsKey(name))
			throw new AddRemoveElemException("No match with name '"+name+"'");
		list.remove(name);
	}
	
	@Override
	public final String getName(){
		return name;
	}

	@Override
	public final Map<String, HalMatchInterface> getMatchList() {
		return list;
	}	

	@Override
	public int getMaxMatches() {
		return list.size();
	}

	@Override
	public int getMinMatches() {
		return list.size();
	}
	
	@Override
	public final int countMatches() {
		return list.size();
	}
	
	@Override
	public final boolean initialMatch(){
		return (whenFlags & INITIAL_RUN_FLAG)!=0;
	}
	
	@Override
	public final boolean subsequentMatch(){
		return (whenFlags & SUBSEQUENT_RUN_FLAG)!=0;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((list == null) ? 0 : list.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + whenFlags;
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
		AbstractHalFilter other = (AbstractHalFilter) obj;
		if (list == null) {
			if (other.list != null)
				return false;
		} else if (!list.equals(other.list))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (whenFlags != other.whenFlags)
			return false;
		return true;
	}

}
