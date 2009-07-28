/**
 * 
 */
package jcu.sal.common.sml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.naming.ConfigurationException;

import jcu.sal.common.Parameters;
import jcu.sal.common.Parameters.Parameter;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.utils.XMLhelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * @author gilles
 *
 */
public class SMLDescriptionTest {
	private Parameters p1, p2, p3;
	
	//missing sid attribute
	private String sml1 = "<"+SMLConstants.SENSOR_TAG+">\n" +
							"<"+Parameters.PARAMETERS_NODE+">\n"+
							"<"+Parameters.PARAMETER_NODE+" "+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+"=\""+
							SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"\" "+SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE+"=\"MyProtocol\" />\n"+
							"<"+Parameters.PARAMETER_NODE+" "+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+"=\""+
							SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE+"\" "+SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE+"=\"MySensorAddress\" />\n"+
							"</"+Parameters.PARAMETERS_NODE+">\n"+
							"</"+SMLConstants.SENSOR_TAG+">";
	
	//has an extra parameter
	private String sml2 = "<"+SMLConstants.SENSOR_TAG+" "+SMLConstants.SENSOR_ID_ATTRIBUTE_NODE+"=\"10\">\n" +
							"<"+Parameters.PARAMETERS_NODE+">\n"+
							"<"+Parameters.PARAMETER_NODE+" "+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+"=\""+
							SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"\" "+SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE+"=\"MyProtocol\" />\n"+
							"<"+Parameters.PARAMETER_NODE+" "+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+"=\""+
							SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE+"\" "+SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE+"=\"MySensorAddress\" />\n"+
							"<"+Parameters.PARAMETER_NODE+" "+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+
							"=\"should\" "+SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE+"=\"goTHROUGH\" />\n"+
							"</"+Parameters.PARAMETERS_NODE+">\n"+
							"</"+SMLConstants.SENSOR_TAG+">";
	
	//missing one required parameter
	private String sml3 = "<"+SMLConstants.SENSOR_TAG+" "+SMLConstants.SENSOR_ID_ATTRIBUTE_NODE+"=\"10\">\n" +
							"<"+Parameters.PARAMETERS_NODE+">\n"+
							"<"+Parameters.PARAMETER_NODE+" "+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+"=\""+
							SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"\" "+SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE+"=\"MyProtocol\" />\n"+
							"</"+Parameters.PARAMETERS_NODE+">\n"+
							"</"+SMLConstants.SENSOR_TAG+">";
	
	//shoud be OK
	private String sml4 = "<"+SMLConstants.SENSOR_TAG+" "+SMLConstants.SENSOR_ID_ATTRIBUTE_NODE+"=\"10\">\n" +
							"<"+Parameters.PARAMETERS_NODE+">\n"+
							"<"+Parameters.PARAMETER_NODE+" "+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+"=\""+
							SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE+"\" "+SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE+"=\"MyProtocol\" />\n"+
							"<"+Parameters.PARAMETER_NODE+" "+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+"=\""+
							SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE+"\" "+SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE+"=\"MySensorAddress\" />\n"+
							"<"+Parameters.PARAMETER_NODE+" "+SMLConstants.PARAMETER_NAME_ATTRIBUTE_NODE+"=\""+
							SMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE+"\" "+SMLConstants.PARAMETER_VALUE_ATTRIBUTE_NODE+"=\"MyProtocolType\" />\n"+
							"</"+Parameters.PARAMETERS_NODE+">\n"+
							"</"+SMLConstants.SENSOR_TAG+">";
	
	
	Document d1, d2, d3, d4;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		Vector<Parameter> v = new Vector<Parameter>();

		//missing one required attribute
		v.add(new Parameter(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE, "MyProtocol"));
		v.add(new Parameter(SMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE, "MyProtocolType"));
		v.add(new Parameter("shoudNOT", "goTHROUGH"));
		p1 = new Parameters(v);
		
		v.removeAllElements();
		//missing two required attributes
		v.add(new Parameter(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE, "MyProtocol"));
		v.add(new Parameter("shoudNOT", "goTHROUGH"));
		p2 = new Parameters(v);
		
		v.removeAllElements();
		//should be ok
		v.add(new Parameter(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE, "MyProtocol"));
		v.add(new Parameter(SMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE, "MyProtocolType"));
		v.add(new Parameter(SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE, "MySensorAddress"));
		p3 = new Parameters(v);
		
