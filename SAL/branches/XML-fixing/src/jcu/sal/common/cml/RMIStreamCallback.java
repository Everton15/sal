package jcu.sal.common.cml;

import java.rmi.Remote;
import java.rmi.RemoteException;

import jcu.sal.common.Response;

public interface RMIStreamCallback extends Remote {
	public void collect(Response r) throws RemoteException;
}
