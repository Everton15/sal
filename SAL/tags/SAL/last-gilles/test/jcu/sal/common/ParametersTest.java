package jcu.sal.common;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Vector;

import javax.naming.ConfigurationException;

import jcu.sal.common.Parameters.Parameter;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;

import org.junit.Test;

public class ParametersTest {

	@Test
	public void testParametersListOfParameter() {
		List<Parameter> l = new Vector<Parameter>();
		l.add(new Parameter("name1", "value1"));
		l.add(new Parameter("name2", "value2"));
		l.add(new Parameter("name3", "value3"));
		new Parameters(l);		
	}

	@Test(expected=ConfigurationException.class)
	public void testParametersString() throws ConfigurationException, SALDocumentException {
		String d = "<"+Parameters.PARAMETERS_NODE+"/>";
		try {
			new Parameters(d);
		} catch (Exception e) {
			e.printStackTrace();
			fail("shouldnt be here");
		}
		
		d = "<"+Parameters.PARAMETERS_NODE+">"
					+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
					+"\"name1\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value1\" />"
					+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
					+"\"name2\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value2\" />"
					+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
					+"\"name3\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value3\" />"
					+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
					+"\"name4\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value4\" />"
					+"</"+Parameters.PARAMETERS_NODE+">";
		try {
			new Parameters(d);
		} catch (Exception e) {
			e.printStackTrace();
			fail("shouldnt be here");
		}
		
		d = "<test><child><child2><"+Parameters.PARAMETERS_NODE+">"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name1\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value1\" />"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name2\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value2\" />"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name3\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value3\" />"
			+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
			+"\"name4\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value4\" />"
			+"</"+Parameters.PARAMETERS_NODE+"></child2></child></test>";
		try {
			new Parameters(d);
		} catch (Exception e) {
			fail("shouldnt be here");
		}

		
		d = "<test><child><child2><"+Parameters.PARAMETERS_NODE+">"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name1\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value1\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name2\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value2\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name3\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value3\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name4\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value4\" />"
		+"</"+Parameters.PARAMETERS_NODE+"></child2>"
		+"<"+Parameters.PARAMETERS_NODE+">"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name1\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value1\" />"
		+"</"+Parameters.PARAMETERS_NODE+">"
		+"</child></test>";
		System.out.println(d);
		new Parameters(d);
		fail("shouldnt be here");
	}

	@Test
	public void testGetParameter() throws ConfigurationException, NotFoundException, SALDocumentException {
		String d = "<test><child><child2><"+Parameters.PARAMETERS_NODE+">"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name1\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value1\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name2\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value2\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name3\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value3\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name4\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value4\" />"
		+"</"+Parameters.PARAMETERS_NODE+"></child2></child></test>";
	Parameters p = new Parameters(d);
	
	assertTrue(p.getParameter("name1").getStringValue().equals("value1"));
	assertTrue(p.getParameter("name2").getStringValue().equals("value2"));
	assertTrue(p.getParameter("name3").getStringValue().equals("value3"));
	assertTrue(p.getParameter("name4").getStringValue().equals("value4"));
	

	}
	
	@Test
	public void testEquals() throws ConfigurationException, SALDocumentException {
		String d = "<test><child><child2><"+Parameters.PARAMETERS_NODE+">"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name1\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value1\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name2\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value2\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name3\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value3\" />"
		+"<"+Parameters.PARAMETER_NODE+" "+Parameters.NAME_ATTRIBUTE_NODE+"="
		+"\"name4\" "+Parameters.VALUE_ATTRIBUTE_NODE+"=\"value4\" />"
		+"</"+Parameters.PARAMETERS_NODE+"></child2></child></test>";
	Parameters p = new Parameters(d);
	Parameters p1 = new Parameters(d);
	
	assertTrue(p.equals(p1));
	

	}

}
