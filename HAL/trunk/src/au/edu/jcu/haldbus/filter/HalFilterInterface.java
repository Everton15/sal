package au.edu.jcu.haldbus.filter;


import java.util.Map;

import au.edu.jcu.haldbus.match.HalMatchInterface;

/**
 * This interface defines required methods in all Hal clients interested in filtering HAL objects. Clients typically
 * provide a list of match objects implementing the <code>HalMatchInterface</code>. Each object is used to filter the
 * list of properties associated with a newly added/removed HAL object. When all the match objects have been tested,
 * {@link #deviceAdded(Map)} is called if the number of matches is between {@link #getMinMatches()} and
 * {@link #getMaxMatches()}.<br>
 * The use case goes as follows:
 * <ul>
 * <li>A list of HAL match objects/names is retrieved with {@link #getMatchList()}. Each match object specifies
 * a single HAL property that is to be checked and its expected values.</li>
 * <li>Then, for each device to be filtered, each HAL match object is tested against each of the device's property.
 * For each correct match, the resulting string is mapped to the name of the HAL match, and stored in a result map
 * object</li>
 * <li>After checking all the HAL match objects against each HAL device, if there are at least
 * {@link #getMinMatches()} and at most {@link #getMaxMatches()} correct matches, either
 * {@link #deviceAdded(Map)} or {@link #deviceRemoved(Map)} is invoked with the previously created 
 * result map object as unique argument. </li>
 * </ul>
 * @author gilles
 *
 */
public interface HalFilterInterface {
	/**
	 * This method returns true if matches in this object are to be tested during the initial run.  The initial run
	 * is done by the Hal helper once on startup to detect already-connected devices. 
	 * @return true if matches in this object are to be tested during the initial run
	 */	
	public boolean initialMatch();
	
	/**
	 * This method returns true if matches in this object are to be tested after the initial run.  The initial run
	 * is done by the Hal helper once on startup to detect already-connected devices. 
	 * @return true if matches in this object are to be tested after the initial run
	 */	
	public boolean subsequentMatch();
	
	/**
	 * This method returns a map of names and HalMatchInterfaces associated with this client.
	 * @return a map of HalMatchInterfaces associated with this client
	 */
	public Map<String, HalMatchInterface> getMatchList();
	
	/**
	 * When a new device is added, this method is executed if there is at least {@link #getMinMatches()}
	 * and at most {@link #getMaxMatches()}correct matches.
	 * @param l a map of matched strings identified by the {@link HalMatchInterface}'s name
	 */
	public void deviceAdded(Map<String,String> l);
	
	/**
	 * When an existing device is removed, this method is executed iff {@link #deviceAdded(Map)} was executed for that
	 * same device.
	 * @param l a map of matched strings identified by the {@link HalMatchInterface}'s name
	 */
	public void deviceRemoved(Map<String,String> l);
	
	/**
	 * This method returns the name of this client
	 * @return the name of this client
	 */
	public String getName();
	
	/**
	 * This method returns the minimum number of matches required
	 * @return the minimum number of matches required  
	 */
	public int getMinMatches();
	
	/**
	 * This method returns the maximum number of matches required
	 * @return the minimum number of matches required  
	 */
	public int getMaxMatches();
	
	/**
	 * This method returns the total number of matches in this filter
	 * @return the number of matches  
	 */
	public int countMatches();
	
	public int hashCode();
	public boolean equals(Object obj);
}
