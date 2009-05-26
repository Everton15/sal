package au.edu.jcu.haldbus;


import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.naming.ConfigurationException;

import org.freedesktop.Hal.Device;
import org.freedesktop.Hal.Manager;
import org.freedesktop.Hal.Manager.DeviceAdded;
import org.freedesktop.Hal.Manager.DeviceRemoved;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.Variant;

import au.edu.jcu.haldbus.examples.V4LHalFilter;
import au.edu.jcu.haldbus.exceptions.AddRemoveElemException;
import au.edu.jcu.haldbus.exceptions.DBusException;
import au.edu.jcu.haldbus.exceptions.MatchNotFoundException;
import au.edu.jcu.haldbus.filter.AbstractHalFilter;
import au.edu.jcu.haldbus.filter.HalFilterInterface;
import au.edu.jcu.haldbus.match.HalMatchInterface;

/**
 * A HardwareWatcher object maintains a list of client objects wishing to be 
 * notified when HAL signals for devices matching specific criteria are emitted 
 * by the HAL daemon. It runs a single thread which waits for HAL 
 * DeviceAdded/DeviceRemoved signals. When such a signal is received, a list of 
 * criteria (specified by all registered clients) is checked and in case of a 
 * match, the client is notified.
 * <br><br>
 * Clients are objects implementing the {@link HalFilterInterface}. Clients 
 * should extend the {@link AbstractHalFilter} class which encapsulates common
 * client behaviour ({@link AbstractHalFilter} implements the 
 * {@link HalFilterInterface}). Typically, clients specify three items: 
 * <ul>
 * <li>the criteria an HAL object must match</li>
 * <li>DeviceAdded & DeviceRemoved callback methods</li>
 * </ul>  
 * See {@link V4LHalFilter} for an example of a client.
 * <br><br>
 * Clients register/unregister with a HardwareWatcher using 
 * {@link #addClient(HalFilterInterface)} and 
 * {@link #removeClient(HalFilterInterface)}. 
 * When the HAL daemon emits a DeviceAdded or DeviceRemoved signal, the HAL 
 * object the signal refers to is matched against the list of 
 * {@link HalFilterInterface} clients. For each client, if enough 
 * criteria are matched, {@link HalFilterInterface#deviceAdded(Map)} is called. 
 * When a DeviceRemoved signal is received,
 * {@link HalFilterInterface#deviceRemoved(Map)} is called. In both cases, the 
 * given map contains the value of properties of interest as contained in the 
 * matching {@link HalFilterInterface}. This class is thread-safe.
 * @author gilles
 *
 */
@SuppressWarnings("unchecked")
public class HardwareWatcher implements Runnable, DBusSigHandler{
	private Thread t;
	private DBusConnection conn = null;
	private BlockingQueue<Map<String,Variant<Object>>> properties;
	private List<HalFilterInterface> clients;
	/**
	 * The following is a map of the UDI we have seen so far and their 
	 * associated successful matches. This map is required so that when a 
	 * DeviceRemoved signal is received, previous successful matches can be 
	 * notified of the DeviceRemoved signal. MatchingFilters are added to this 
	 * map when a DeviceAdded signal is received and the matches in these 
	 * filters are successful. The matches are stored with the UDI of the added 
	 * object. When this object is removed (DeviceRemoved signal), the same
	 * matches are passed to the filter. 
	 */
	private Map<String, List<MatchingFilter>> udiMatches;
	
	/**
	 * Default constructor. It initialises the new object's members.
	 * No clients are registered at this stage. This must be done by calling 
	 * {@link #addClient(HalFilterInterface)} subsequently;
	 */
	public HardwareWatcher(){
		t = null;
		properties = new LinkedBlockingQueue<Map<String,Variant<Object>>>();
		clients = new LinkedList<HalFilterInterface>();
		udiMatches = new Hashtable<String, List<MatchingFilter>>();
	}

	/**
	 * This method starts the watcher thread which intercepts HAL signals and
	 * matches them against client criteria. This method can be called 
	 * multiple times with no side effects if the thread is already started.
	 * @throws DBusException if there is an error connecting to DBus or 
	 * registering for HAL signals
	 */
	public synchronized void start() throws DBusException{
		if(t==null) {
			t = new Thread(this, "HalHelper thread");
			try {
				conn = DBusConnection.getConnection(DBusConnection.SYSTEM);
				conn.addSigHandler(Manager.DeviceAdded.class, this);
				conn.addSigHandler(Manager.DeviceRemoved.class, this);
			} catch (org.freedesktop.dbus.exceptions.DBusException e) {
				throw new DBusException(e);
			}
			initialCheck();
			t.start();
		}
	}
	
