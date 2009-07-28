/**
 * 
 */
package jcu.sal.common.cml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Vector;

import javax.naming.ConfigurationException;

import jcu.sal.common.XMLhelper;


import org.junit.Test;
import org.w3c.dom.Document;

/**
 * @author gilles
 *
 */
public class CMLDescriptionTest {
	private CMLDescription c1, c2, c3,c4, c5;
	private String cml1 = "<"+CMLConstants.CMD_DESCRIPTION_TAG+" "+CMLConstants.CID_ATTRIBUTE+"=\"100\">\n"
								+"<"+CMLConstants.NAME_TAG+">NoArgDoc</"+CMLConstants.NAME_TAG+">\n"
								+"<"+CMLConstants.SHORT_DESCRIPTION_TAG+">it works without args too</"+CMLConstants.SHORT_DESCRIPTION_TAG+">\n"
								+"<"+CMLConstants.RETURN_TYPE_TAG+" type=\""+CMLConstants.RET_TYPE_VOID+"\" />\n"
								+"</"+CMLConstants.CMD_DESCRIPTION_TAG+">\n";
	
	private String cml2 = "<"+CMLConstants.CMD_DESCRIPTION_TAG+" "+CMLConstants.CID_ATTRIBUTE+"=\"100\">\n"
								+"<"+CMLConstants.SHORT_DESCRIPTION_TAG+">it works without args too</"+CMLConstants.SHORT_DESCRIPTION_TAG+">\n"
								+"<"+CMLConstants.RETURN_TYPE_TAG+" type=\""+CMLConstants.RET_TYPE_VOID+"\" />\n"
								+"</"+CMLConstants.CMD_DESCRIPTION_TAG+">\n";
	
	private String cml3 = "<"+CMLConstants.CMD_DESCRIPTION_TAG+">\n"
								+"<"+CMLConstants.NAME_TAG+">noCIDDoc</"+CMLConstants.NAME_TAG+">\n"
								+"<"+CMLConstants.SHORT_DESCRIPTION_TAG+">it works without args too</"+CMLConstants.SHORT_DESCRIPTION_TAG+">\n"
								+"<"+CMLConstants.RETURN_TYPE_TAG+" type=\""+CMLConstants.RET_TYPE_VOID+"\" />\n"
								+"</"+CMLConstants.CMD_DESCRIPTION_TAG+">\n";
	
	private String cml4 = "<"+CMLConstants.CMD_DESCRIPTION_TAG+" "+CMLConstants.CID_ATTRIBUTE+"=\"100\">\n"
								+"<"+CMLConstants.NAME_TAG+">noDescDoc</"+CMLConstants.NAME_TAG+">\n"
								+"<"+CMLConstants.RETURN_TYPE_TAG+" type=\""+CMLConstants.RET_TYPE_VOID+"\" />\n"
								+"</"+CMLConstants.CMD_DESCRIPTION_TAG+">\n";

	private String cml5 = "<"+CMLConstants.CMD_DESCRIPTION_TAG+" "+CMLConstants.CID_ATTRIBUTE+"=\"100\">\n"
								+"<"+CMLConstants.NAME_TAG+">noRetTypeDoc</"+CMLConstants.NAME_TAG+">\n"
								+"<"+CMLConstants.SHORT_DESCRIPTION_TAG+">it works without args too</"+CMLConstants.SHORT_DESCRIPTION_TAG+">\n"
								+"</"+CMLConstants.CMD_DESCRIPTION_TAG+">\n";
	
