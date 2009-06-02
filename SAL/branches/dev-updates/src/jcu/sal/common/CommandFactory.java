package jcu.sal.common;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLArgument;
import jcu.sal.common.cml.CMLDescription;
import jcu.sal.common.cml.StreamCallback;
import jcu.sal.common.exceptions.ArgumentNotFoundException;
import jcu.sal.common.exceptions.ConfigurationException;
import jcu.sal.common.exceptions.InvalidValueException;

import org.apache.log4j.Logger;

/**
 * CommandFactory objects are used to create correct {@link Command} objects which can then be given to SAL for execution.
 * To construct a {@link Command} object, you first create a {@link CommandFactory} object and pass the {@link CMLDescription} 
 * for the {@link Command} you want to create, and a {@link StreamCallback} object where results of the command will be delivered.
 * All command arguments have a default value If you wish to change the default value,  
 * you can iterate over a list of names of arguments (the list is returned by 
 * {@link #listArgNames()}).
 * For each argument name, you can retrieve its {@link CMLArgument} object which provides
 * information on the type of the value, the default value, ...
 * You can then supply a new value using one of the 
 * <code>addArgumentValue{Int,String,Float}()</code> methods. You can also use {@link #addArgumentValue(String, String)} and let
 * the {@link CommandFactory} do the type conversion for you. If you do, make sure you handle any returned exception.<br>
 * You can get an instance of a {@link Command} object by invoking {@link #getCommand()}.
 * <b>Note that you must supply a {@link StreamCallback} object (either through {@link #CommandFactory(CMLDescription, StreamCallback)} or
 * {@link #addCallBack(StreamCallback)}). Otherwise, {@link #getCommand()} will throw an exception.
 * 
 * @author gilles
 *
 */
public class CommandFactory {
	private static Logger logger = Logger.getLogger(CommandFactory.class);
	static {
		Slog.setupLogger(logger);
	}
	
	public static final int CONTINUOUS_STREAM=0;
	public static final int ONLY_ONCE=-1;

	private Map<String, String> argValues;
	private CMLDescription cml;
	private Map<String,CMLArgument> args;
	private StreamCallback callback;
	private int interval;
	
	/**
	 * This constructor creates a empty command template based on the CML description object.
	 * A {@link StreamCallback} where command replies will be delivered must be set using
	 * {@link #addCallBack(StreamCallback)}. The command is run only once.
	 * @param desc the {@link CMLDescription} for which a {@link Command} must be created
	 * @param c the callback method that will be invoked to collect the results of the command
	 */
	public CommandFactory(CMLDescription desc){
		this(desc,null);
	}
		
	/**
	 * This constructor creates a empty command template based on the CML description object
	 * and the given {@link StreamCallback} where command replies will be delivered.
	 * The command is run only once. This can be changed by calling 
	 * @param desc the {@link CMLDescription} for which a {@link Command} must be created
	 * @param c the callback method that will be invoked to collect the results of the command
	 */
	public CommandFactory(CMLDescription desc, StreamCallback c){
//		logger.debug("Creating command for CML:\n"+desc.getXMLString());
		argValues = new Hashtable<String, String>();
		args = new Hashtable<String,CMLArgument>();
		callback = c;
		cml = desc;
		interval = ONLY_ONCE;
		for(CMLArgument a: desc.getArguments())
			args.put(a.getName(), a);

	}
	
	/**
	 * This method returns the {@link CMLArgument} for a given argument
	 * @param name the name of the argument
	 * @return the {@link CMLArgument} for an argument, which contains meta-data
	 * about this argument
	 * @throws ArgumentNotFoundException if the argument cannot be found
	 */
	public CMLArgument getArg(String name) {
		return args.get(name);
	}
	
	/**
	 * This method returns an Enumeration<String> of argument names.   
	 * @return a List<String> of the argument names associated with the command
	 */
	public List<String> listArgNames() {
		return new Vector<String>(args.keySet());
	}
	
