package jcu.sal.client.gui;

import java.rmi.RemoteException;

import jcu.sal.common.StreamID;
import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.agents.SALAgent;
import jcu.sal.common.agents.SALAgentFactory;
import jcu.sal.common.events.ClientEventHandler;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.SensorControlException;

public class ClientControllerImpl implements ClientController {

	@Override
	public void addProtocol(SALAgent a, String xml, boolean loadSensors)
			throws ConfigurationException, SALDocumentException {
		a.addProtocol(xml, loadSensors);

	}

	@Override
	public String addSensor(SALAgent a,String xml) throws SALDocumentException,
			ConfigurationException {
		return a.addSensor(xml);
	}

	@Override
	public SALAgent rmiConnect(String rmiName, String ipAddress, String ourIpAddress) throws ConfigurationException, RemoteException {
		return SALAgentFactory.getFactory().createRMIAgent(ipAddress, ourIpAddress, rmiName);
	}
	
	@Override
	public void rmiDisonnect(SALAgent a) {
		SALAgentFactory.getFactory().releaseRMIAgent(a);
	}
	
	@Override
	public void disconnect(SALAgent a) {
		SALAgentFactory.getFactory().releaseRMIAgent(a);
		a = null;
		System.gc();
	}

	@Override
	public StreamID setupStream(SALAgent a, Command c, String sid) throws NotFoundException,
			SensorControlException {
		return a.setupStream(c, sid);
	}
	
	@Override
	public void startStream(SALAgent a,StreamID s) throws NotFoundException {
		a.startStream(s);
	}
	
	@Override
	public void terminateStream(SALAgent a,StreamID s) throws NotFoundException {
		a.terminateStream(s);
	}

	@Override
	public String getCML(SALAgent a, String sid) throws NotFoundException {
		return a.getCML(sid);
	}

	@Override
	public String listActiveSensors(SALAgent a) {
		return a.listActiveSensors();
	}

	@Override
	public String listProtocols(SALAgent a) {
		return a.listProtocols();
	}

	@Override
	public String listSensors(SALAgent a) {
		return a.listSensors();
	}

	@Override
	public void removeProtocol(SALAgent a, String pid, boolean removeSensors)
			throws NotFoundException {
		a.removeProtocol(pid, removeSensors);

	}

	@Override
	public void removeSensor(SALAgent a, String sid) throws NotFoundException {
		a.removeSensor(sid);

	}

	@Override
	public void registerEventHandler(SALAgent a,ClientEventHandler ev, String producerID) 
		throws NotFoundException{
		a.registerEventHandler(ev, producerID);
		
	}

	@Override
	public void unregisterEventHandler(SALAgent a,ClientEventHandler ev,
			String producerID) throws NotFoundException {
		a.unregisterEventHandler(ev, producerID);		
	}

	@Override
	public String listSensor(SALAgent a, String sid) throws NotFoundException{
		return a.listSensor(sid);
	}

}