	private String cml6 = "<"+CMLConstants.CMD_DESCRIPTION_TAG+" "+CMLConstants.CID_ATTRIBUTE+"=\"100\">\n"
								+"<"+CMLConstants.NAME_TAG+">OKDoc</"+CMLConstants.NAME_TAG+">\n"
								+"<"+CMLConstants.SHORT_DESCRIPTION_TAG+">it works without args too</"+CMLConstants.SHORT_DESCRIPTION_TAG+">\n"
								+"<"+CMLConstants.ARGUMENTS_TAG+">"
								+"<"+CMLConstants.ARGUMENT_TAG+" "+CMLConstants.NAME_ATTRIBUTE+"=\"myString\" type=\""+CMLConstants.ARG_TYPE_STRING+"\"/>"
								+"<"+CMLConstants.ARGUMENT_TAG+" "+CMLConstants.NAME_ATTRIBUTE+"=\"Callback\" type=\""+CMLConstants.ARG_TYPE_CALLBACK+"\"/>"
								+"</"+CMLConstants.ARGUMENTS_TAG+">"
								+"<"+CMLConstants.RETURN_TYPE_TAG+" type=\""+CMLConstants.RET_TYPE_VOID+"\" />\n"
								+"</"+CMLConstants.CMD_DESCRIPTION_TAG+">\n";
	private String cml7 = "<"+CMLConstants.CMD_DESCRIPTION_TAG+" "+CMLConstants.CID_ATTRIBUTE+"=\"100\">\n"
								+"<"+CMLConstants.NAME_TAG+">OKDoc</"+CMLConstants.NAME_TAG+">\n"
								+"<"+CMLConstants.SHORT_DESCRIPTION_TAG+">it works without args too</"+CMLConstants.SHORT_DESCRIPTION_TAG+">\n"
								+"<"+CMLConstants.ARGUMENTS_TAG+">"
								+"<"+CMLConstants.ARGUMENT_TAG+" "+CMLConstants.NAME_ATTRIBUTE+"=\"myInt\" type=\""+CMLConstants.ARG_TYPE_INT+"\"/>"
								+"</"+CMLConstants.ARGUMENTS_TAG+">"
								+"<"+CMLConstants.RETURN_TYPE_TAG+" type=\""+CMLConstants.RET_TYPE_BYTE_ARRAY+"\" />\n"
								+"</"+CMLConstants.CMD_DESCRIPTION_TAG+">\n";
	
	Document d1, d2,d3, d4, d5, d6, d7;


	/**
	 * Test method for {@link jcu.sal.common.cml.CMLDescription#CMLDescription(java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.util.List, java.util.List, jcu.sal.common.cml.ReturnType)}.
	 * @throws Exception 
	 */
	@Test
	public void testCMLDescriptionStringIntegerStringStringListOfArgumentTypeListOfStringReturnType() throws Exception {
		List<ArgumentType> t1;
		List<String> n1;
		ReturnType r1;
		t1 = new Vector<ArgumentType>();
		t1.add(new ArgumentType(CMLConstants.ARG_TYPE_INT));

		n1 = new Vector<String>();
		n1.add("myInteger");		
		r1 = new ReturnType(CMLConstants.RET_TYPE_BYTE_ARRAY);
		
		c1 = new CMLDescription("MethodName1", new Integer(1), "Command1", "Description for command 1", t1, n1, r1);
	}

	/**
	 * Test method for {@link jcu.sal.common.cml.CMLDescription#CMLDescription(java.lang.Integer, java.lang.String, jcu.sal.common.cml.CMLDescription)}.
	 * @throws Exception 
	 */
	@Test
	public void testCMLDescriptionIntegerStringCMLDescription() throws Exception {
		testCMLDescriptionStringIntegerStringStringListOfArgumentTypeListOfStringReturnType();
		c2 = new CMLDescription(new Integer(2), "Command2",c1);
	}

	/**
	 * Test method for {@link jcu.sal.common.cml.CMLDescription#CMLDescription(org.w3c.dom.Document)}.
	 * @throws Exception 
	 */
	@Test
	public void testCMLDescriptionDocument() throws Exception {
		try {
			d1 = XMLhelper.createDocument(cml1);
			d2 = XMLhelper.createDocument(cml2);
			d3 = XMLhelper.createDocument(cml3);
			d4 = XMLhelper.createDocument(cml4);
			d5 = XMLhelper.createDocument(cml5);
			d6 = XMLhelper.createDocument(cml6);
			d7 = XMLhelper.createDocument(cml7);
		} catch (Exception e){
			e.printStackTrace();
			throw e;
		}
		
		c3 = new CMLDescription(d1);
		try {
			c4 = new CMLDescription(d2);
			fail("shouldnt be here");
		} catch (Exception e) {}
		try {
			c4 = new CMLDescription(d3);
			fail("shouldnt be here");
		} catch (Exception e) {}
		try {
			c4 = new CMLDescription(d4);
			fail("shouldnt be here");
		} catch (Exception e) {}
		try {
			c4 = new CMLDescription(d5);
			fail("shouldnt be here");
		} catch (Exception e) {}
		c4 = new CMLDescription(d6);
		c5 = new CMLDescription(d7);
	}

