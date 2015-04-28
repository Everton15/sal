This page details the SAL agent API, which is is user-visible portion of SAL.

# Introduction #
The API is fairly simple. The methods it offers are grouped in three categories:
  * sensor management (enumerate, add & remove sensors)
  * sensor control (discover capabilities, start & stop reading streams)
  * platform configuration
Each category is covered in detail in the following sections.


# Sensor management #
Methods in this category are used to manage the pool of sensor nodes. The [Sensor markup language](SML.md) is used to describe sensor configuration. An SML description document is an SML document containing configuration information for a single sensor. An SML descriptions document is an SML document containing configuration information for multiple sensor.

```
public String addSensor(String xml) throws SALDocumentException,  ConfigurationException;
```
This method is used to add a new sensor, assuming the protocol is uses is already instantiated and started. The sole argument is the SML description document describing the configuration of the sensor to be created. If successful, this method returns the unique sensor identifier (SID) for the new sensor.

```
public void removeSensor(String sid) throws NotFoundException;
```
This method removes a sensor given its SID.

```
public String listActiveSensors();
```
This method returns a string representation of an SML descriptions document containing the configuration of all currently active sensors. An active sensor is one that has been connected at least once since startup. An active sensor may not be currently connected (for instance if its protocol has been removed).

```
public String listSensors();
```
This method returns a string representation of the SML descriptions document containing the configuration of all known sensors. A known sensor is one that has its configuration stored in the sensor configuration file. Known sensors may or may not be currently connected, and may not have been connected at all since startup.

```
public String listSensor(String sid) throws NotFoundException;
```
This method returns a string representation of the SML description document containing the configuration for a sensor given its SID.


# Sensor control #
Methods in this category report on sensor capabilities and control data streaming.


```
public StreamID setupStream(Command c, String sid) throws NotFoundException, SensorControlException;
```
This method sets up a new data stream produced by sending the given command repeatedly to a sensor identified by its sensor ID. The command contains the sampling frequency (how often it should be executed), which can be either:
  * continuous, the sensor sends data continuously, until `terminateStream()` is called,
  * once, the sensor sends data only once and closes the stream.
  * predefined, the sensor sends data at specific intervals.
See ADD LINK HERE section on how to create Command objects.

The returned value is a stream identifier object which uniquely identifies this stream. However, the returned value is null if the command's sampling frequency is set to 'once'.
Note that if the sampling frequency is set to 'continuous' or 'predefined', the stream will only start when a call to `startStream()` is made. This is not required when the sampling frequency is set to 'once'.

```
public void startStream(StreamID streamId) throws NotFoundException;
```
This method starts a stream. It must be called only if the stream's sampling frequency is set to 'continuous' or 'once'.

```
public void terminateStream(StreamID streamId) throws NotFoundException;
```
This method closes a stream.

```
public String getCML(String sid) throws NotFoundException;
```
This method returns a string representation of a CML document, which describes commands supported by a given sensor.

# Platform configuration #
Methods in this category adjust the running configuration of the agent. They can be used to add or remove support for a new technology.
```
public void addProtocol(String xml, boolean loadSensors) throws ConfigurationException, SALDocumentException;
```
This method instantiates a new protocol given a string representation of its PCML description. The boolean specifies whether to instantiates sensors belonging to this protocol if found in the configuration file.

```
public void removeProtocol(String pid, boolean removeSensors) throws NotFoundException;
```
This method removes an existing protocol given it protocol identifier.

```
public String listProtocols();
```
This method returns a string representation of the PCML descriptions object which lists all currently existing protocols.

# Miscellaneous #
```
public String getID();
```
This method returns a unique identifier for this SAL agent.

```
public String getType();
```
This method returns the type of this agent (local or remote)

```
public void registerEventHandler(ClientEventHandler eh, String producerID) throws NotFoundException;
```
This method registers a event handler which will receive events originating from the producer identified by its producer ID.

```
public void unregisterEventHandler(ClientEventHandler eh, String producerID) throws NotFoundException;
```
This method unregisters a event handler.
