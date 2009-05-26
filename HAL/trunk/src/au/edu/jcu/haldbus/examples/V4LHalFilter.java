package au.edu.jcu.haldbus.examples;


import java.io.IOException;
import java.util.Map;

import au.edu.jcu.haldbus.HardwareWatcher;
import au.edu.jcu.haldbus.exceptions.AddRemoveElemException;
import au.edu.jcu.haldbus.exceptions.DBusException;
import au.edu.jcu.haldbus.filter.AbstractHalFilter;
import au.edu.jcu.haldbus.match.AlwaysMatch;
import au.edu.jcu.haldbus.match.GenericMatch;
import au.edu.jcu.haldbus.match.NextMatch;
import au.edu.jcu.haldbus.match.VectorMatch;

/**
 * This class is a good example of how to use the HAL java package.
 * It defines a client filter by extending {@link AbstractHalFilter}. It
 * creates a list of criteria matching V4L devices. When a V4L device is 
 * connected/disconnected, it simply prints a line on the terminal.
 * @author gilles
 *
 */
public class V4LHalFilter extends AbstractHalFilter {
	public V4LHalFilter(String n) throws AddRemoveElemException{
		super(n);
		
		//the calls to addMatch() below add new criteria 
		//and assigns a name to each criterion   
		
		//look for a property called 'info.capabilities' which contains a list of strings.
		//if the list contains 'video4linux' then we have a match.
		addMatch("1-capability", new VectorMatch<String>("info.capabilities", "video4linux"));
		
		//look for a property called 'info.category'.
		//if it contains 'video4linux' we have a match.
		addMatch("2-category", new GenericMatch<String>("info.category", "video4linux"));
		
		//look for a property called 'info.capabilities' which contains a list of strings.
		//if it contains 'video4linux.video_capture' then we have a match.
		addMatch("3-category.capture", new VectorMatch<String>("info.capabilities", "video4linux.video_capture"));
		
		//look for a property called 'video4linux.device'.
		//if it contains 'video' we have a match.
		addMatch("4-video4linux.device", new GenericMatch<String>("video4linux.device", "video", true,true));
		
		//look for a property called 'linux.device_file' and always match its value
		addMatch("5-deviceFile", new AlwaysMatch("linux.device_file"));
		
		//look for a property called 'info.parent'. Its value contains the UDI of another object.
		//In that object, always match the value of the property 'info.product'
		addMatch("6-info.product", new NextMatch("@info.parent", new AlwaysMatch("info.product")));
		
		//look for a property called 'info.parent'. Its value contains the UDI of another object.
		//In that object, always match the value of the property 'info.vendor'
		addMatch("7-info.vendor", new NextMatch("@info.parent", new AlwaysMatch("info.vendor")));
	}

	/**
	 * this method is called when a device that matches the above criteria is connected.
	 * the argument l is a map of critrion names and their values. The names are
	 * the ones used when creating the list in the constructor.
	 * For instance, if a Logitech webcam is plugged in and is assigned /dev/video0,
	 * l.get("6-info.vendor") returns "046d" (the logitech PID) and
	 * l.get("5-deviceFile") returns "/dev/video0"
	 */
	@Override
	public void deviceAdded(Map<String,String> l) {
		System.out.println(l.get("6-info.product")+" - "+l.get("7-info.vendor")+ " on "+l.get("5-deviceFile")+" Connected");
	}

	/**
	 * this method is called when a device that matches the above criteria is disconnected
	 */
	@Override
	public void deviceRemoved(Map<String, String> l) {
		System.out.println(l.get("6-info.product")+" - "+l.get("7-info.vendor")+ " on "+l.get("5-deviceFile")+" Disconnected");
	}
	
	public static void main(String[] args) throws DBusException{
		//creates an instance of a HardwareWatcher
		HardwareWatcher w = new HardwareWatcher();
		
		//Instantiate our client
		V4LHalFilter v1 = new V4LHalFilter("Filter1");
		
		//Register it with the watcher
		w.addClient(v1);
		
		//start the watcher
		w.start();
		
		//Wait until we press enter
		System.out.println("Try connect / disconnect a v4l device");
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//stop the watcher
		w.stop();
		
		//remove our client
		w.removeClient(v1);
	}
}
