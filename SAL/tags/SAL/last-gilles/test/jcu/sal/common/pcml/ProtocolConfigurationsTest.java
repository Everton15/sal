package jcu.sal.common.pcml;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Vector;

import javax.naming.ConfigurationException;

import jcu.sal.common.Parameters;
import jcu.sal.common.exceptions.AlreadyPresentException;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;

import org.junit.Test;

public class ProtocolConfigurationsTest {
	private String params = "<"+Parameters.PARAMETERS_NODE+">"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name1\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value1\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name2\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value2\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name3\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value3\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name4\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value4\" />"
		+"</"+Parameters.PARAMETERS_NODE+">";
	
	//	should pass
	private String ep1 = "<"+PCMLConstants.PROTOCOL_NODE+" "+PCMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"=\"testProtocol1\" "
		+PCMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"=\"testTypeProtocol\">"
		+params
		+"<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
		+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" >"
		+params + "</" + PCMLConstants.ENDPOINT_NODE+">"
		+"</"+PCMLConstants.PROTOCOL_NODE+">";

	
	//should pass
	private String ep2 = "<"+PCMLConstants.PROTOCOL_NODE+" "+PCMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"=\"testProtocol2\" "
		+PCMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"=\"testTypeProtocol\">"
		+"<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
		+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" />"
		+"</"+PCMLConstants.PROTOCOL_NODE+">";

	
	//should pass
	private String ep3 = "<"+PCMLConstants.PROTOCOL_NODE+" "+PCMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"=\"testProtocol3\" "
		+PCMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"=\"testTypeProtocol\">"
		+params
		+"<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
		+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" />"
		+"</"+PCMLConstants.PROTOCOL_NODE+">";
	
	
	//should pass
	private String ep4 = "<"+PCMLConstants.PROTOCOL_NODE+" "+PCMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"=\"testProtocol4\" "
		+PCMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"=\"testTypeProtocol\">"
		+"<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
		+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\">"
		+params
		+"</"+PCMLConstants.ENDPOINT_NODE+">"
		+"</"+PCMLConstants.PROTOCOL_NODE+">";


	
	//shoud fail, no name
	private String ep5 = "<"+PCMLConstants.PROTOCOL_NODE+" "
		+PCMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"=\"testTypeProtocol\">"
		+"<"+PCMLConstants.ENDPOINT_NODE+" "+PCMLConstants.ENDPOINT_NAME_ATTRIBUTE_NODE+"=\"testEP\" "
		+ PCMLConstants.ENDPOINT_TYPE_ATTRIBUTE_NODE+"=\"epType\" />"
		+"</"+PCMLConstants.PROTOCOL_NODE+">";
	

	//should fail, no ep config
	private String ep6 = "<"+PCMLConstants.PROTOCOL_NODE+" "+PCMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"=\"testProtocol\" "
		+PCMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"=\"testTypeProtocol\">"
		+params
		+"</"+PCMLConstants.PROTOCOL_NODE+">";

	//should pass
	private String pcml1 = "<"+PCMLConstants.PLATFORM_CONFIGURATION_NODE+">"
							+ep1
							+ep2
							+ep3
							+ep4
							+"</"+PCMLConstants.PLATFORM_CONFIGURATION_NODE+">";
	
	//should fail, contains ep5, which is not valid
	private String pcml2 = "<"+PCMLConstants.PLATFORM_CONFIGURATION_NODE+">"
							+ep5
							+ep2
							+ep3
							+ep4
							+"</"+PCMLConstants.PLATFORM_CONFIGURATION_NODE+">";
	
	//should fail no platform configuration node
	private String pcml3 = ep1;
	
	//should pass, empty document
	private String pcml4 = "<"+PCMLConstants.PLATFORM_CONFIGURATION_NODE+" />";
	
	//should fail, contains a protocol with the same name twice
	private String pcml5 = "<"+PCMLConstants.PLATFORM_CONFIGURATION_NODE+">"
							+ep1
							+ep2
							+ep3
							+ep1
							+"</"+PCMLConstants.PLATFORM_CONFIGURATION_NODE+">";

	@Test
	public void testProtocolConfigurationsListOfProtocolConfiguration() throws ConfigurationException, SALDocumentException, AlreadyPresentException {
		Vector<ProtocolConfiguration> l = new Vector<ProtocolConfiguration>();
		l.add(new ProtocolConfiguration(ep1));
		l.add(new ProtocolConfiguration(ep2));
		l.add(new ProtocolConfiguration(ep3));
		l.add(new ProtocolConfiguration(ep4));
		try {
			l.add(new ProtocolConfiguration(ep5));
			fail("shouldnt be here");
		} catch (Exception e){}
		try {
			l.add(new ProtocolConfiguration(ep6));
			fail("shouldnt be here");
		} catch (Exception e){}
		new ProtocolConfigurations(l);
	}

	@Test
	public void testProtocolConfigurationsString() throws ConfigurationException, SALDocumentException {
		new ProtocolConfigurations(pcml1);
		try {
			new ProtocolConfigurations(pcml2);
			fail("shouldnt be here");
		} catch (Exception e){}
		try {
			new ProtocolConfigurations(pcml3);
			fail("shouldnt be here");
		} catch (Exception e){}
		new ProtocolConfigurations(pcml4);
		try {
			new ProtocolConfigurations(pcml5);
			fail("shouldnt be here");
		} catch (Exception e){}
	}

	@Test
	public void testGetPIDs() throws ConfigurationException, SALDocumentException {
		ProtocolConfigurations p = new ProtocolConfigurations(pcml1);
		HashSet<String> v = new HashSet<String>();
		v.add("testProtocol1");
		v.add("testProtocol2");
		v.add("testProtocol3");
		v.add("testProtocol4");
		assertTrue(p.getPIDs().equals(v));
		
		p = new ProtocolConfigurations(pcml4);
		v.clear();
		assertTrue(p.getPIDs().equals(v));	
	}

	@Test
	public void testGetConfigurations() throws ConfigurationException, SALDocumentException {
		ProtocolConfigurations p = new ProtocolConfigurations(pcml1);
		HashSet<ProtocolConfiguration> v = new HashSet<ProtocolConfiguration>();
		v.add(new ProtocolConfiguration(ep1));
		v.add(new ProtocolConfiguration(ep2));
		v.add(new ProtocolConfiguration(ep3));
		v.add(new ProtocolConfiguration(ep4));
		assertTrue(p.getConfigurations().equals(v));
		
		p = new ProtocolConfigurations(pcml4);
		v.clear();
		assertTrue(p.getConfigurations().equals(v));	
	}

	@Test()
	public void testGetDescription() throws ConfigurationException, SALDocumentException, NotFoundException {
		ProtocolConfigurations p = new ProtocolConfigurations(pcml1);
		assertTrue(p.getDescription("testProtocol1").equals(new ProtocolConfiguration(ep1)));
		assertTrue(p.getDescription("testProtocol2").equals(new ProtocolConfiguration(ep2)));
		assertTrue(p.getDescription("testProtocol3").equals(new ProtocolConfiguration(ep3)));
		assertTrue(p.getDescription("testProtocol4").equals(new ProtocolConfiguration(ep4)));
		
		p = new ProtocolConfigurations(pcml4);
		try {
			p.getDescription("test");
			fail("shouldnt be here");
		} catch (Exception e){}
	}
}
