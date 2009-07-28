package jcu.sal.common.cml;

public interface CMLConstants {
	/**
	 * 
	 * A R G U M E N T   T Y P E S
	 * 
	 */
	/*
	 * The strings below must match the possible "type" attribute
	 * in the CML schema. Additions and removals must be also
	 * updated in the ARG_TYPES array
	 */
	public static final String ARG_TYPE_STRING="string";
	public static final String ARG_TYPE_INT="int";
	public static final String ARG_TYPE_FLOAT="float";
	public static final String ARG_TYPE_LIST="list";
	
	/*
	 * Update the array below with additions/removals of argument types
	 */
	public static final String[] ARG_TYPES={
		ARG_TYPE_STRING,
		ARG_TYPE_INT,
		ARG_TYPE_FLOAT,
		ARG_TYPE_LIST,
	};
	
	
	
	
	/**
	 * 
	 * R E T U R N   T Y P E S
	 * 
	 */
	/*
	 * Additions and removals must be also
	 * updated in the RET_TYPE array
	 */
	public static final String RET_TYPE_INT="int";
	public static final String RET_TYPE_FLOAT="float";
	public static final String RET_TYPE_STRING="string";
	public static final String RET_TYPE_BYTE_ARRAY="byte array";
	public static final String RET_TYPE_VOID="void";
	
	/*
	 * Update the array below with additions/removals of return types
	 */
	public static final String[] RET_TYPES={
		RET_TYPE_INT,
		RET_TYPE_FLOAT,
		RET_TYPE_STRING,
		RET_TYPE_BYTE_ARRAY,
		RET_TYPE_VOID
	};
	
	
	
	
	/**
	 * 
	 * C O N T E N T   T Y P E S
	 * 
	 */
	/*
	 * Additions and removals must be also
	 * updated in the CONTENT_TYPES array
	 */
	public static final String CONTENT_TYPE_JPEG="image/jpeg";
	public static final String CONTENT_TYPE_RGB24="image/x-rgb";
	public static final String CONTENT_TYPE_IMAGE_RAW="image/raw";
	public static final String CONTENT_TYPE_TEXT_PLAIN="text/plain";
	
	/*
	 * Update the array below with additions/removals of content types
	 */
	public static final String[] CONTENT_TYPES={
		CONTENT_TYPE_JPEG,
		CONTENT_TYPE_RGB24,
		CONTENT_TYPE_TEXT_PLAIN,
		CONTENT_TYPE_IMAGE_RAW
	};
	
	/**
	 * 
	 * U N I T S
	 * 
	 */
	/*
	 * Additions and removals must be also
	 * updated in the UNITS array
	 */
	public static final String UNIT_NONE="none";
	public static final String UNIT_PERCENT="%";
	public static final String UNIT_DEGREE_C="degree C";
	public static final String UNIT_MBYTES="MBytes";
	public static final String UNIT_VOLTS="Volts";
	public static final String UNIT_ACCCEL_G="G";
	public static final String UNIT_LUX="Lux";
	
	/*
	 * Update the array below with additions/removals of units
	 */
	public static final String[] UNITS={
		UNIT_NONE,
		UNIT_PERCENT,
		UNIT_DEGREE_C,
		UNIT_MBYTES,
		UNIT_VOLTS,
		UNIT_ACCCEL_G,
		UNIT_LUX
	};

}
