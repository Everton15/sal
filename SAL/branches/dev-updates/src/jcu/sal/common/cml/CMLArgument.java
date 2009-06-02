package jcu.sal.common.cml;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import jcu.sal.common.cml.xml.Argument;
import jcu.sal.common.cml.xml.ObjectFactory;
import jcu.sal.common.cml.xml.Argument.Bounds;
import jcu.sal.common.cml.xml.Argument.List.Item;
import jcu.sal.common.exceptions.SALRunTimeException;

/**
 * This class encapsulates information about a single argument to a SAL command.
 * 
 * @author gilles
 *
 */
public class CMLArgument {
	
	private Argument argument;
	private static final ObjectFactory factory = new ObjectFactory();

	private CMLArgument(String name){
		argument = factory.createArgument();
		if(name==null)
			throw new SALRunTimeException("CML arguments must be given a name");
		argument.setName(name);
	}
	
	/**
	 * This package-private constructor is meant to be used only
	 * by {@link CMLDescription} objects
	 * @param a
	 */
	CMLArgument(Argument a) {
		argument = a;
	}
	
	/**
	 * This method build a CMLArgument of type {@link CMLConstants#ARG_TYPE_LIST}.
	 * The first element in items is used as the default value.
	 * @param type the type of this argument (see {@link CMLConstants}).
	 * @param items a map of possible values and their textual names
	 * @param optional if the argument is optional
	 * @param def the default value. If null, 
	 * @throws SALRunTimeException if name or items is null
	 */
	public CMLArgument(String name, Map<String,String> items){
		this(name, items, null);
	}
	
	/**
	 * This method build a CMLArgument of type {@link CMLConstants#ARG_TYPE_LIST}.
	 * @param type the type of this argument (see {@link CMLConstants}).
	 * @param items a map of possible values and their textual names
	 * @param optional if the argument is optional
	 * @param def the default value. If null, the first element in items is used
	 * @throws SALRunTimeException if name or items is null
	 */
	public CMLArgument(String name, Map<String,String> items, String def){
		this(name);
		argument.setType(CMLConstants.ARG_TYPE_LIST);
		addList(items);
		if(def==null)
			def = (String) (items.keySet().toArray()[0]);
		argument.setDefaultValue(def);
	}
	
	/**
	 * This method build a CMLArgument of a given type which must be either
	 * {@link CMLConstants#ARG_TYPE_FLOAT},
	 * {@link CMLConstants#ARG_TYPE_INT} or {@link CMLConstants#ARG_TYPE_STRING}.
	 * Any other value will throw a {@link SALRunTimeException}.    
	 * @param name the name of this argument.
	 * @param type the type of this argument (see {@link CMLConstants}).
	 * @param def the default value. if null, 0 is used as default for FLOAT and INT,
	 * and "" for STRING
	 * @throws SALRunTimeException if name or type is null or invalid.
	 */
	public CMLArgument(String name, ArgumentType type,String def){
		this(name);
		if(type==null)
			throw new SALRunTimeException("No type provided");
		
		if(!type.equals(ArgumentType.FloatArgument)	&& 
				!type.equals(ArgumentType.StringArgument) && 
				!type.equals(ArgumentType.IntegerArgument))
			throw new SALRunTimeException("The given type ("+type+") is invalid");
		
		if(def!=null){
			if(type.equals(ArgumentType.FloatArgument))
				try{Float.parseFloat(def);}
				catch(NumberFormatException e){ 
					throw new SALRunTimeException("The default value is not of type float"); 
				}
			else if(type.equals(ArgumentType.IntegerArgument))
				try{Integer.parseInt(def);}
			catch(NumberFormatException e){ 
				throw new SALRunTimeException("The default value is not of type int"); 
			} 	
		} else {
			if(type.equals(ArgumentType.FloatArgument) || 
					type.equals(ArgumentType.IntegerArgument))
					def = "0";
			else 
				def = "";
		}
		argument.setDefaultValue(def);
		argument.setType(type.getType());
		
	}
	
