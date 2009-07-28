package jcu.sal.common.sml;

import java.util.List;
import java.util.Vector;

public class SMLConstants {

	public static int SENSOR_ID_MAX = 65535;
	public static int NB_REQUIRED_PARAMETERS;
	
	public static final String SENSOR_TYPE = "Sensor";

	public static final String SENSOR_CONF_NODE= "SensorConfiguration";
	public static final String SENSOR_TAG= "Sensor";
	public static final String SENSOR_ID_ATTRIBUTE_NODE= "sid";
	public static final String PARAMETER_NAME_ATTRIBUTE_NODE = "name";
	public static final String PARAMETER_VALUE_ATTRIBUTE_NODE= "value";
	public static final String SENSOR_ADDRESS_ATTRIBUTE_NODE= "Address";
	public static final String PROTOCOL_NAME_ATTRIBUTE_NODE = "ProtocolName";
	public static final String PROTOCOL_TYPE_ATTRIBUTE_NODE = "ProtocolType";
	
	public static List<String> PARAM_NAMES = new Vector<String>();
	static {
		PARAM_NAMES.add(PROTOCOL_NAME_ATTRIBUTE_NODE);
		PARAM_NAMES.add(PROTOCOL_TYPE_ATTRIBUTE_NODE);
		PARAM_NAMES.add(SENSOR_ADDRESS_ATTRIBUTE_NODE);
		NB_REQUIRED_PARAMETERS = PARAM_NAMES.size();
	}

	
}
