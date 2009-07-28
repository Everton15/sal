package jcu.sal.common.events;

import java.io.IOException;

import jcu.sal.common.agents.SALAgent;

/**
 * This interface is implemented by SAL clients. Objects implementing
 * this interfaces can be registered to receive {@link Event} objects
 * reporting events such as sensor addition, removal, and many others.  
 * @author gilles
 *
 */
public interface ClientEventHandler{
	public void handle(Event e, SALAgent a) throws IOException;
}
