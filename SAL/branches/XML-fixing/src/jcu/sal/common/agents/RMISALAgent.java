package jcu.sal.common.agents;

import java.rmi.Remote;
import java.rmi.RemoteException;

import jcu.sal.common.Response;
import jcu.sal.common.RMICommandFactory.RMICommand;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SensorControlException;

public interface RMISALAgent extends Remote{
	/**
	 * This string is the name of the SAL agent stub as found in the RMI registry.
	 */
	public static String RMI_STUB_NAME = "RMI SAL Agent";
	
	/**
	 * This method registers a new SAL client. 
	 * @param rmiName A unique name associated with the RMI Client. The name is chosen by the caller and must be used in subsequent calls
	 * to execute() and {un}registerEventHandler().
	 * @param ipAddress the IP address of the RMI registry the client will use to register its objects. The RMI registry
	 * will be accessed by this Agent to invoke methods on the Client (StreamCallbacks and EventHandlers) 
	 * @throws ConfigurationException if this name already exists
	 * @throws RemoteException if the registry cant be reached
	 */
	public void registerClient(String rmiName, String ipAddress) throws ConfigurationException, RemoteException;
	
	/**
	 * This method unregisters a SAL client. 
	 * @param rmiName The unique name associated with the Client
	 * @throws ConfigurationException if this name already exists
	 */
	public void unregisterClient(String rmiName) throws ConfigurationException, RemoteException;
	
	/*
	 * Sensor-related methods
	 */
	
	/**
	 * This method instantiates a new sensor given its SML document. the returned value is a representation
	 * of the sensor identifier. If one is specified in the SML document, it will be ignored and replaced
	 * with a new one (the returned value).
	 * @param xml the sensor's SML configuration document
	 * @return a string representing the sensor identifier
	 * @throw SALDocumentException if the given SML document is malformed
	 * @throws ConfigurationException if the sensor cant be instantiated because of invalid configuration information
	 */
	public String addSensor(String xml) throws SALDocumentException, ConfigurationException, RemoteException;
	
	/**
	 * This method removes a sensor with the given identifier. Its configuration information is also removed from the
	 * configuration file.
	 * @param sid the sensor identifier
	 * @throws NotFoundException if the given sensor ID doesnt match any existing sensor
	 */
	public void removeSensor(String sid) throws NotFoundException, RemoteException;
	
	/**
	 * This method returns an XML document containing the configuration of all currently active sensors.
	 * An active sensor is one that has been connected at least once since startup. Note that an active
	 * sensor may not be currently connected (for instance if its protocol has been removed).
	 * @return the list of all active sensors as an XML doc
	 */
	public String listActiveSensors() throws RemoteException;
	
	/**
	 * This method returns an XML document containing the configuration of all known sensors.
	 * A known sensor is one that has its configuration stored in the sensor configuration file 
	 * Known sensors may or may not be currently connected, and may not have been connected at all since
	 * startup.
	 * @return the list of all known sensors as an XML doc
	 */
	public String listSensors() throws RemoteException;
	
	/**
	 * This method instructs a sensor identified by sid to execute the command c 
	 * @param c the command to be executed
	 * @param sid the target sensor identifier
	 * @return the result
	 * @throws NotFoundException if the given sensor id doesnt match any existing sensor
	 * @throws SensorControlException if there is an error controlling the sensor. If this exception is raised,
	 * the cause of this exception will be linked to it and can be retrieved using <code>getCause()</code>  
	 */
	public Response execute(RMICommand c, String sid) throws NotFoundException, SensorControlException, RemoteException;
	
	/**
	 * This method returns the CML document for a given sensor
	 * @param sid the sensor identifier
	 * @return the CML doc
	 * @throws NotFoundException if the given sensor ID doesnt match any existing sensor
	 */
	public String getCML(String sid) throws NotFoundException, RemoteException;
	
	/*
	 * Protocols-related methods 
	 */
	
	/**
	 * This method instantiates a new protocol given its PCML document. If successful, this method will also
	 * store the protocol's PCML configuration information in the platform configuration file
	 * @param xml the protocol's PCML configuration document
	 * @param loadSensors set to true if the sensor configuration file should be checked for sensors associated with 
	 * this protocol and create them.
	 * @throws SALDocumentException if the given PCML document is malformed
	 * @throws ConfigurationException if the protocol cant be instantiated because of invalid configuration information
	 */
	public void addProtocol(String xml, boolean loadSensors) throws ConfigurationException, SALDocumentException, RemoteException;
	
	/**
	 * This method removes a protocol given its ID. The protocol is first stopped so commands are no further 
	 * accepted. It then removes all associated sensors and their configuration if <code>removeSensors</code> is set to true. 
	 * @throws NotFoundException if the given protocol ID doesnt match any existing protocols
	 */
	public void removeProtocol(String pid, boolean removeSensors) throws NotFoundException, RemoteException;
	
	/**
	 * This method lists the configuration of all existing protocols
	 * @return a PCML document listing the protocols configuration
	 */
	public String listProtocols() throws RemoteException;
	
	/*
	 * Event-related methods 
	 */
	
	/**
	 * This method registers an RMI event handler. Whenever the producer <code>producerID</code> generates an event, the method
	 * <code>handle</code> will be called on the RMI EventHandler <code>ev</code> with a matching Event object as the sole argument.
	 * A Producers ID is a protocol name. Three special producers also exist: <code>SensorManager.PRODUCER_ID</code> which generates
	 * <code>SensorNodeEvent</code> events when sensors are created and deleted, <code>ProtocolManager.PRODUCER_ID</code> which
	 * generates <code>ProtocolListEvent</code> events when protocols are created and deleted, <code>SensorState.PRODUCER_ID</code>
	 * which generates <code>SensorStateEvent</code> events when a sensor is connected or disconnected.  
	 * @param rmiName the name of the RMI client as previously registered with registerClient().
	 * @param objName the name of the RMI event handler to lookup in the RMI registry.
	 * @param producerID the identifier of a protocol or the special identifiers "SensorManager", "ProtocolManager" or "SensorState"
	 * @throws NotFoundException if the given producerID doesnt exist
	 * @throws RemoteException if the RMI event handler object cant be found in the RMI registry
	 */
	public void registerEventHandler(String rmiName, String objName, String producerID) throws NotFoundException, RemoteException;
	
	/**
	 * This method unregisters an EventHandler previously registered with <code>registerEventHandler()</code>
	 * @param rmiName the name of the RMI client as previously registered with registerClient().
	 * @param objName the name of the object to lookup in the RMI registry.
	 * @param producerID the producer to which it is associated
	 * @throws NotFoundException if the handler can not be found/removed
	 * @throws RemoteException if the RMI event handler object cant be found in the RMI registry
	 */
	public void unregisterEventHandler(String rmiName, String objName, String producerID) throws NotFoundException, RemoteException;
}