	/**
	 * Test method for {@link jcu.sal.common.cml.CMLDescription#getXML()}.
	 */
	@Test
	public void testGetCML() throws Exception  {
		testCMLDescriptionDocument();

		System.out.println(XMLhelper.toString(c3.getXML()));
		System.out.println(XMLhelper.toString(d1));
		//assertTrue(c3.getCML().equals(d1));
		
		System.out.println(XMLhelper.toString(c4.getXML()));
		System.out.println(XMLhelper.toString(d6));
		//assertTrue(c4.getCML().equals(d6));

		System.out.println(XMLhelper.toString(c5.getXML()));
		System.out.println(XMLhelper.toString(d7));
		//assertTrue(c5.getCML().equals(d7));
	}

	/**
	 * Test method for {@link jcu.sal.common.cml.CMLDescription#getCID()}.
	 */
	@Test
	public void testGetCID() throws Exception  {
		testCMLDescriptionStringIntegerStringStringListOfArgumentTypeListOfStringReturnType();
		testCMLDescriptionIntegerStringCMLDescription();
		testCMLDescriptionDocument();
		assertTrue(c1.getCID().intValue()==1);
		assertTrue(c2.getCID().intValue()==2);
		assertTrue(c3.getCID().intValue()==100);
		assertTrue(c4.getCID().intValue()==100);
		assertTrue(c5.getCID().intValue()==100);
	}

	/**
	 * Test method for {@link jcu.sal.common.cml.CMLDescription#getName()}.
	 */
	@Test
	public void testGetName() throws Exception  {
		testCMLDescriptionStringIntegerStringStringListOfArgumentTypeListOfStringReturnType();
		testCMLDescriptionIntegerStringCMLDescription();
		testCMLDescriptionDocument();
		assertTrue(c1.getName().equals("Command1"));
		assertTrue(c2.getName().equals("Command2"));
		assertTrue(c3.getName().equals("NoArgDoc"));
		assertTrue(c4.getName().equals("OKDoc"));
		assertFalse(c5.getName().equals("NoArgDoc"));
	}

	/**
	 * Test method for {@link jcu.sal.common.cml.CMLDescription#getMethodName()}.
	 */
	@Test
	public void testGetMethodName() throws Exception  {
		testCMLDescriptionStringIntegerStringStringListOfArgumentTypeListOfStringReturnType();
		testCMLDescriptionIntegerStringCMLDescription();
		testCMLDescriptionDocument();
		assertTrue(c1.getMethodName().equals("MethodName1"));
		assertTrue(c2.getMethodName().equals(c1.getMethodName()));
		assertTrue(c3.getMethodName().equals(""));
		assertTrue(c4.getMethodName().equals(""));
		assertTrue(c5.getMethodName().equals(""));
	}

	/**
	 * Test method for {@link jcu.sal.common.cml.CMLDescription#getArgTypes()}.
	 * @throws ConfigurationException 
	 */
	@Test
	public void testGetArgTypes() throws Exception  {
		testCMLDescriptionStringIntegerStringStringListOfArgumentTypeListOfStringReturnType();
		testCMLDescriptionIntegerStringCMLDescription();
		testCMLDescriptionDocument();
		Vector<ArgumentType>args = new Vector<ArgumentType>();
		args.add(new ArgumentType(CMLConstants.ARG_TYPE_INT));
		
		assertTrue(c1.getArgTypes().equals(args));
		assertTrue(c1.getArgTypes().equals(c2.getArgTypes()));
		assertTrue(c3.getArgTypes().size()==0);
		args.removeAllElements();
		args.add(new ArgumentType(CMLConstants.ARG_TYPE_STRING));
		args.add(new ArgumentType(CMLConstants.ARG_TYPE_CALLBACK));
		assertTrue(c4.getArgTypes().equals(args));
		assertTrue(c5.getArgTypes().equals(c1.getArgTypes()));
	}

