package jcu.sal.plugins.protocols.v4l2;

import jcu.sal.components.protocols.AbstractStore;


public class CMLDescriptionStore extends AbstractStore {
	public static final String CCD_KEY = "CCD";
	public static final String CONTROL_VALUE_NAME="value";
	public static final String WIDTH_VALUE_NAME="width";
	public static final String HEIGHT_VALUE_NAME="height";
	public static final String CHANNEL_VALUE_NAME="channel";
	public static final String STANDARD_VALUE_NAME="standard";
	public static final String FORMAT_VALUE_NAME="format";
	public static final String QUALITY_VALUE_NAME="quality";

	
	public static CMLDescriptionStore getStore() {
		//return c;
		try {
			return new CMLDescriptionStore();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
