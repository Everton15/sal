package jcu.sal.events;

import java.io.IOException;

import jcu.sal.common.events.ClientEventHandler;
import jcu.sal.common.events.Event;

/**
 * This interface is implemented by by objects within the SAL agent, and SAL client stubs.
 * It must not be used by any SAL client (other than stubs). Instead, these should use the
 * {@link ClientEventHandler} interface instead. 
 * @author gilles
 *
 */
public interface EventHandler{
	/**
	 * This method is called by the {@link EventDispatcher} thread on registered
	 * event handler objects, ie objects which implements this interface
	 * @param e the {@link Event} to be dispatched
	 * @throws IOException if the event handler does not accept events anymore
	 */
	public void handle(Event e) throws IOException;
	public int hashCode();
	public boolean equals(Object obj);
}
