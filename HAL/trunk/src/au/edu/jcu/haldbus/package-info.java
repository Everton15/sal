/**
 * <h1>HAL java main Package</h1>
 * <h2>Introduction</h2>
 * Using this GPL java package, an application can intercept DeviceAdded and 
 * DeviceRemoved signals emitted by the HAL daemon when a device is connected or
 * disconnected.
 * <h2>Background</h2>
 * The Hardware Abstraction Layer (HAL) maintains a database of objects 
 * representing various bits and pieces of hardware present on the 
 * platform it runs on.  Each object has a Unique Device Identifier (UDI). For 
 * each object, HAL maintains a table of properties (key-value pairs). All 
 * objects share common properties (called general properties) and can also have
 * properties that are specific to its device class. See the 
 * <a href="http://people.freedesktop.org/~david/hal-spec/hal-spec.html">HAL specifications</a> 
 * for more detail).  
 * The HAL daemon is reachable through DBus and 
 * emits a DBus signal whenever a device is plugged in or removed. The signal
 * carries the UDI of the device that has been added/removed
 * Given a UDI, the HAL daemon can be queried to gather more information on the
 * associated device. 
 * <h2>Usage</h2>
 * This section explains how to intercept the HAL signals.
 * <h3>Clients (also called a filter)</h3>
 * A client is an object that implements the 
 * {@link au.edu.jcu.haldbus.filter.HalFilterInterface}. A client does not 
 * usually implement this interface itself. Instead, it derives the 
 * {@link au.edu.jcu.haldbus.filter.AbstractHalFilter} base class, which 
 * implements the inteface and encapsulates behaviour common to all clients.
 * 
 * A client provides:
 * <ul>
 * <li>a list of criteria an HAL object must match,</li>
 * <li>a <code>deviceAdded()</code> method that will be called when a 
 * DeviceAdded signal is received about an HAL object that matches the criteria,</li>
 * <li>a <code>deviceRemoved()</code> method that will be called when a 
 * DeviceRemoved signal is received about an HAL object that matches the criteria.</li>
 * </ul>
 * 
 * <h3>Criteria list</h3>
 * A criterion (also called a match object) is an object that implements the 
 * {@link au.edu.jcu.haldbus.match.HalMatchInterface}. Common behaviour for all
 * match objects is encapsulated in {@link au.edu.jcu.haldbus.match.AbstractMatch}
 * which implements {@link au.edu.jcu.haldbus.match.HalMatchInterface}. Existing
 * match classes can be found in the au.edu.jcu.haldbus.match package, and are
 * summarised below:
 * <br>
 * <table border="1">
 * <tr>
 * <th>Class name</th>
 * <th>Matches what ?</th>
 * </tr>
 * <tr>
 * <td>GenericMatch<T></td>
 * <td>matches only if the value of property is equals to a given object</td>
 * </tr>
 * <tr>
 * <td>IntMatch<T></td>
 * <td>matches only if the value of property is equals to a given integer</td>
 * </tr>
 * <tr>
 * <td>VectorMatch<T></td>
 * <td>matches only if a given value is found in the list of values of property
 * </td>
 * </tr>
 * <tr>
 * <td>AlwaysMatch<T></td>
 * <td>always matches a property, regardless of its value</td>
 * </tr>
 * <td>NextMatch<T></td>
 * <td>matches against the value of another property whose UDI can be found in
 * the current property</td>
 * </tr>
 * </table>
 * 
 * <h3>Hardware watcher</h3>
 * Clients typically register with a hardware watcher object. The hardware 
 * watcher intercepts the HAL signals. Then, for each client, it checks the 
 * criteria against the HAL object for which it received the signal. If there 
 * are enough matches, either the <code>deviceAdded()</code> or 
 * <code>deviceRemoved()</code> method is called.
 * 
 * <h3>Example</h3>
 * This example presented below is taken from the 
 * au.edu.jcu.haldbus.examples.V4LhalFilter class. It defines a client (by 
 * extending {@link au.edu.jcu.haldbus.filter.AbstractHalFilter}), and creates a
 * criteria list in its constructor.
 * <br>
 * <br><pre>
 * public class V4LHalFilter extends AbstractHalFilter {
 *   public V4LHalFilter(String n) throws AddRemoveElemException{
 *     super(n);
 *     
 *     //the calls to addMatch() below add new criteria 
 *     //and assigns a name to each criterion
 *     
 *     //look for a property called 'info.capabilities' which contains a list of strings.
 *     //if the list contains 'video4linux' then we have a match.
 *     addMatch("1-capability", new VectorMatch&lt;String&gt;("info.capabilities", "video4linux"));
 *     
 *     //look for a property called 'info.category'.
 *     //if it contains 'video4linux' we have a match.
 *     addMatch("2-category", new GenericMatch&lt;String&gt;("info.category", "video4linux"));
 *     
 *     //look for a property called 'info.capabilities' which contains a list of strings.
 *     //if it contains 'video4linux.video_capture' then we have a match.
 *     addMatch("3-category.capture", new VectorMatch&lt;String&gt;("info.capabilities", "video4linux.video_capture"));
 *     
 *     //look for a property called 'video4linux.device'.
 *     //if it contains 'video' we have a match.
 *     addMatch("4-video4linux.device", new GenericMatch&lt;String&gt;("video4linux.device", "video", true,true));
 *     
 *     //look for a property called 'linux.device_file' and always match its value
 *     addMatch("5-deviceFile", new AlwaysMatch("linux.device_file"));
 *     
 *     //look for a property called 'info.parent'. Its value contains the UDI of another object.
 *     //In that object, always match the value of the property 'info.product'
 *     addMatch("6-info.product", new NextMatch("@info.parent", new AlwaysMatch("info.product")));
 *     
 *     //look for a property called 'info.parent'. Its value contains the UDI of another object.
 *     //In that object, always match the value of the property 'info.vendor'
 *     addMatch("7-info.vendor", new NextMatch("@info.parent", new AlwaysMatch("info.vendor")));
 *   }
 *
 *   //this method is called when a device that matches the above criteria is connected
 *   //the argument l is a map of critrion names and their values.
 *   //for instance, if a Logitech webcam is plugged in and is assigned /dev/video0,
 *   //l.get("6-info.vendor") returns "046d" (the logitech PID) and 
 *   //l.get("5-deviceFile") returns "/dev/video0"
 *   public void deviceAdded(Map&lt;String,String&gt; l) {
 *     System.out.println(l.get("6-info.product")+" - "+l.get("7-info.vendor")+ " on "+l.get("5-deviceFile")+" Connected");
 *   }
 *
 *   //this method is called when a device that matches the above criteria is disconnected
 *   public void deviceRemoved(Map&lt;String, String&gt; l) {
 *     System.out.println(l.get("6-info.product")+" - "+l.get("7-info.vendor")+ " on "+l.get("5-deviceFile")+" Disconnected");
 *   }
 *
 *   public static void main(String[] args) throws DBusException{
 *     //creates an instance of a HardwareWatcher
 *     HardwareWatcher w = new HardwareWatcher();
 *     
 *     //Instantiate our client
 *     V4LHalFilter v1 = new V4LHalFilter("Filter1");
 *     
 *     //Register it with the watcher
 *     w.addClient(v1);
 *     
 *     //start the watcher
 *     w.start();
 *     
 *     //Wait until we press enter
 *     System.out.println("Try connect / disconnect a v4l device");
 *     try {
 *       System.in.read();
 *     } catch (IOException e) {
 *       e.printStackTrace();
 *     }
 *     
 *     //stop the watcher
 *     w.stop();
 *     
 *     //remove our client
 *     w.removeClient(v1);
 *   }
 *  }
 * </pre>
 */
package au.edu.jcu.haldbus;