	/**
	 * This method stops the watcher thread. When this method returns, the 
	 * thread is guaranteed to have stopped. This method can be called 
	 * multiple times with no side effects if the thread is already stopped.
	 */
	public synchronized void stop(){
		if(t!=null && t.isAlive()) {
			t.interrupt();
			join();
			conn.disconnect();
			t = null;
		}
	}
	
	/**
	 * this method adds a new {@link HalFilterInterface} client to the list of 
	 * clients maintained in this object.
	 * @param f the new client to be added
	 * @throws AddRemoveElemException if the client already exists.
	 */
	public void addClient(HalFilterInterface f){
		synchronized (clients) {
			if(clients.contains(f))
				throw new AddRemoveElemException("Filter "+f.getName()
						+" already registered");
			clients.add(f);
		}
	}
	
	/**
	 * This method removes an {@link HalFilterInterface} from the list of 
	 * existing clients maintained by this object.
	 * @param f the client to be removed
	 * @throws AddRemoveElemException if the client is not in the list.
	 */
	public void removeClient(HalFilterInterface f){
		synchronized (clients) {
			if(!clients.contains(f))
				throw new AddRemoveElemException("Filter "+f.getName()
						+" not registered");
			clients.remove(f);
		}
	}
	
	/**
	 * This method is called to update the current client filter list with the 
	 * supplied one. New clients in the given list are added, and existing 
	 * clients not in the list are removed. 
	 */
	public void updateClientList(List<HalFilterInterface> list) {
		List<HalFilterInterface> tmp;

		synchronized(clients) {
			//creates a temp copy of clients
			tmp = new LinkedList<HalFilterInterface>(clients);
			
			//Create the new Halfilter and try to insert it in clients 
			for (HalFilterInterface c : list)
				try {
					addClient(c);
				} catch (AddRemoveElemException e) {
					//client already in the list, remove it from temp
					tmp.remove(c);				
				} 
				
			//in temp, we now have the clients that must be removed.
			if(tmp.size()>0)
				for(HalFilterInterface f: tmp)
					removeClient(f);	
		}
	}
	
	/**
	 * This method implements a loop which waits for HAL property list to be 
	 * available, and then filters them. DO NOT CALL THIS METHOD !!!
	 */
	@Override
	public void run() {
		Map<String,Variant<Object>> map = null;

		try {			
			while((map=properties.take())!=null && !Thread.interrupted())
				checkProperties(map, false);
		} catch (InterruptedException e) {} 
	}

	/**
	 * This method is called by DBus whenever a new HAL object is created. It 
	 * retrieves the object's properties and queues them for filtering by the 
	 * thread. DO NOT CALL THIS METHOD !!! 
	 * @param s the DeviceAdded signal object.
	 */
	@Override
	public void handle(DBusSignal s) {
		if(s instanceof DeviceAdded) {
			DeviceAdded d = (DeviceAdded) s;
			//System.out.println("New device added '"+d.udiAdded+"'");
			try {properties.add(getAllProperties(d.udiAdded));}
			catch (DBusException e) {
				System.out.println("Cant handle newly added device - unable" +
						" to list its properties");
			}
		} else if(s instanceof DeviceRemoved) {
			DeviceRemoved d = (DeviceRemoved) s;
			//System.out.println("Device removed '"+d.udiRemoved+"'");
			List<MatchingFilter> l = udiMatches.get(d.udiRemoved);
			if(l!=null) {
				for(MatchingFilter m: l)
					m.f.deviceRemoved(m.matches);
				udiMatches.remove(d.udiRemoved);
			}
		}
			
	}
	

	/*
	 * PRIVATE METHODS 
	 */