	/**
	 * This method build a bounded argument of type {@link CMLConstants#ARG_TYPE_FLOAT}.
	 * The default value is set to the mid-point between min and max 
	 * (equal to min+(max-min)/2 ).
	 * @param name the name of this argument
	 * @param min the min value
	 * @param max the max value
	 * @param step the step value, can be equal to 0, which means any value between min and max are valid
	 * @throws SALRunTimeException if the minimum is greater than the maximum,
	 * if the step value is negative or if name is null
	 */
	public CMLArgument(String name, float min, float max, float step){
		this(name, min, max, step, min + (max-min)/2);
	}
	
	
	/**
	 * This method build a bounded argument of type {@link CMLConstants#ARG_TYPE_FLOAT}
	 * @param name the name of this argument
	 * @param min the min value
	 * @param max the max value
	 * @param step the step value, can be equal to 0, which means any value between min and max are valid
	 * @param def the default value
	 * @throws SALRunTimeException if the minimum is greater than the maximum,
	 * if the step value is negative, if the default value is not within [min;max] 
	 * or if name is null
	 */
	public CMLArgument(String name, float min, float max, float step, float def){
		this(name);
		if(min>max)
			throw new SALRunTimeException("The minimum value is greater than the maximum");
		if(step<0)
			throw new SALRunTimeException("The step value is negative");
		if(min>def || max<def)
			throw new SALRunTimeException("The default value is not within [min;max]");
		
		argument.setType(CMLConstants.ARG_TYPE_FLOAT);
		Bounds b = factory.createArgumentBounds();
		b.setMin(String.valueOf(min));
		b.setMax(String.valueOf(max));
		b.setStep(String.valueOf(step));
		argument.setBounds(b);
		argument.setDefaultValue(String.valueOf(def));
	}
	
	/**
	 * This method build a bounded argument of type {@link CMLConstants#ARG_TYPE_INT}.
	 * The default value is set to the median point between min and max, rounded
	 * to the closest lowest integer (equal to (int) (min+(max-min)/2) ).
	 * @param name the name of this argument
	 * @param min the min value
	 * @param max the max value
	 * @param step the step value
	 * @throws SALRunTimeException if the minimum is greater than the maximum,
	 * , if the step value is negative or if name is null
	 */
	public CMLArgument(String name, int min, int max, int step){
		this(name, min, max, step, min+(max-min)/2);
	}
	
	/**
	 * This method build a bounded argument of type {@link CMLConstants#ARG_TYPE_INT}
	 * @param name the name of this argument
	 * @param min the min value
	 * @param max the max value
	 * @param step the step value
	 * @param def the default value
	 * @throws SALRunTimeException if the minimum is greater than the maximum,
	 * if the default value is not within [min;max], if the step value is negative, 
	 * or if name is null
	 */
	public CMLArgument(String name, int min, int max, int step, int def){
		this(name);
		if(min>max)
			throw new SALRunTimeException("The minimum value is greater than the maximum");
		if(step<0)
			throw new SALRunTimeException("The step value is negative");
		if(min>def || max<def)
			throw new SALRunTimeException("The default value is not within [min;max]");
		
		Bounds b = factory.createArgumentBounds();
		b.setMin(String.valueOf(min));
		b.setMax(String.valueOf(max));
		b.setStep(String.valueOf(step));
		argument.setBounds(b);
		argument.setDefaultValue(String.valueOf(def));
		argument.setType(CMLConstants.ARG_TYPE_INT);
	}
	
	/**
	 * This method returns the name of this argument.
	 * @return the name of this argument.
	 */
	public String getName(){
		return argument.getName();
	}
	
	/**
	 * This method returns the type of this argument in
	 * an {@link ArgumentType} object
	 * @return the type of this argument in
	 * an {@link ArgumentType} object.
	 */
	public ArgumentType getType(){
		return new ArgumentType(argument.getType());
	}
	
