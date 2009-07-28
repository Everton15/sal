package jcu.sal.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.utils.XMLhelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class XMLhelperTest {
	private Document d;
									

	@Before
	public void setUp() throws Exception {
		d = XMLhelper.createDocument(new File("test/platformConfig.xml"));
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testDuplicateDocument() {
		fail("Not yet implemented");
	}

	@Test
	public void testDuplicateNode() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddChildNodeStringDocument() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddChildNodeStringStringDocument() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddChildNodeString() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddChildNodeNode() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddAttribute() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddTextChild() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteNode() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRootElement() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNodeStringDocumentBoolean() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNodeStringNodeBoolean() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSubDocument() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeListFromElementsStringDocument() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeListFromElementsStringNode() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeListFromElementStringDocument() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeListFromElementStringNode() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeFromNameStringNode() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeFromNameStringStringDocument() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeFromNameStringStringNode() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetAttributeFromName() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNodeListStringNode() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNodeListStringDocument() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTextValueStringDocument() throws NotFoundException {
		assertEquals(XMLhelper.getTextValue("count(//AbstractProtocol)", d), "5");
		assertEquals(XMLhelper.getTextValue("//Argument[@name=\"bearing\"]", d), "180");
	}

	@Test
	public void testGetTextValueStringNode() {
		fail("Not yet implemented");
	}

	@Test
	public void testEvaluate() {
		fail("Not yet implemented");
	}

	@Test
	public void testToStringDocument() {
		fail("Not yet implemented");
	}

	@Test
	public void testToStringNode() {
		fail("Not yet implemented");
	}

	@Test
	public void testTransform() {
		fail("Not yet implemented");
	}

}
