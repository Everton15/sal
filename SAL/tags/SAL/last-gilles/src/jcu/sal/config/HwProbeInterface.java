package jcu.sal.config;

/**
 * This interface is implemented by all classes used to probe hardware devices. This interface
 * specifies only three methods. All hardware probes are implemented as threads
 * waiting for specific events (new device connected (HAL), new file appeared somewhere in filesystem 
 * (inotify) ). Threads use a list of clients which specify what event they are interested in,
 * and what must be done when such an event is detected. 
 * This interface allows threads to be started and stopped. The {@link #listChanged()} method
 * notifies the thread that a new list of clients is available. 
 * @author gilles
 *
 */
public interface HwProbeInterface{

	/**
	 * This method starts the threads
	 * @throws Exception if the thread cant be started
	 */
	public void start() throws Exception;

	/**
	 * This method stops the threads and returned when all have exited 
	 */
	public void stop();
	
	/**
	 * This method is called whenever a new list of clients is available 
	 */
	public void listChanged();

}