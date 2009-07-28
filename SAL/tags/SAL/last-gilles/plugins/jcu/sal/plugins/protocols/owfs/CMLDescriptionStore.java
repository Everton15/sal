package jcu.sal.plugins.protocols.owfs;

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
		return c;
	}
	
	private CMLDescriptionStore() throws ConfigurationException, NotFoundException, AlreadyPresentException{
		int i;
		String key, name, mName, desc;
		ResponseType r;
		
		/* 
		 * Family 10. 
		 * */
		key = OWFSProtocol.DS_10_FAMILY;
		mName = OWFSProtocol.GET_TEMPERATURE_METHOD;
		name = "ReadTemperature";
		desc = "Reads the temperature";
		r = new ResponseType(CMLConstants.RET_TYPE_FLOAT, CMLConstants.CONTENT_TYPE_TEXT_PLAIN, CMLConstants.UNIT_DEGREE_C);
		i = addPrivateCommand(key, mName, name, desc, null, r, new SamplingBounds(500,10*1000,false));
		//generic GetReading command
		addGenericCommand(OWFSProtocol.DS_10_FAMILY, GENERIC_GETREADING, i);
		//generic GetTemp command
		addGenericCommand(OWFSProtocol.DS_10_FAMILY, GENERIC_GETTEMP, i);
		
		/* 
		 * Family 26. 
		 * */
		key = OWFSProtocol.DS_26_FAMILY;
		mName = OWFSProtocol.GET_HUMIDITY_METHOD;
		name = "ReadHumidity";
		desc = "Reads the humidity";
		r = new ResponseType(CMLConstants.RET_TYPE_FLOAT, CMLConstants.CONTENT_TYPE_TEXT_PLAIN, CMLConstants.UNIT_PERCENT);
		i = addPrivateCommand(key, mName, name, desc, null, r, new SamplingBounds(500,10*1000,false));
		//generic GetReading command
		addGenericCommand(OWFSProtocol.DS_26_FAMILY, GENERIC_GETREADING, i);
		//generic GetHUMIDITY command
		addGenericCommand(OWFSProtocol.DS_26_FAMILY, GENERIC_GETHUM, i);
		
		key = OWFSProtocol.DS_26_FAMILY;
		mName = OWFSProtocol.GET_TEMPERATURE_METHOD;
		name = "ReadTemperature";
		desc = "Reads the temperature";
		r = new ResponseType(CMLConstants.RET_TYPE_FLOAT, CMLConstants.CONTENT_TYPE_TEXT_PLAIN, CMLConstants.UNIT_DEGREE_C);
		i = addPrivateCommand(key, mName, name, desc, null, r, new SamplingBounds(500,10*1000,false));
		//generic GetTemp command
		addGenericCommand(OWFSProtocol.DS_26_FAMILY, GENERIC_GETTEMP, i);

		//private GetHumidityHIH4000 command
		key = OWFSProtocol.DS_26_FAMILY;
		mName = OWFSProtocol.GET_HIH400_METHOD;
		name = "ReadHIH400Humidity";
		desc = "Reads the humidity from a HIH4000 sensor";
		r = new ResponseType(CMLConstants.RET_TYPE_FLOAT, CMLConstants.CONTENT_TYPE_TEXT_PLAIN, CMLConstants.UNIT_PERCENT);
		addPrivateCommand(key, mName, name, desc, null, r, new SamplingBounds(500,10*1000,false));
		
		//private GetHumidityHTM1735 command
		key = OWFSProtocol.DS_26_FAMILY;
		mName = OWFSProtocol.GET_HTM1735_METHOD;
		name = "ReadHTM1735Humidity";
		desc = "Reads the humidity from a HTM1735 sensor";
		addPrivateCommand(key, mName, name, desc, null, r, new SamplingBounds(500,10*1000,false));
		
		//private GetVAD command
		key = OWFSProtocol.DS_26_FAMILY;
		mName = OWFSProtocol.GET_VAD_METHOD;
		name = "ReadVAD";
		desc = "Reads the Vad voltage";
		r = new ResponseType(CMLConstants.RET_TYPE_FLOAT, CMLConstants.CONTENT_TYPE_TEXT_PLAIN, CMLConstants.UNIT_VOLTS);
		addPrivateCommand(key, mName, name, desc, null, r, new SamplingBounds(100,10*1000,false));
		
		//private GetVDD command
		key = OWFSProtocol.DS_26_FAMILY;
		mName = OWFSProtocol.GET_VDD_METHOD;
		name = "ReadVDD";
		desc = "Reads the Vdd voltage";
		addPrivateCommand(key, mName, name, desc, null, r, new SamplingBounds(100,10*1000,false));
		
		//private GetVis command
		key = OWFSProtocol.DS_26_FAMILY;
		mName = OWFSProtocol.GET_VIS_METHOD;
		name = "ReadVIS";
		desc = "Reads the Vis voltage";
		addPrivateCommand(key, mName, name, desc, null, r, new SamplingBounds(100,10*1000,false));
	}
}
