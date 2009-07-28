package jcu.sal.common.pcml;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.naming.ConfigurationException;

import jcu.sal.common.Parameters;
import jcu.sal.common.exceptions.SALDocumentException;

import org.junit.Test;

public class EndPointConfigurationTest {

	@Test
	public void testEndPointConfigurationStringStringParameters() throws ConfigurationException, SALDocumentException {
		new EndPointConfiguration("name1", "type1", new Parameters("<"+Parameters.PARAMETERS_NODE+" />"));
	}

	@Test
	public void testEndPointConfigurationDocument() throws ConfigurationException, SALDocumentException {
		
		//should pass
		String ep = "<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
					+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" />";
		System.out.println(ep);
		new EndPointConfiguration(ep);
		
		//should pass
		ep = "<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
			+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" >"
			+"<"+Parameters.PARAMETERS_NODE+">"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name1\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value1\" />"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name2\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value2\" />"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name3\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value3\" />"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name4\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value4\" />"
			+"</"+Parameters.PARAMETERS_NODE+"></" + PCMLConstants.ENDPOINT_NODE+">";
		System.out.println(ep);
		new EndPointConfiguration(ep);
		
		//should pass
		ep = "<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
		+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" >"
		+"</" + PCMLConstants.ENDPOINT_NODE+">";
		System.out.println(ep);
		new EndPointConfiguration(ep);
		
		//shoud fail, no name
		ep = "<"+PCMLConstants.ENDPOINT_NODE+" "
		+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" >"
		+"</" + PCMLConstants.ENDPOINT_NODE+">";
		System.out.println(ep);
		try {new EndPointConfiguration(ep); fail("shouldnt be here");}
		catch (Exception e){}
		
		
		//shoud fail, no type
		ep = "<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" >"
		+"</" + PCMLConstants.ENDPOINT_NODE+">";
		System.out.println(ep);
		try {new EndPointConfiguration(ep); fail("shouldnt be here");}
		catch (Exception e){}
	}

	@Test
	public void testGetName() throws ConfigurationException, SALDocumentException {
		//should pass
		String ep = "<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
			+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" >"
			+"<"+Parameters.PARAMETERS_NODE+">"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name1\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value1\" />"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name2\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value2\" />"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name3\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value3\" />"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name4\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value4\" />"
			+"</"+Parameters.PARAMETERS_NODE+"></" + PCMLConstants.ENDPOINT_NODE+">";
		System.out.println(ep);
		EndPointConfiguration epc = new EndPointConfiguration(ep);
		assertTrue(epc.getID().equals("testEP"));
	}

	@Test
	public void testGetType() throws ConfigurationException, SALDocumentException {
//		should pass
		String ep = "<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
			+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" >"
			+"<"+Parameters.PARAMETERS_NODE+">"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name1\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value1\" />"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name2\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value2\" />"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name3\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value3\" />"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name4\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value4\" />"
			+"</"+Parameters.PARAMETERS_NODE+"></" + PCMLConstants.ENDPOINT_NODE+">";
		System.out.println(ep);
		EndPointConfiguration epc = new EndPointConfiguration(ep);
		assertTrue(epc.getType().equals("epType"));
	}

	@Test
	public void testGetParameter() throws ConfigurationException, SALDocumentException {
//		should pass
		String params = "<"+Parameters.PARAMETERS_NODE+">"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name1\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value1\" />"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name2\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value2\" />"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name3\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value3\" />"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name4\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value4\" />"
			+"</"+Parameters.PARAMETERS_NODE+">";
		String ep = "<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
			+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" >"
			+params
			+"</" + PCMLConstants.ENDPOINT_NODE+">";
		System.out.println(ep);
		EndPointConfiguration epc = new EndPointConfiguration(ep);
		Parameters p = new Parameters(params);
		assertTrue(epc.getParameters().equals(p));
	}

}
