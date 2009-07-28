package jcu.sal.components.protocols;

import jcu.sal.common.exceptions.SALRunTimeException;


/**
 * Objects implementing this interface are streaming threads, used to send 
 * a command to a sensor regularly until stopped. The interval of time between
 * two command can be changed while the thread is running.
 * A streaming thread can only be started once. Subsequent calls {@link #start()} must 
 * throw a {@link SALRunTimeException}. It may be {@link #stop()}ped multiple times, 
 * although {@link StreamingThreadListener}s will be notified only once when the thread
 * exits: either when the thread exits on its own, or on the first call to {@link #stop()}.
 * 
 * @author gilles
 *
 */
public interface StreamingThread {
	/**
	 * This method starts the streaming thread and responses are streamed
	 * back to the client
	 * @throws SALRunTimeException if the stream has already been started once before.
	 */
	public void start();
	
	/**
	 * This method updates the streaming thread with a new interval
	 * @param i the interval of time between two successive commands
	 */
	public void update(int i);
	
	/**
	 * This method terminates the streaming thread, and returns when the thread
	 * has exited. Once the thread has exited, it must not be started again.
	 */
	public void stop();
	
	/**
	 * This method returns the local stream id of this thread 
	 * @return the local stream id of this thread
	 */
	public LocalStreamID getLocalStreamID();
}