	/**
	 * this method is invoked only once, when the helper is started. It runs 
	 * the list of filters
	 * against the exiting devices.
	 */
	private void initialCheck() {
		//long start;
		try {
			String[] list = getAllDevices();
			//start = System.currentTimeMillis();
			for (String udi : list) 
				checkProperties(getAllProperties(udi), true);
			//System.out.println("took "+((float) 
			//	(System.currentTimeMillis() - start)/1000 ) + " sec overall");
		} catch (DBusException e) {
			System.out.println("Cannot carryout initial check: DBus error "
					+e.getMessage());
		}
	}
	
	
	/**
	 * This method matches each of the HAL match objects (from all filters) 
	 * against the HAl property list <code>map</code>given in argument.
	 * @param map the property list against which each of the HAL match object 
	 * must be tested.
	 */
	private void checkProperties(Map<String,Variant<Object>> map, boolean initial){
		Map<String,String> matches = new Hashtable<String,String>();
		Map<String, HalMatchInterface> matchList;
		HalMatchInterface m;
		String s;
		int maxMismatch, countMismatch;
		
		synchronized(clients) {
			for(HalFilterInterface c: clients){
				//for each HAL client, get the match list
				maxMismatch = c.countMatches() - c.getMinMatches();
				countMismatch = 0;
				//System.out.println("Checking client "+c.getName() 
				//	+ c.initialMatch() + c.subsequentMatch());

				matchList = c.getMatchList(); 

				for(String matchName: matchList.keySet()){
					//for each HALMatch object, check if there is a match
					m = matchList.get(matchName);
					try {
						s = match(map, m);
						//System.out.println("Match for "+matchName+ " - "
						//+m.getName() + " : "+s	);
						matches.put(matchName,s);
					} catch (MatchNotFoundException e) {
						//System.out.println("No Match for "+matchName+ " - "
						//+m.getName());
						if(++countMismatch>maxMismatch) break;
					}
				}

				//if we have enough matches, call deviceAdded()
				//System.out.println("We had "+matches.size()+
				//" matches - expected min: "+c.getMinMatches()+" - max: "
				//+c.getMaxMatches());
				if(c.getMinMatches()<=matches.size() && 
						matches.size()<=c.getMaxMatches()) {
					
					if(udiMatches.get(getUDI(map))==null)
						udiMatches.put(getUDI(map), new Vector<MatchingFilter>());
					
					udiMatches.get(getUDI(map)).add(
							new MatchingFilter(c, matches));

					if( (initial && c.initialMatch()) || 
							(!initial && c.subsequentMatch()) )
						c.deviceAdded(matches);
				}

				//move on to next client
				matches.clear();
			}
		}
	}
	
	/**
	 * This recursive method matches a <code>HalMatchInterface</code> object to 
	 * a list of HAL properties.
	 * @param props the list of HAL properties
	 * @param m the <code>HalMatchInterface</code> object
	 * @return the result of the match if successful
	 * @throws ConfigurationException if the match is unsuccessful
	 */
	private String match(Map<String,Variant<Object>> props, HalMatchInterface m) 
			throws MatchNotFoundException{
		String p;
		//if property is in current HAL object
		if(m.matchThisObject()){
			p = m.getPropName();

			//get property value
			if(props.containsKey(p))
				//check for match
				return m.match(props.get(p).getValue());

			//property not found
			throw new MatchNotFoundException();
		} else {
			//if property in another HAL object, get the object's properties 
			//and test them
			p = (m.matchNextObjectValue()) ? m.getNextObjectValue() : 
				props.get(m.getNextObjectLink())!=null ? 
						(String) props.get(m.getNextObjectLink()).getValue() : 
							null;

			if(p==null)
				throw new MatchNotFoundException();

			try {
				return match(getAllProperties(p), m.getNextMatch());
			} catch (DBusException e){
				throw new MatchNotFoundException(e);
			}
		}
	}
	
	/**
	 * This method fetches the properties of an HAL object given its UDI.
	 * @param udi the UDI of the object whose properties are needed.
	 * @return a map of the object's properties
	 * @throws ConfigurationException if there is an error getting the properties
	 */
	private Map<String,Variant<Object>> getAllProperties(String udi) 
			throws DBusException{
		Device d;
		try {
			d = (Device) conn.getRemoteObject("org.freedesktop.Hal", udi, 
					Device.class);
			return d.GetAllProperties();
		} catch (org.freedesktop.dbus.exceptions.DBusException e) {
			//System.out.println("Cant list properties for HAL object '"+udi+"'");
			throw new DBusException(e);
		} 
	}
	
	/**
	 * This method fetches the properties of an HAL object given its UDI.
	 * @param udi the UDI of the object whose properties are neede.
	 * @return a map of the object's properties
	 * @throws ConfigurationException if there is an error getting the properties
	 */
	private String[] getAllDevices() throws DBusException{
		Manager m;
		try {
			m =  conn.getRemoteObject("org.freedesktop.Hal", 
					"/org/freedesktop/Hal/Manager", Manager.class);
			return m.GetAllDevices();
		} catch (org.freedesktop.dbus.exceptions.DBusException e) {
			//System.out.println("Cant list existing HAL objects");
			throw new DBusException(e);
		} 
	}
	
	/**
	 * This method calls {@link Thread#join()} on the thread object and wait 
	 * till the thread exits, or an {@link InterruptedException} is caught, 
	 * and then returns.
	 */
	private void join(){
		try {
			t.join();
		} catch (InterruptedException e) {
			System.out.println("Interrupted while joining");
		}
	}
	
	private String getUDI(Map<String,Variant<Object>> m) {
        return (String) m.get("info.udi").getValue();
	}
	
	private static class MatchingFilter {
		public HalFilterInterface f;
		public Map<String,String> matches;
		public MatchingFilter(HalFilterInterface f, Map<String,String> m){
			this.f =f;
			matches = new Hashtable<String,String>(m);
		}
	}
}