	/**
	 * This method returns the list of possible values
	 * if this argument is of type {@link CMLConstants#ARG_TYPE_LIST}.
	 * @return the list of possible values for this argument
	 * @throw {@link SALRunTimeException} if this argument is not a list
	 * ({@link CMLConstants#ARG_TYPE_LIST})
	 */
	public Map<String,String> getList(){
		if(!argument.getType().equals(CMLConstants.ARG_TYPE_LIST))
			throw new SALRunTimeException("this argument is not of type list");
		Hashtable<String,String> m = new Hashtable<String, String>();
		for(Item i: argument.getList().getItem())
			m.put(i.getId(), i.getValue());
		return m;
	}
	
	/**
	 * This method returns whether this argument has bounds.
	 * @return whether this argument has bounds.
	 */
	public boolean hasBounds(){
		return argument.getBounds()!=null;
	}
	
	/**
	 * This method returns whether this argument has a default value
	 * @return whether this argument has a default value
	 */
	public boolean hasDefaultValue(){
		return argument.getDefaultValue()!=null;
	}
	
	/**
	 * This method returns the default value for this argument
	 * @return the default value for this argument 
	 */
	public String getDefaultValue(){
		return argument.getDefaultValue();
	}
	
	/**
	 * This method returns the minimum value for this argument
	 * if this argument is of type {@link CMLConstants#ARG_TYPE_INT}.
	 * @return the minimum value for this argument
	 * @throw {@link SALRunTimeException} if this argument is not an int 
	 * ({@link CMLConstants#ARG_TYPE_INT}) or does not have bounds
	 */
	public int getMinInt(){
		if(!hasBounds())
			throw new SALRunTimeException("this argument does not have bounds");
		if(!argument.getType().equals(CMLConstants.ARG_TYPE_INT))
			throw new SALRunTimeException("this argument is not of type integer");
		
		return (int) Float.parseFloat(argument.getBounds().getMin());
	}
	
	/**
	 * This method returns the default value for this argument
	 * if this argument is of type {@link CMLConstants#ARG_TYPE_INT}.
	 * @return the default value for this argument
	 * @throw {@link SALRunTimeException} if this argument is not an int 
	 */
	public int getDefaultValueInt(){
		if(!argument.getType().equals(CMLConstants.ARG_TYPE_INT))
			throw new SALRunTimeException("this argument is not of type integer");
		
		if(!hasDefaultValue())
			throw new SALRunTimeException("this argument does not have a default value");
		
		return (int) Float.parseFloat(argument.getDefaultValue());
	}
	
	/**
	 * This method returns the maximum value for this argument
	 * if this argument is of type {@link CMLConstants#ARG_TYPE_INT}.
	 * @return the maximum value for this argument
	 * @throw {@link SALRunTimeException} if this argument is not an int 
	 * ({@link CMLConstants#ARG_TYPE_INT}) or does not have bounds
	 */
	public int getMaxInt(){
		if(!hasBounds())
			throw new SALRunTimeException("this argument does not have bounds");
		if(!argument.getType().equals(CMLConstants.ARG_TYPE_INT))
			throw new SALRunTimeException("this argument is not of type integer");
		
		return (int) Float.parseFloat(argument.getBounds().getMax());
	}
	
	/**
	 * This method returns the step value for this argument
	 * if this argument is of type {@link CMLConstants#ARG_TYPE_INT}.
	 * @return the step value for this argument
	 * @throw {@link SALRunTimeException} if this argument is not an int 
	 * ({@link CMLConstants#ARG_TYPE_INT}) or does not have bounds
	 */
	public int getStepInt(){
		if(!hasBounds())
			throw new SALRunTimeException("this argument does not have bounds");
		if(!argument.getType().equals(CMLConstants.ARG_TYPE_INT))
			throw new SALRunTimeException("this argument is not of type integer");
		
		return (int) Float.parseFloat(argument.getBounds().getStep());
	}
	