	/**
	 * This method adds the value for a float argument and overwrites any previous values.
	 * @param val the value
	 * @param name the name of the argument for which the value is to be added
	 * @throws ArgumentNotFoundException if no argument matches the given name
	 * @throws InvalidValueException if the given value is not within the bounds,
	 * or is not a multiple of the step.
	 */
	public void addArgumentValueFloat(String name, float val) {
		CMLArgument arg = args.get(name);
		if(arg==null)
			throw new ArgumentNotFoundException("No such argument "+name);
	
		if(!arg.getType().equals(ArgumentType.FloatArgument)) {
			logger.error("Type of argument '"+name+"' is '"+args.get(name).getType()+"' not 'float'");
			throw new ArgumentNotFoundException("Type of argument '"+name+"' is '"+args.get(name).getType()+"' not 'float'");
		}
		
		if(arg.hasBounds())
			checkValue(val, arg.getMinFloat(), 
					arg.getMaxFloat(), arg.getStepFloat());

		addValue(name, String.valueOf(val));
	}
	
	/**
	 * This method adds the value for an integer argument and overwrites any previous values.
	 * @param val the value
	 * @param name the name of the argument for which the value is to be added
	 * @throws ArgumentNotFoundException if no argument matches the given name
	 * @throws InvalidValueException if the given value is not within the bounds,
	 * or is not a multiple of the step.
	 */
	public void addArgumentValueInt(String name, int val) {
		CMLArgument arg = args.get(name);
		if(arg==null)
			throw new ArgumentNotFoundException("No such argument "+name);
	
		if(!arg.getType().equals(ArgumentType.IntegerArgument)) {
			logger.error("Type of argument '"+name+"' is '"+args.get(name).getType().getType()+"' not 'int'");
			throw new ArgumentNotFoundException("Type of argument '"+name+"' is '"+args.get(name).getType().getType()+"' not 'int'");
		}
		
		if(arg.hasBounds())
			checkValue(val, args.get(name).getMinInt(), 
					args.get(name).getMaxInt(), args.get(name).getStepInt());
		
		addValue(name, String.valueOf(val));
	}
	
	/**
	 * This method adds the value for an string argument and overwrites any previous values.
	 * @param val the value
	 * @param name the name of the argument for which the value is to be added
	 * @throws ArgumentNotFoundException if no argument matches the given name or
	 * if the argument <code>name</code> is not of type string
	 */
	public void addArgumentValueString(String name, String val) {
		CMLArgument arg = args.get(name);
		if(arg==null)
			throw new ArgumentNotFoundException("No such argument "+name);
	
		if(!arg.getType().equals(ArgumentType.StringArgument)) {
			logger.error("Type of argument '"+name+"' is '"+args.get(name).getType().getType()+"' not 'string'");
			throw new ArgumentNotFoundException("Type of argument '"+name+"' is '"+args.get(name).getType().getType()+"' not 'string'");
		}
		addValue(name, val);
	}
	
	/**
	 * This method adds the value for a list argument and overwrites any previous value.
	 * @param val the value
	 * @param name the name of the argument for which the value is to be added
	 * @throws ArgumentNotFoundException if no argument matches the given name or
	 * if the argument <code>name</code> is not of type list
	 */
	public void addArgumentValueList(String name, String val) {
		CMLArgument arg = args.get(name);
		if(arg==null)
			throw new ArgumentNotFoundException("No such argument "+name);
	
		if(!arg.getType().equals(ArgumentType.ListArgument)) {
			logger.error("Type of argument '"+name+"' is '"+args.get(name).getType().getType()+"' not 'list'");
			throw new ArgumentNotFoundException("Type of argument '"+name+"' is '"+args.get(name).getType().getType()+"' not 'list'");
		}
		if(!arg.getList().containsKey(val)){
			logger.error("Incorrect value for argument '"+name+"': not in the list");
			throw new ArgumentNotFoundException("Incorrect value for argument '"+name+"': not in the list");
		}
		addValue(name, val);
	}
	
