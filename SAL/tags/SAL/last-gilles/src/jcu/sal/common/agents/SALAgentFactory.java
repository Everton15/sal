package jcu.sal.common.agents;

import java.rmi.RemoteException;

import jcu.sal.common.agents.rmi.RMIClientStub;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.SALRunTimeException;

/**
 * This class of object is responsible for instantiating various types
 * of client stubs connecting to SAL agents in different ways. 
 * Stubs are created with this factory using one of the <code>create*</code> methods
 * and <b>must<b> be released when done using either the <code>release</code>
 * method matching the <code>create</code> method used to instantiate the agent, or
 * using the generic {@link #releaseAgent(SALAgent)} method which will invoke the right
 * <code>release</code> method.  
 * @author gilles
 *
 */
public class SALAgentFactory {
	
	private static SALAgentFactory factory = new SALAgentFactory();
	
	private SALAgentFactory(){}
	
	public static SALAgentFactory getFactory(){
		return factory;
	}
	
//	/**
//	 * This method creates a new client stub connecting to a local SAL agent,
//	 * and returns a reference to it.
//	 * @param pc the file to be used as platform configuration file
//	 * @param sc the file to be used as sensor configuration file
//	 * @return a reference to a fully initialised local SAL agent
//	 * @throws ConfigurationException if the given files cannot be written to, parsed,
//	 * or if their content is not valid.
//	 */
//	public SALAgent createLocalAgent(String pc, String sc) throws ConfigurationException{
//		LocalAgentImpl s = new LocalAgentImpl();
//		s.start(pc, sc);
//		return s;
//	}
//	
//	/**
//	 * This method releases a reference to a local SAL agent previously instantiated 
//	 * with {@link #createLocalAgent(String, String)}.
//	 * @param s the local SAL agent to be released
//	 * @throws ClassCastException if the given SAL agent is not a local SAL agent.
//	 */
//	public void releaseLocalAgent(SALAgent s){
//		LocalAgentImpl agent = (LocalAgentImpl) s;
//		agent.stop();
//	}
	
	/**
	 * This method creates a new client stub connecting to remote SAL agent through RMI 
	 * and returns a reference to it.
	 * @param agentIP the IP address of the RMI registry where the SAL agent is registered
	 * @param ourIP the IP address of the local RMI registry where the client stub will be registered
	 * @return a reference to a fully initialised RMI SAL agent
	 * @throws ConfigurationException if the given RMI name cannot be registered with the agent
	 * @throws RemoteException if an RMI error occurs. 
	 */
	public SALAgent createRMIAgent(String agentIP, String ourIP, String rmiName)
		throws RemoteException, ConfigurationException{
		return new RMIClientStub(agentIP, ourIP, rmiName);
	}
	
	/**
	 * This method releases a reference to an RMI SAL agent previously 
	 * instantiated with {@link #createRMIAgent(String, String, String)}
	 * @param s the RMI SALagent to be released
	 * @throws ClassCastException if the given SAL agent is not an RMI SAL agent.
	 */
	public void releaseRMIAgent(SALAgent s){
		RMIClientStub agent = (RMIClientStub) s;
		agent.release();
	}

	
	/**
	 * This method releases a reference to a SAL agent previously 
	 * instantiated with any of the <code>create*</code> methods in this factory.
	 * The type of the given SAL agent is automatically detected, and the 
	 * correct <code>release*</code> method is invoked.
	 * @param s the SAL agent to be released
	 * @throws SALRunTimeException if the given object is not a SAL agent.
	 */
	public void releaseAgent(SALAgent s){
		if(s instanceof RMIClientStub)
			releaseRMIAgent(s);
		else 
//		if (s instanceof LocalAgentImpl)
//			releaseLocalAgent(s);
//		else
			throw new SALRunTimeException("The given object is not a SAL agent");
	}
}
