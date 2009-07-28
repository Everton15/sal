package jcu.sal.common.agents.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import jcu.sal.common.events.Event;


public interface RMIEventHandler extends Remote {
	public void handle(Event e) throws RemoteException;
}