	/**
	 * Test method for {@link jcu.sal.common.cml.CMLDescription#getArgCount()}.
	 */
	@Test
	public void testGetArgCount() throws Exception  {
		testCMLDescriptionStringIntegerStringStringListOfArgumentTypeListOfStringReturnType();
		testCMLDescriptionIntegerStringCMLDescription();
		testCMLDescriptionDocument();
		assertTrue(c1.getArgCount()==1);
		assertTrue(c2.getArgCount()==1);
		assertTrue(c3.getArgCount()==0);
		assertTrue(c4.getArgCount()==2);
		assertTrue(c5.getArgCount()==1);
	}

	/**
	 * Test method for {@link jcu.sal.common.cml.CMLDescription#getDesc()}.
	 */
	@Test
	public void testGetDesc() throws Exception  {
		testCMLDescriptionStringIntegerStringStringListOfArgumentTypeListOfStringReturnType();
		testCMLDescriptionIntegerStringCMLDescription();
		testCMLDescriptionDocument();
		assertTrue(c1.getDesc().equals("Description for command 1"));
		assertTrue(c2.getDesc().equals(c1.getDesc()));
		assertTrue(c3.getDesc().equals("it works without args too"));
		assertTrue(c4.getDesc().equals(c3.getDesc()));
	}

	/**
	 * Test method for {@link jcu.sal.common.cml.CMLDescription#getArgNames()}.
	 * @throws Exception 
	 */
	@Test
	public void testGetArgNames() throws Exception {
		testCMLDescriptionStringIntegerStringStringListOfArgumentTypeListOfStringReturnType();
		testCMLDescriptionIntegerStringCMLDescription();
		testCMLDescriptionDocument();
		Vector<String>argNames = new Vector<String>();
		argNames.add("myInteger");
		
		assertTrue(c1.getArgNames().equals(argNames));
		assertTrue(c2.getArgNames().equals(c1.getArgNames()));
		argNames.removeAllElements();
		assertTrue(c3.getArgNames().equals(argNames));
		argNames.removeAllElements();
		argNames.add("myString");
		argNames.add("Callback");
		assertTrue(c4.getArgNames().equals(argNames));
		argNames.removeAllElements();
		argNames.add("myInt");
		assertTrue(c5.getArgNames().equals(argNames));
	}

	/**
	 * Test method for {@link jcu.sal.common.cml.CMLDescription#getReturnType()}.
	 * @throws Exception 
	 */
	@Test
	public void testGetReturnType() throws Exception {
		testCMLDescriptionStringIntegerStringStringListOfArgumentTypeListOfStringReturnType();
		testCMLDescriptionIntegerStringCMLDescription();
		testCMLDescriptionDocument();
		assertFalse(c1.getReturnType().equals(new ReturnType(CMLConstants.RET_TYPE_VOID)));
		assertTrue(c1.getReturnType().equals(new ReturnType(CMLConstants.RET_TYPE_BYTE_ARRAY)));
		assertTrue(c2.getReturnType().equals(c1.getReturnType()));
		assertTrue(c3.getReturnType().equals(new ReturnType(CMLConstants.RET_TYPE_VOID)));
		assertTrue(c4.getReturnType().equals(new ReturnType(CMLConstants.RET_TYPE_VOID)));
		assertTrue(c5.getReturnType().equals(new ReturnType(CMLConstants.RET_TYPE_BYTE_ARRAY)));
	}

	/**
	 * Test method for {@link jcu.sal.common.cml.CMLDescription#getArgType(java.lang.String)}.
	 * @throws Exception 
	 */
	@Test
	public void testGetArgType() throws Exception {
		testCMLDescriptionStringIntegerStringStringListOfArgumentTypeListOfStringReturnType();
		testCMLDescriptionIntegerStringCMLDescription();
		testCMLDescriptionDocument();
		assertTrue(c1.getArgType("myInteger").equals(new ArgumentType(CMLConstants.ARG_TYPE_INT)));
		assertFalse(c1.getArgType("myInteger").equals(new ArgumentType(CMLConstants.ARG_TYPE_STRING)));
		assertTrue(c2.getArgType("myInteger").equals(c1.getArgType("myInteger")));
		assertTrue(c4.getArgType("Callback").equals(new ArgumentType(CMLConstants.ARG_TYPE_CALLBACK)));
		assertFalse(c4.getArgType("myString").equals(new ArgumentType(CMLConstants.ARG_TYPE_INT)));
		assertTrue(c5.getArgType("myInt").equals(new ArgumentType(CMLConstants.ARG_TYPE_INT)));
	}

}
