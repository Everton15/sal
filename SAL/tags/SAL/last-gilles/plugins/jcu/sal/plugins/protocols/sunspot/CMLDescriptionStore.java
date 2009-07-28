package jcu.sal.plugins.protocols.sunspot;

import javax.naming.ConfigurationException;

import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.ResponseType;
import jcu.sal.common.cml.CMLDescription.SamplingBounds;
import jcu.sal.common.exceptions.AlreadyPresentException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.components.protocols.AbstractStore;


public class CMLDescriptionStore extends AbstractStore {

	private static CMLDescriptionStore c; 
	static {
		try {
			c = new CMLDescriptionStore();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public static CMLDescriptionStore getStore() {
		//same store for everybody
		return c;
	}
	
	private CMLDescriptionStore() throws ConfigurationException, NotFoundException, AlreadyPresentException{
		int i;
		String key, name, mName, desc;
		ResponseType r;

		/* 
		 * ACCEL TOTAL
		 * */
		key = SensorConstants.TYPE_ACCEL;
		mName = SunSPOTProtocol.SEND_ACCEL_TOTAL_METHOD;
		name = "ReadAcceleration";
		desc = "Reads the current total acceleration (vector sum)";
		r = new ResponseType(CMLConstants.RET_TYPE_FLOAT, CMLConstants.CONTENT_TYPE_TEXT_PLAIN, CMLConstants.UNIT_ACCCEL_G);
		i = addPrivateCommand(key, mName, name, desc, null, r, new SamplingBounds(20,1000,true));
		//generic GetReading command
		addGenericCommand(SensorConstants.TYPE_ACCEL, GENERIC_GETREADING, i);
		//generic GetAccel command
		addGenericCommand(SensorConstants.TYPE_ACCEL, GENERIC_GETACCEL, i);

		
		/* 
		 * ACCEL X
		 * */
		key = SensorConstants.TYPE_ACCEL;
		mName = SunSPOTProtocol.SEND_ACCEL_X_METHOD;
		name = "ReadAccelerationX";
		desc = "Reads the acceleration along the X-axis";
		r = new ResponseType(CMLConstants.RET_TYPE_FLOAT, CMLConstants.CONTENT_TYPE_TEXT_PLAIN, CMLConstants.UNIT_ACCCEL_G);
		i = addPrivateCommand(key, mName, name, desc, null, r, new SamplingBounds(20,1000,true));
		
		/* 
		 * ACCEL Y
		 * */
		key = SensorConstants.TYPE_ACCEL;
		mName = SunSPOTProtocol.SEND_ACCEL_Y_METHOD;
		name = "ReadAccelerationY";
		desc = "Reads the acceleration along the Y-axis";
		r = new ResponseType(CMLConstants.RET_TYPE_FLOAT, CMLConstants.CONTENT_TYPE_TEXT_PLAIN, CMLConstants.UNIT_ACCCEL_G);
		i = addPrivateCommand(key, mName, name, desc, null, r, new SamplingBounds(20,1000,true));

		/* 
		 * ACCEL Z
		 * */
		key = SensorConstants.TYPE_ACCEL;
		mName = SunSPOTProtocol.SEND_ACCEL_Z_METHOD;
		name = "ReadAccelerationZ";
		desc = "Reads the acceleration along the Z-axis";
		r = new ResponseType(CMLConstants.RET_TYPE_FLOAT, CMLConstants.CONTENT_TYPE_TEXT_PLAIN, CMLConstants.UNIT_ACCCEL_G);
		i = addPrivateCommand(key, mName, name, desc, null, r, new SamplingBounds(20,1000,true));
		
		/* 
		 * TEMP
		 * */
		key = SensorConstants.TYPE_TEMP;
		mName = SunSPOTProtocol.SEND_TEMP_C_METHOD;
		name = "ReadTemp";
		desc = "Reads the temperature";
		r = new ResponseType(CMLConstants.RET_TYPE_FLOAT, CMLConstants.CONTENT_TYPE_TEXT_PLAIN, CMLConstants.UNIT_DEGREE_C);
		i = addPrivateCommand(key, mName, name, desc, null, r, new SamplingBounds(200,10*1000,true));
		//generic GetReading command
		addGenericCommand(SensorConstants.TYPE_TEMP, GENERIC_GETREADING, i);
		//generic GetTemp command
		addGenericCommand(SensorConstants.TYPE_TEMP, GENERIC_GETTEMP, i);
		
		/* 
		 * LIGHT
		 * */
		key = SensorConstants.TYPE_LIGHT;
		mName = SunSPOTProtocol.SEND_LIGHT_LUX_METHOD;
		name = "ReadLux";
		desc = "Reads the luminance";
		r = new ResponseType(CMLConstants.RET_TYPE_INT, CMLConstants.CONTENT_TYPE_TEXT_PLAIN, CMLConstants.UNIT_LUX);
		i = addPrivateCommand(key, mName, name, desc, null, r, new SamplingBounds(100,10*1000,true));
		//generic GetReading command
		addGenericCommand(SensorConstants.TYPE_LIGHT, GENERIC_GETREADING, i);
		//generic GetTemp command
		addGenericCommand(SensorConstants.TYPE_LIGHT, GENERIC_GETLUX, i);
	}
}
