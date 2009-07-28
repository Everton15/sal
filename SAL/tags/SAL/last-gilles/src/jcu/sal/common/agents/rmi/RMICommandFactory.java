package jcu.sal.common.agents.rmi;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import jcu.sal.common.CommandFactory;
import jcu.sal.common.CommandFactory.Command;

/**
 * This class is an adapter around a {@link CommandFactory} object, 
 * which adds code to handle RMIStreamCallback objects
 * (instead of StreamCallback).
 * @author gilles
 *
 */
public class RMICommandFactory {

	public static RMICommand getCommand(Command c, List<String> cb){
		return new RMICommand(c, cb);
	}

	/**
	 * Objects of this class represent a SAL command when using the RMI version.
	 * An RMICommandFactory is responsible for instantiating these objects.
	 * @author gilles
	 *
	 */
	public static class RMICommand implements Serializable{
		private static final long serialVersionUID = 6054676797304225967L;
		private List<String> callbacks;
		private Command c;

		private RMICommand(Command c, List<String> RMIcallback){
			//the following is done so that the existing StreamCallback object
			//in the Command c is not transmitted through RMI to the agent,
			//which would require the object to be Serializable (pain!!)
			this.c = CommandFactory.getCommand(c, null);
			callbacks = RMIcallback;
		}
		
		/**
		 * This method returns a list of string representing an RMI callback argument.
		 * The string at position 0 in the list is the rmiName (the name the Client used when calling RMISALAgent.registerClient()). 
		 * The string at position 1 in the list is the objName (the name of the object in the RMI registry representing the callback object).
		 * @return
		 */
		public List<String> getRMIStreamCallBack(){
			if(callbacks==null)
				return null;
			
			return new Vector<String>(callbacks);
		}

		
		public Command getCommand(){
			return c;
		}
	}
}
