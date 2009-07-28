package jcu.sal.plugins.protocols.sunspot;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

public class SensorConstants {
	
	/**
	 * The value of the sampling frequency for a single shot command
	 */
	public static final int SINGLE_COMMAND=-1;
    /*
     * TO ADD A NEW SENSOR:
     * - create a new sensor type (TYPE_blabla)
     *
     * - create a new list "sensorCommands"
     *
     * - create static strings for each command (TYPE_DO_this or TYPE_GET_that)
     *
     * - populate "sensorCommands" with each command as the key and a data
     * producer as element
     *
     */


    /**
     * Hashtable<String, List<String>>
     * hashtable<SensorType, List<CMD>>
     * The following map associate sensor types (TYPE_*),
     * with a list of supported commands
     */
    private final static Hashtable<String, List<String>> knownSensors =
    	new Hashtable<String, List<String>>();
    /**
     * a map of commands for the light sensor and associated data producer
     */
    private final static List<String> lightCommands = new Vector<String>();
    /**
     * a map of commands for the temperature sensor and associated data producer
     */
    private final static List<String> tempCommands = new Vector<String>();
    /**
     * a map of commands for the accelerometer and associated data producer
     */
    private final static List<String> accelCommands = new Vector<String>();
    /*
     * Known sensor types
     */
    public final static String TYPE_ACCEL = "1";
    public final static String TYPE_TEMP = "2";
    public final static String TYPE_LIGHT = "3";
    /*
     * NEW SENSORS MUST ALSO BE ADDED TO
     * THE knownSensors LIST BELOW
     */
    static {
        knownSensors.put(TYPE_ACCEL, accelCommands);
        knownSensors.put(TYPE_TEMP, tempCommands);
        knownSensors.put(TYPE_LIGHT, lightCommands);
    }



    /*
     * Light sensor commands
     */
    public final static String LIGHT_GET_LUX = "1";
    /*
     * NEW COMMANDS MUST BE ADDED TO THE
     * lightCommands LIST BELOW
     */
    static {
        lightCommands.add(LIGHT_GET_LUX);
    }



    /*
     * Temp sensor commands
     */
    public final static String TEMP_GET_C = "1";
    /*
     * NEW COMMANDS MUST BE ADDED TO THE
     * tempCommands LIST BELOW
     */
    static {
        tempCommands.add(TEMP_GET_C);
    }



    /*
     * Accelerometersensor commands
     */
    public final static String ACCEL_GET_X = "1";
    public final static String ACCEL_GET_Y = "2";
    public final static String ACCEL_GET_Z = "3";
    public final static String ACCEL_GET_TOTAL = "4";
    /*
     * NEW COMMANDS MUST BE ADDED TO THE
     * accelCommands VECTOR BELOW
     */
    static {
        accelCommands.add(ACCEL_GET_X);
        accelCommands.add(ACCEL_GET_Y);
        accelCommands.add(ACCEL_GET_Z);
        accelCommands.add(ACCEL_GET_TOTAL);
    }


    
    
    
    
    /*
     *
     *  U T I L I T Y   M E T H O D S
     *
     * 
     */


    /**
     * This method returns a list of all known sensor types
     * @return a list of all known sensor types
     */
    public static List<String> getSensorTypeList() {
        Vector<String> v = new Vector<String>(knownSensors.keySet());
        return v;
    }

    /**
     * This method checks if the given sensor type <code>t</code> exists.
     * @param t the sensor type to be tested
     * @return <code>true</code> if the type exists. <code>false</code>
     * otherwise
     */
    public static boolean hasType(String t){
        return knownSensors.containsKey(t);
    }

    /**
     * This method returns the list of commands for a given sensor
     * @param t the sensor type
     * @return (a copy of) the list of its commands, or null if the
     * given sensor type does not exits.
     */
    public static List<String> getSensorCommands(String t) {
        if(!hasType(t))
            return null;
        
        Vector<String> v = new Vector<String>(knownSensors.get(t));
        return v;
    }
}