	/**
	 * This method adds the value for an argument and overwrites any previous values.
	 * The value is passed as a string and converted to the given type for checking
	 * before being added.
	 * @param val the value
	 * @param name the name of the argument for which the value is to be added
	 * @throws ArgumentNotFoundException if no argument matches the given name or
	 * if the value cannot be converted, the argument <code>name</code> cannot be found.
	 */
	public void addArgumentValue(String name, String val){
		CMLArgument arg = args.get(name);
		if(arg==null)
			throw new ArgumentNotFoundException("No such argument "+name);
		
		ArgumentType t = arg.getType();
		if(t.equals(ArgumentType.FloatArgument)){
			try {
				addArgumentValueFloat(name, Float.parseFloat(val));
			} catch (NumberFormatException nfe){
				logger.error("Cant convert '"+val+"' to float");
				throw new ArgumentNotFoundException("Cant convert '"+val+"' to float");
			}
		} else if(t.equals(ArgumentType.IntegerArgument)){
			try {
			addArgumentValueInt(name, Integer.parseInt(val));
			} catch (NumberFormatException nfe){
				logger.error("Cant convert '"+val+"' to integer");
				throw new ArgumentNotFoundException("Cant convert '"+val+"' to integer");
			}
		} else if(t.equals(ArgumentType.StringArgument))
			addArgumentValueString(name, val);
		else if(t.equals(ArgumentType.ListArgument))
			addArgumentValueList(name, val);
		else {
			logger.error("Unknown argument type '"+t.getType()+"'");
			throw new ArgumentNotFoundException("Unknown argument type '"+t.getType()+"'");
		}
	}
	
	/**
	 * This method sets the sampling frequency of this command, ie
	 * how often it will be run.
	 * @param i how often (in milliseconds) the command must be run. Use 
	 * {@link #CONTINUOUS_STREAM} to create a continuous stream (if supported)
	 * or {@link #ONLY_ONCE} to run the command only once (always supported).
	 * @throws ArgumentNotFoundException if this command can only be run once,
	 * or if the given value is outside the allowed range.
	 */
	public void setInterval(int i){
		if(!cml.isStreamable() && i!=ONLY_ONCE)
			throw new ArgumentNotFoundException("This command can only be run once");
		
		if(i==CONTINUOUS_STREAM && !cml.getSamplingBounds().isContinuous())
			throw new ArgumentNotFoundException("This command cannot be run continuously");
		
		if(i!=ONLY_ONCE && i!= CONTINUOUS_STREAM && i<cml.getSamplingBounds().getMin())
			throw new ArgumentNotFoundException("The value is below the minimum ("+cml.getSamplingBounds().getMin()+")");
		
		if(i!=ONLY_ONCE && i!= CONTINUOUS_STREAM && i>cml.getSamplingBounds().getMax())
			throw new ArgumentNotFoundException("The value is above the maximum ("+cml.getSamplingBounds().getMax()+")");
		
		interval = i;
	}
	
	/**
	 * This method adds the value for an string argument and overwrites any previous values.
	 * @param c the callback object
	 */
	public void addCallBack(StreamCallback c) {
		callback = c;
	}
	
	/**
	 * This method creates a new {@link Command} object, provided 
	 * there is a {@link StreamCallback} object.
	 * @throws ConfigurationException if a {@link StreamCallback} has not been provided.
	 * @return a new {@link Command} object
	 */
	public Command getCommand() throws ConfigurationException{
		//make sure the callback is there
		if(callback==null)
			throw new ConfigurationException("Callback object missing");
				
		//Make sure we have all the args and their values
		for(String name: args.keySet())
			if(argValues.get(name)==null)
				argValues.put(name, args.get(name).getDefaultValue());				
		
		return new Command(cml.getCID().intValue(), interval, argValues, callback);
	}
	
	/**
	 * this methods adds a value to an argument
	 * @param name the argument
	 * @param value the value
	 */
	private void addValue(String name, String val){
		argValues.put(name, val);
	}
	
