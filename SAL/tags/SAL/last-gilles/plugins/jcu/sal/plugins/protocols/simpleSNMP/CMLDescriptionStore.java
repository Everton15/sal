package jcu.sal.plugins.protocols.simpleSNMP;

import javax.naming.ConfigurationException;

import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.ResponseType;
import jcu.sal.common.cml.CMLDescription.SamplingBounds;
import jcu.sal.common.exceptions.AlreadyPresentException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.components.protocols.AbstractStore;


public class CMLDescriptionStore extends AbstractStore{
	private static CMLDescriptionStore c;
	public static String SNMP_KEY="ALL";
	static {
		try {
			c = new CMLDescriptionStore();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public static CMLDescriptionStore getStore() {
		return c;
	}
	

	private CMLDescriptionStore() throws ConfigurationException, AlreadyPresentException, NotFoundException{
		int i;
		String key, name, mName, desc;
		ResponseType r;
		
		/* 
		 * ALL
		 * */
		key = SNMP_KEY;
		mName = SSNMPProtocol.GET_READING_METHOD;
		name = "ReadValue";
		desc = "Reads the value of this sensor";
		r = new ResponseType(CMLConstants.ARG_TYPE_STRING);
		i = addPrivateCommand(key, mName, name, desc, null, r, new SamplingBounds(100,10*1000,false));
		//generic GetReading command
		addGenericCommand(SNMP_KEY, GENERIC_GETREADING, i);
		}
}
