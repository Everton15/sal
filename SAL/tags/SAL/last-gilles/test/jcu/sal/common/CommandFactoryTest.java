//package jcu.sal.common;
//
//import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.fail;
//
//import java.util.Iterator;
//
//import jcu.sal.common.cml.StreamCallback;
//import jcu.sal.common.exceptions.ConfigurationException;
//import jcu.sal.common.exceptions.NotFoundException;
//import jcu.sal.common.exceptions.SALDocumentException;
//import jcu.sal.utils.XMLhelper;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.w3c.dom.Document;
//
//public class CommandFactoryTest implements StreamCallback {
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 1L;
//	private String descStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
//							+"<commandDescriptions>"
//							+"<CommandDescription cid=\"1000\">"
//							+"<Name>GetFreeMem</Name>"
//        					+"<ShortDescription>Reads the amount of free memory</ShortDescription>"
//        					+"<arguments/>"
//        					+"<ReturnType type=\"int\"/>"
//        					+"</CommandDescription>"
//        					+"<CommandDescription cid=\"10\">"
//        					+"<Name>Enable</Name>"
//        					+"<ShortDescription>Enables the sensor</ShortDescription>"
//        					+"<arguments/>"
//        					+"<ReturnType type=\"void\"/>"
//        					+"</CommandDescription>"
//        					+"<CommandDescription cid=\"100\">"
//        					+"<Name>getReading</Name>"
//        					+"<ShortDescription>Reads the amount of free memory</ShortDescription>"
//        					+"<arguments>"
//        					+"<Argument type=\"callback\" name=\"cb\"/>"
//        					+"<Argument type=\"int\" name=\"i\"/>"
//        					+"</arguments>"
//        					+"<ReturnType type=\"int\"/>"
//        					+"</CommandDescription>"
//        					+"<CommandDescription cid=\"101\">"
//        					+"<Name>setMemory</Name>"
//        					+"<ShortDescription>Set the amount of memory</ShortDescription>"
//        					+"<arguments>"
//        					+"<Argument type=\"int\" name=\"memory\"/>"
//        					+"</arguments>"
//        					+"<ReturnType type=\"int\"/>"
//        					+"</CommandDescription>"
//        					+"<CommandDescription cid=\"102\">"
//        					+"<Name>setVariables</Name>"
//        					+"<ShortDescription>Set some variablea</ShortDescription>"
//        					+"<arguments>"
//        					+"<Argument type=\"int\" name=\"varA\"/>"
//        					+"<Argument type=\"float\" name=\"varB\"/>"
//        					+"<Argument type=\"string\" name=\"varC\"/>"
//        					+"</arguments>"
//        					+"<ReturnType type=\"void\"/>"
//        					+"</CommandDescription>"
//        					+"<CommandDescription cid=\"11\">"
//        					+"<Name>Disable</Name>"
//        					+"<ShortDescription>Disables the sensor</ShortDescription>"
//        					//+"<arguments/>"
//        					+"<ReturnType type=\"void\"/>"
//        					+"</CommandDescription>"
//        					+"</commandDescriptions>";
//	private String inst1Str = "<CommandInstance cid=\"102\" />";
//	private String inst2Str = "<CommandInstance cid=\"101\">"
//								+"<arguments>"
//								+"<Argument name=\"bearing\">180</Argument>"
//								+"</arguments>"
//								+"</CommandInstance>";
//	private String inst3Str = "<CommandInstance cid=\"101\">"
//								+"<arguments>"
//								+"<Argument name=\"memory\">180</Argument>"
//								+"</arguments>"
//								+"</CommandInstance>";
//	private String inst4Str = "<CommandInstance cid=\"102\">"
//								+"<arguments>"
//								+"<Argument name=\"varA\">ShouldntWork</Argument>"
//								+"<Argument name=\"varB\">180</Argument>"
//								+"<Argument name=\"varC\">180</Argument>"
//								+"</arguments>"
//								+"</CommandInstance>";
//	private String inst5Str = "<CommandInstance cid=\"102\">"
//								+"<arguments>"
//								+"<Argument name=\"varA\">180</Argument>"
//								+"<Argument name=\"varB\">180.12</Argument>"
//								+"<Argument name=\"varC\">180.12</Argument>"
//								+"</arguments>"
//								+"</CommandInstance>";
//	private String inst6Str = "<CommandInstance cid=\"111\">"
//								+"<arguments />"
//								+"</CommandInstance>";
//	private String inst7Str = "<CommandInstance cid=\"100\">"
//								+"<arguments>"
//								+"<Argument name=\"i\">1</Argument>"
//								+"</arguments>"
//								+"</CommandInstance>";
//	private String inst8Str = "<CommandInstance cid=\"100\">"
//								+"<arguments>"
//								+"<Argument name=\"cb\">testMethod</Argument>"
//								+"<Argument name=\"i\">1</Argument>"
//								+"</arguments>"
//								+"</CommandInstance>";
//	private Document desc, inst1, inst2, inst3, inst4, inst5, inst6,inst7, inst8;
//	CommandFactory cf;
//	
//	@Before
//	public void setUp() throws Exception {
//		desc = XMLhelper.createDocument(descStr);
//		inst1 = XMLhelper.createDocument(inst1Str);
//		inst2 = XMLhelper.createDocument(inst2Str);
//		inst3 = XMLhelper.createDocument(inst3Str);
//		inst4 = XMLhelper.createDocument(inst4Str);
//		inst5 = XMLhelper.createDocument(inst5Str);
//		inst6 = XMLhelper.createDocument(inst6Str);
//		inst7 = XMLhelper.createDocument(inst7Str);
//		inst8 = XMLhelper.createDocument(inst8Str);
//	}
//
//	@After
//	public void tearDown() throws Exception {
//	}
//
//	@Test(expected=ConfigurationException.class)
//	public void testCommandFactoryDocumentDocument() throws ConfigurationException {
//		try {
//			cf = new CommandFactory(desc, inst1);
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//		try {
//			cf = new CommandFactory(desc, inst2);
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//		try {
//			cf = new CommandFactory(desc, inst3);
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//		try {
//			cf = new CommandFactory(desc, inst4);
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//		try {
//			cf = new CommandFactory(desc, inst5);
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//
//		cf = new CommandFactory(desc, inst6);
//		fail("shouldnt be here");
//	}
//
//	@Test(expected=ConfigurationException.class)
//	public void testCommandFactoryDocumentInt() throws SALDocumentException, ConfigurationException, NotFoundException {
//		try {
//			cf = new CommandFactory(desc, 1000);
//		} catch (SALDocumentException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//		try {
//			cf = new CommandFactory(desc, 10);
//		} catch (SALDocumentException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//		try {
//			cf = new CommandFactory(desc, 100);
//		} catch (SALDocumentException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//		try {
//			cf = new CommandFactory(desc, 101);
//		} catch (SALDocumentException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//		try {
//			cf = new CommandFactory(desc, 102);
//		} catch (SALDocumentException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//
//		cf = new CommandFactory(desc, 111);
//		fail("shouldnt be here");
//	}
//
//	@Test
//	public void testGetArgType() throws ConfigurationException {
//		cf = new CommandFactory(desc, inst1);
//		assertTrue(cf.getArgType("varA").getArgType().equals("int"));
//		assertTrue(cf.getArgType("varB").getArgType().equals("float"));
//		assertTrue(cf.getArgType("varC").getArgType().equals("string"));
//		cf = new CommandFactory(desc, inst7);
//		assertTrue(cf.getArgType("cb").getArgType().equals("callback"));
//	}
//
//	@Test
//	public void testListMissingArgNames() throws ConfigurationException {
//		int i=0;
//		String n;
//		try {
//			cf = new CommandFactory(desc, inst1);
//			Iterator<String> e = cf.listMissingArgNames().iterator();
//			while(e.hasNext()){
//				n=e.next();
//				assertTrue(n.equals("varA") || n.equals("varB") || n.equals("varC"));  
//				i++;
//			}
//			assertTrue(i==3);
//			
//			cf = new CommandFactory(desc, inst4);
//			i=0;
//			e = cf.listMissingArgNames().iterator();
//			while(e.hasNext()){
//				n=e.next();
//				assertTrue(n.equals("varA"));  
//				i++;
//			}
//			assertTrue(i==1);
//			
//			cf = new CommandFactory(desc, inst7);
//			i=0;
//			e = cf.listMissingArgNames().iterator();
//			while(e.hasNext()){
//				n=e.next();
//				assertTrue(n.equals("cb"));  
//				i++;
//			}
//			assertTrue(i==1);
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//
//		cf = new CommandFactory(desc, inst8);
//	}
//
//	@Test(expected=ConfigurationException.class)
//	public void testAddArgumentValueFloat() throws ConfigurationException {
//		try {
//			cf = new CommandFactory(desc, inst1);
//			cf.addArgumentValueFloat("varB", (float) 100.25);
//			cf.addArgumentValueFloat("varB", (float) 1);
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//		cf.addArgumentValueFloat("varA", (float) 1);
//	}
//
//	@Test(expected=ConfigurationException.class)
//	public void testAddArgumentValueInt() throws ConfigurationException {
//		try {
//			cf = new CommandFactory(desc, inst1);
//			cf.addArgumentValueInt("varA", 1);
//			cf.addArgumentValueInt("varA", 2);
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//		cf.addArgumentValueInt("varB", 1);
//	}
//
//	@Test(expected=ConfigurationException.class)
//	public void testAddArgumentValueString() throws ConfigurationException {
//		try {
//			cf = new CommandFactory(desc, inst1);
//			cf.addArgumentValueString("varC", "1");
//			cf.addArgumentValueString("varC", "2");
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//		cf.addArgumentValueString("varA", "2");
//	}
//
//	@Test(expected=ConfigurationException.class)
//	public void testAddArgumentValue() throws ConfigurationException{
//		boolean ok=false;
//		try {
//			cf = new CommandFactory(desc, inst1);
//			cf.addArgumentValue("varC", "1");
//			cf.addArgumentValue("varB", "2.556");
//			cf.addArgumentValue("varA", "45");
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//		try {
//			cf.addArgumentValueString("varA", "2.456");
//		} catch (ConfigurationException e) {
//			ok=true;
//		}
//		assertTrue(ok==true);
//		
//		try {
//			cf = new CommandFactory(desc, inst7);
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//		cf.addArgumentValue("cb", "1");
//		
//	}
//
//	@Test(expected=ConfigurationException.class)
//	public void testAddArgumentValueCallback() throws ConfigurationException {
//		try {
//			cf = new CommandFactory(desc, inst7);
//			cf.addArgumentCallback("cb", this);
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//		cf.addArgumentCallback("cb", null);
//	}
//
//	@Test
//	public void testGetCommand() {
//		boolean ok=false;
//		try {
//			cf = new CommandFactory(desc, inst5);
//			cf.getCommand();
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//		
//		try {
//			cf = new CommandFactory(desc, inst4);
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//		
//		try {
//			cf.getCommand();
//		} catch (ConfigurationException e) {
//			ok=true;
//		}
//		assertTrue(ok==true);
//		
//		try {
//			cf.addArgumentValue("varA", "123");
//			cf.getCommand();
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//		
//		try {
//			cf = new CommandFactory(desc, inst7);
//			cf.addArgumentCallback("cb", this);
//			cf.getCommand();
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//		
//		try {
//			cf = new CommandFactory(desc, inst8);
//			cf.addArgumentCallback("cb", this);
//			cf.getCommand();
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			fail("shouldnt be here");
//		}
//		
//	}
//
//	public void collect(Response r) {
//		// TODO Auto-generated method stub
//		
//	}
//
//}