	/**
	 * This method checks that a given value is within a minimum and maximum,
	 * and that it is multiple of the step value.
	 * @param v the value to be checked
	 * @param min the minimum
	 * @param max the maximum
	 * @param step the step value
	 */
	private void checkValue(float v, float min, float max, float step){
		if(v < min){
			logger.error("Value '"+v+"' is lower than minimum '"+min+"'");
			throw new InvalidValueException("Value '"+v+"' is lower than minimum '"+min+"'");
		}
		
		if(v > max){
			logger.error("Value '"+v+"' is greater than maximum '"+max+"'");
			throw new InvalidValueException("Value '"+v+"' is greater than maximum '"+max+"'");
		}
		
		if(step!=0 && (v-min)%step!=0){
			logger.error("Value '"+v+"' is not a multiple of the step '"+step+"'");
			throw new InvalidValueException("Value '"+v+"' is not a multiple of the step '"+step+"'");
		}
	}
	
	/**
	 * This method returns the command id
	 * @return the command id
	 */
	public int getCID(){
		return cml.getCID().intValue();
	}
	
	/**
	 * This method is meant to be used internally only.
	 * This method updates the current {@link StreamCallback} object associated
	 * with the given {@link Command} with the given one.
	 * @param c the {@link Command} to be updates
	 * @param cb the {@link StreamCallback} to be associated with the command
	 * @return the updated {@link Command}
	 */
	public static Command getCommand(Command c, StreamCallback cb) {
		return new Command(c, cb);
	}
	
	/**
	 * This class encapsulates data related to a SAL command, mainly
	 * a {@link StreamCallback} object which will receive the command's
	 * result, and a list of arguments & their values.
	 * Instantiating command is done using a {@link CommandFactory} object
	 * which will make sure the command is correct.
	 * 
	 * @author gilles
	 *
	 */
	public static class Command implements Serializable{
		private static final long serialVersionUID = -8361578743898576812L;
		private int cid;
		private int interval;
		private Map<String,String> parameters;
		private StreamCallback streamc;

		private Command(int cid, int i, Map<String, String> values, StreamCallback c){
			this.cid = cid;
			interval = i;
			parameters = values;
			streamc = c;		
		}
		
		private Command(Command cmd, StreamCallback c){
			this(cmd.cid, cmd.interval, cmd.parameters, c);
		}

		/**
		 * this method returns the {@link StreamCallback} associated with this command
		 * @return the {@link StreamCallback} associated with this command
		 */
		public StreamCallback getStreamCallBack() {
			return streamc;
		}
		
		/**
		 * This method returns the arguments list and values
		 * @return returns the arguments list and values
		 */
		public Map<String,String> getParameters(){
			return new Hashtable<String,String>(parameters);
		}
		
		/**
		 * This method returns the CId of this command
		 * @return
		 */
		public int getCID(){
			return cid; 
		}
		
		/**
		 * This method returns the interval of this command
		 * @return
		 */
		public int getInterval(){
			return interval; 
		}

		/**
		 * This method returns the value of argument <code>name</code>
		 * @param name the name of the argument for which the value is to be returned
		 * @return the value of argument <code>name</code>
		 */
		public String getValue(String name){
			return parameters.get(name);
		}
		
		/**
		 * This method returns the int value of an argument given its name.
		 * @param name the name of the argument
		 * @return the int value of the argument 
		 * @throws NumberFormatException if the value of the argument cannot be parsed to int.
		 */
		public int getIntValue(String name){
			return Integer.valueOf(parameters.get(name));
		}
		
		/**
		 * This method returns the float value of an argument given its name.
		 * @param name the name of the argument
		 * @return the float value of the argument 
		 * @throws NumberFormatException if the value of the argument cannot be parsed to float.
		 */
		public float getFloatValue(String name){
			return Float.valueOf(parameters.get(name));
		}
	}
}
