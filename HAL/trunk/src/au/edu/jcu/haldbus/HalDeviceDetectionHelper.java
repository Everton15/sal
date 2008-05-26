package au.edu.jcu.haldbus;


import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.naming.ConfigurationException;

import org.freedesktop.Hal.Device;
import org.freedesktop.Hal.Manager;
import org.freedesktop.Hal.Manager.DeviceAdded;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.Variant;

import au.edu.jcu.haldbus.exceptions.AddRemoveElemException;
import au.edu.jcu.haldbus.exceptions.DBusException;
import au.edu.jcu.haldbus.exceptions.MatchNotFoundException;
import au.edu.jcu.haldbus.match.HalMatchInterface;

/**
 * Objects of this class run a single thread waiting for HAL DeviceAdded signals. When such a signal is recevied, 
 * a list of <code>HalFilterInterface</code> objects is matched against the new device's properties. If enough 
 * matches are successful, <code>HalFilterInterface.doAction()</code> is called. The list of
 * <code>HalFilterInterface</code> objects can be changed at runtime using <code>addClient()</code> and 
 * <code>removeClient()</code>.
 * @author gilles
 *
 */
public class HalDeviceDetectionHelper implements Runnable, DBusSigHandler<Manager.DeviceAdded>{
	private Thread t;
	private DBusConnection conn = null;
	private BlockingQueue<Map<String,Variant<Object>>> properties;
	private List<HalFilterInterface> clients;
	
	/**
	 * Default constructor. It initialises the new object's members, registers with DBUS and starts the thread.
	 * @throws ConfigurationException if the DBus registration fails.
	 */
	public HalDeviceDetectionHelper() throws DBusException {
		try {
			conn = DBusConnection.getConnection(DBusConnection.SYSTEM);
			conn.addSigHandler(Manager.DeviceAdded.class, this);
		} catch (org.freedesktop.dbus.exceptions.DBusException e) {
			throw new DBusException(e);
		}
		t = new Thread(this);
		properties = new LinkedBlockingQueue<Map<String,Variant<Object>>>();
		clients = new LinkedList<HalFilterInterface>();
		t.start();
	}
	
	/**
	 * This method interrupts the thread, and waits for it to finish. The DBus resources are then freed.	 *
	 */
	public void stop(){
		t.interrupt();
		join();
		conn.disconnect();
	}
	
	/**
	 * This method adds a new <code>HalFilterInterface</code> object to the list. This object will be
	 * matched when the next new device is added.
	 * @param f the <code>HalFilterInterface</code> object to be added
	 * @throws AddRemoveElemException if the list already contains this <code>HalFilterInterface</code> object.
	 */
	public void addClient(HalFilterInterface f) throws AddRemoveElemException{
		synchronized (clients) {
			if(clients.contains(f))
				throw new AddRemoveElemException();
			clients.add(f);
		}
	}
	
	/**
	 * This method removes an existing <code>HalFilterInterface</code> object from the list.
	 * @param f the <code>HalFilterInterface</code> object to be removed
	 * @throws AddRemoveElemException if the list doesnt contains the <code>HalFilterInterface</code> object.
	 */
	public void removeClient(HalFilterInterface f) throws AddRemoveElemException{
		synchronized (clients) {
			if(!clients.contains(f))
				throw new AddRemoveElemException();
			clients.remove(f);
		}
	}

	/**
	 * This method implements a loop which waits for HAL property list to be available, and then filters them
	 */
	public void run() {
		Map<String,Variant<Object>> map = null;
		Map<String,String> matches = new Hashtable<String,String>();
		Map<String, HalMatchInterface> matchList;
		Iterator<HalFilterInterface> iter;
		Iterator<String> iter2;
		HalFilterInterface c;
		HalMatchInterface m;
		String s, matchName;
		try {			
			while((map=properties.take())!=null && !Thread.interrupted()){
				synchronized(clients) {
					iter = clients.iterator();
					while(iter.hasNext()){
						//for each HAL client, get the match list
						c = iter.next();
						matchList = c.getMatchList(); 
						iter2 = matchList.keySet().iterator();
							while(iter2.hasNext()){
								//for each HALMatch object, check if there is a match
								matchName = iter2.next();
								m = matchList.get(matchName);
								try {
									s = match(map, m);
									matches.put(matchName,s);
								} catch (MatchNotFoundException e) {System.out.println("No Match for "+matchName+ " - "+m.getName());}
							}
							
							//if we have enough matches, call doAction()
							System.out.println("We had "+matches.size()+" matches - expected min: "+c.getMinMatches()+" - max: "+c.getMaxMatches());
							if(c.getMinMatches()<=matches.size() && matches.size()<=c.getMaxMatches()){
								//System.out.println("We had "+matches.size()+" matches");
								iter2 = matches.keySet().iterator();
								while(iter2.hasNext()){
									s = iter2.next();
									System.out.println("Key: "+s+" - value: "+matches.get(s));
								}
								c.doAction(matches);
							}
						
						//move on to next client
						matches.clear();
					}
				}
			}
		} catch (InterruptedException e) {} 
	}
	
	/**
	 * This method fetches the properties of an HAL object given its UDI.
	 * @param udi the UDI of the object whose properties are neede.
	 * @return a map of the object's properties
	 * @throws ConfigurationException if there is an error getting the properties
	 */
	public Map<String,Variant<Object>> getAllProperties(String udi) throws DBusException{
		Device d;
		try {
			d = (Device) conn.getRemoteObject("org.freedesktop.Hal", udi, Device.class);
			return d.GetAllProperties();
		} catch (org.freedesktop.dbus.exceptions.DBusException e) {
			System.out.println("Cant list properties for HAL object '"+udi+"'");
			throw new DBusException(e);
		} 
	}

	/**
	 * This method is called by DBus whenever a new HAL object is created. It retrieves the object's properties and
	 * queue them for filtering by the thread. 
	 * @param d the DeviceAdded signal object.
	 */
	public void handle(DeviceAdded d) {
		System.out.println("New device added '"+d.udiAdded+"'");
		try {properties.add(getAllProperties(d.udiAdded));}
		catch (IllegalStateException e) {
			System.out.println("Cant add Property list for newly added device - Queue full");
		} catch (DBusException e) {
			System.out.println("Cant handle newly added device - no properties");
		}
	}
	
	/**
	 * This recursive method matches a <code>HalMatchInterface</code> object to a list of HAL properties.
	 * @param props the list of HAL properties
	 * @param m the <code>HalMatchInterface</code> object
	 * @return the result of the match if successful
	 * @throws ConfigurationException if the match is unsuccessful
	 */
	private String match(Map<String,Variant<Object>> props, HalMatchInterface m) throws MatchNotFoundException{
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
			//if property in another HAL object, get the object's properties and test them
			p = (m.matchNextObjectValue()) ? m.getNextObjectValue() : (String) props.get(m.getNextObjectLink()).getValue();

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
	 * This method calls <code>join()</code> on the thread object and wait till the thread exits.
	 */
	private void join(){
		try {
			t.join();
		} catch (InterruptedException e) {
			System.out.println("Interrupted while joining");
		}
	}

	public static void main(String[] args) throws IOException, DBusException{
		HalDeviceDetectionHelper h = new HalDeviceDetectionHelper();
		h.addClient(new V4LHalClient());
		System.in.read();
		h.stop();		
	}

}