	/**
	 * This method returns the minimum value for this argument
	 * if this argument is of type {@link CMLConstants#ARG_TYPE_FLOAT}.
	 * @return the minimum value for this argument
	 * @throw {@link SALRunTimeException} if this argument is not a float 
	 * ({@link CMLConstants#ARG_TYPE_FLOAT}) or does not have bounds
	 */
	public float getMinFloat(){
		if(!hasBounds())
			throw new SALRunTimeException("this argument does not have bounds");
		if(!argument.getType().equals(CMLConstants.ARG_TYPE_INT))
			throw new SALRunTimeException("this argument is not of type integer");
		
		return Float.parseFloat(argument.getBounds().getMin());
	}
	
	/**
	 * This method returns the default value for this argument
	 * if this argument is of type {@link CMLConstants#ARG_TYPE_INT}.
	 * @return the default value for this argument
	 * @throw {@link SALRunTimeException} if this argument is not an int 
	 */
	public float getDefaultValueFloat(){
		if(!argument.getType().equals(CMLConstants.ARG_TYPE_FLOAT))
			throw new SALRunTimeException("this argument is not of type float");
		
		if(!hasDefaultValue())
			throw new SALRunTimeException("this argument does not have a default value");
		
		return Float.parseFloat(argument.getDefaultValue());
	}
	
	/**
	 * This method returns the maximum value for this argument
	 * if this argument is of type {@link CMLConstants#ARG_TYPE_FLOAT}.
	 * @return the maximum value for this argument
	 * @throw {@link SALRunTimeException} if this argument is not an float 
	 * ({@link CMLConstants#ARG_TYPE_FLOAT}) or does not have bounds
	 */
	public float getMaxFloat(){
		if(!hasBounds())
			throw new SALRunTimeException("this argument does not have bounds");
		if(!argument.getType().equals(CMLConstants.ARG_TYPE_INT))
			throw new SALRunTimeException("this argument is not of type integer");
		
		return Float.parseFloat(argument.getBounds().getMax());
	}
	
	/**
	 * This method returns the step value for this argument
	 * if this argument is of type {@link CMLConstants#ARG_TYPE_FLOAT}.
	 * @return the step value for this argument
	 * @throw {@link SALRunTimeException} if this argument is not a float
	 * ({@link CMLConstants#ARG_TYPE_FLOAT}) or does not have bounds
	 */
	public float getStepFloat(){
		if(!hasBounds())
			throw new SALRunTimeException("this argument does not have bounds");
		if(!argument.getType().equals(CMLConstants.ARG_TYPE_FLOAT))
			throw new SALRunTimeException("this argument is not of type float");
		
		return Float.parseFloat(argument.getBounds().getStep());
	}
	
	/**
	 * This package-private method is meant to be used only by 
	 * {@link CMLDescription} objects.
	 * @return the {@link Argument} object
	 */
	Argument getArgument(){
		return argument;
	}
	
	/**
	 * This method adds a list of acceptable values to this
	 * argument, which must be of type {@link CMLConstants#ARG_TYPE_LIST}.
	 * Otherwise, a {@link SALRunTimeException} will be thrown.
	 * @param items a map of identifiers and associated values.
	 * @throws SALRunTimeException if this argument is not of type 
	 * {@link CMLConstants#ARG_TYPE_LIST}
	 */
	private void addList(Map<String,String> items){
		if(items==null)
			throw new SALRunTimeException("No item list provided");
		
		argument.setList(factory.createArgumentList());
		List<Item> i = argument.getList().getItem();
		Item item;
		for(String id: items.keySet()){
			item = factory.createArgumentListItem(); 
			item.setId(id);
			item.setValue(items.get(id));
			i.add(item);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((argument == null) ? 0 : argument.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CMLArgument other = (CMLArgument) obj;
		if (argument == null) {
			if (other.argument != null)
				return false;
		} else if (!argument.equals(other.argument))
			return false;
		return true;
	}
}