		try {
			d1 = XMLhelper.createDocument(sml1);
			d2 = XMLhelper.createDocument(sml2);
			d3 = XMLhelper.createDocument(sml3);
			d4 = XMLhelper.createDocument(sml4);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link jcu.sal.common.sml.SMLDescription#SMLDescription(java.lang.Integer, java.util.Hashtable)}.
	 * @throws SALDocumentException 
	 * @throws ConfigurationException 
	 */
	@Test
	public void testSMLDescriptionIntegerHashtableOfStringString() throws SALDocumentException{
		try {
			new SMLDescription(new Integer(10), p1);
			fail("shoudnt be here");
		} catch (SALDocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			new SMLDescription(new Integer(10), p2);
			fail("shoudnt be here");
		} catch (SALDocumentException e) {}
		
		try {
			new SMLDescription(new Integer(SMLConstants.SENSOR_ID_MAX+1), p3);
			fail("shoudnt be here");
		} catch (SALDocumentException e) {}
		
		new SMLDescription(new Integer(1), p3);
	}

	/**
	 * Test method for {@link jcu.sal.common.sml.SMLDescription#SMLDescription(org.w3c.dom.Document)}.
	 * @throws ConfigurationException 
	 */
	@Test
	public void testSMLDescriptionDocument() throws SALDocumentException {
		try {
			new SMLDescription(d1);
			fail("shoudnt be here");
		} catch (SALDocumentException e) {}
		try {
			new SMLDescription(d2);
			fail("shoudnt be here");
		} catch (SALDocumentException e) {}
		try {
			new SMLDescription(d3);
			fail("shoudnt be here");
		} catch (SALDocumentException e) {}
		System.out.println("Checking d4:"+XMLhelper.toString(d4));
		new SMLDescription(d4);
	}

	/**
	 * Test method for {@link jcu.sal.common.sml.SMLDescription#getSID()}.
	 * @throws ConfigurationException 
	 */
	@Test
	public void testGetSID() throws SALDocumentException {
		SMLDescription s = new SMLDescription(d4);
		assertTrue(s.getSID()==10);
		
		s = new SMLDescription(new Integer(1), p3);
		assertTrue(s.getSID()==1);
	}

	/**
	 * Test method for {@link jcu.sal.common.sml.SMLDescription#getParameter(java.lang.String)}.
	 * @throws NotFoundException 
	 * @throws ConfigurationException 
	 */
	@Test
	public void testGetParameter() throws SALDocumentException, NotFoundException {
		SMLDescription s = new SMLDescription(d4);
		assertEquals(s.getParameter(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE), "MyProtocol");
		assertEquals(s.getParameter(SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE), "MySensorAddress");
		
		s = new SMLDescription(new Integer(1), p3);
		assertEquals(s.getParameter(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE), "MyProtocol");
		assertEquals(s.getParameter(SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE), "MySensorAddress");
	}

	/**
	 * Test method for {@link jcu.sal.common.sml.SMLDescription#getParameterNames()}.
	 * @throws ConfigurationException 
	 */
	@Test
	public void testGetParameterNames() throws SALDocumentException {
		SMLDescription s = new SMLDescription(d4);
		Set<String> set = new HashSet<String>();
		Set<String> set1 = new HashSet<String>();
		Set<String> set2 = new HashSet<String>();
		Set<String> set3 = new HashSet<String>();
		set.add(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE);
		set.add(SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE);
		set.add(SMLConstants.PROTOCOL_TYPE_ATTRIBUTE_NODE);
		
		set1.add(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE);
		set1.add(SMLConstants.SENSOR_ADDRESS_ATTRIBUTE_NODE);
		set1.add("DONTWORK");
		
		set2.add(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE);

		set3.add(SMLConstants.PROTOCOL_NAME_ATTRIBUTE_NODE);
		set3.add("DONTWORK");
		
		assertEquals(s.getParameterNames(), set);
		assertFalse(s.getParameterNames().equals(set1));
		assertFalse(s.getParameterNames().equals(set2));
		assertFalse(s.getParameterNames().equals(set3));
		
		s = new SMLDescription(new Integer(1), p3);
		assertEquals(s.getParameterNames(), set);
		assertFalse(s.getParameterNames().equals(set1));
		assertFalse(s.getParameterNames().equals(set2));
		assertFalse(s.getParameterNames().equals(set3));
	}

}
