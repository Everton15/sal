package jcu.sal.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;
import jcu.sal.common.exceptions.XpathException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;


	/**
	 * Set of methods to manipulate XML documents
	 * @author gilles
	 * 
	 */

public class XMLhelper {
	
	private static XPath xpath = XPathFactory.newInstance().newXPath();
	private static DocumentBuilder builder;
	static {
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println("Cant create the document builder, SAL will not work");
			e.printStackTrace();
			throw new SALDocumentException("Cant create the document builder, SAL will not work", e);
		}
	}
	
	/**
     * Creates an empty DOM document
     * @return the empty DOM document
     */
    public synchronized static Document createEmptyDocument(){
		return builder.newDocument();        
    }
    
    /**
     * Creates a DOM document from an Input Stream
     * @param in the input stream
     * @return the DOM document
     * @throws SALDocumentException if the given stream isnt a valid XML document
     */
    public synchronized static Document createDocument(InputStream in) throws SALDocumentException {
    	try {
	        return builder.parse(in);
    	} catch (Exception e) {
    		throw new SALDocumentException("Cant parse the input stream to an XML document");
    	}
    }

    /**
     * Creates a DOM document from a file
     * @param f the file
     * @return the DOM document
     * @throws SALDocumentException if the file cant be parsed to a valid XML document
     */
    public synchronized static Document createDocument(File f) throws SALDocumentException{
    	try {
			return createDocument(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			throw new SALDocumentException("Cant parse file contents (file: '"+f.getName()+"')",e);
		}
    }
    
    /**
     * Creates a DOM document from an XML string
     * @param xmlstr the XML string
     * @return the DOM document
     * @throws SALDocumentException If the string doesnt parse to a valid XML document
     */   
    public synchronized static Document createDocument(String xmlstr) throws SALDocumentException {
    	try {
	        return builder.parse(new InputSource(new StringReader(xmlstr)));
    	} catch (Exception e) {
    		throw new SALDocumentException("The string cant be parsed to a valid XMl document"	);
    	}
    }
    
    /**
     * Creates a DOM document from a Node
     * @param node the XML node
     * @return the DOM document 
     * @throws SALDocumentException if the document cant be created
     */   
    public synchronized static Document createDocument(Node node) {
    	Document d = XMLhelper.createEmptyDocument();
    	try { XMLhelper.transform(new DOMSource(node), new DOMResult(d)); }
    	catch (Exception e) {
    		System.err.println("Cant create a document from the node !!!!");
    		e.printStackTrace();
    		throw new SALDocumentException("Can not create a document from the node", e);
    	}
        return d;
    }
    
    /**
     * Duplicates a node and put it in its own document
     * @param node the XML node to be duplicated
     * @return the node (in its new document)
     */   
    public static Node duplicateNode(Node node){
    	Document d = createEmptyDocument();
    	return d.appendChild(d.importNode(node,true));
    }

    /**
     * Adds a child to the parent node in the DOM document
     * @param parent the node for which to add the child
     * @param child the node to be added
     * @param doc the DOM document
     * @return the new added element
     */
    public static Element addChild(Node parent, String child, Document doc) {
        return addChild(parent, child, null, doc);
    }

    /**
     * Adds a child containing text to the parent node inside the DOM document
     * @param parent the node for which to add the child
     * @param child the node to be added
     * @param value the text node to add to the child node
     * @param doc the DOM document
     * @return the new added element
     */
    public static Element addChild(Node parent, String child, String value, Document doc) {
        Element childElement = doc.createElement(child);
        
        if (value == null) {
            parent.appendChild(childElement);
        } else {
            parent.appendChild(childElement);
            addTextChild(childElement, value, doc);
        }

        doc.getDocumentElement().normalize();

        return childElement;
    }
    
    /**
     * Adds a child node to a parent node. The child node is given as a string, imported in the parent's document as a node,
     * and appended to the parent node 
     * @param parent the parent node
     * @param xml the child node
     * @return the new appended node 
     * @throws SALDocumentException if the child node (the xml string) can not be parsed
     */
    public static Node addChild(Node parent, String xml) throws SALDocumentException{
		Document d = XMLhelper.createDocument(xml);
		return addChild(parent, d.getFirstChild());
    }
    
    /**
     * Adds a child containing text to the parent node inside the DOM document
     * @param parent the node under which to add the child
     * @param child the node to be added
     * @return the new appended node
     */
    public static Node addChild(Node parent, Node child) {
    	Document d = parent.getOwnerDocument();
    	if(child.getNodeType()==Node.DOCUMENT_NODE) child=child.getFirstChild();
    	Node n = d.importNode(child, true);
        return parent.appendChild(n);
    }
    
    /**
     * Adds an attribute to a node
     * @param parent the node to which the attribue will be attached
     * @param name the name of the attribute
     * @param value the value of the attribute
     * @return the new added element
     */
    public static void addAttribute(Node parent, String name, String value) {
    	Attr attrib = parent.getOwnerDocument().createAttribute(name);
    	attrib.setValue(value);
    	parent.getAttributes().setNamedItem(attrib);
    }

    /**
     * Adds text child to the parent node
     * @param parent the node for which to add the text node
     * @param value the next node to add to parent node
     * @param doc the DOM document
     */
    public static void addTextChild(Node parent, String value, Document doc) {
        Node textNode = doc.createTextNode(value);
        parent.appendChild(textNode);
    }
    
    /**
     * Removes a node from its document
     * @param n the node to be removed
     */
    public static void deleteNode(Node n) {
    	n.getParentNode().removeChild(n);
    }
    
    /**
     * This method takes an XPATH query and either a document or a node and returns a node matching the query
     * @param xp the xpath query
     * @param o the document or node
     * @return a node matching the query
     * @throws XpathException if an xpath exception is caught
     */
    private static Node getNode(String xp, Object o){
        try {
			return (Node) xpath.evaluate(xp, o, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			System.err.println("XPATH expression: "+ xpath);
			e.printStackTrace();
			throw new XpathException(e);
		}
    }

    /**
     * Returns the node corresponding to the given xpath expression.
     * If more than one node matches the XPATH expression, only the first one is returned.
     * @param xpath_expression the XPATH expression
     * @param doc the DOM document
     * @param newdoc if set to true, the resulting node will be duplicated and put in its own document
     * @return the node corresponding to the given xpath expression
     * @throws NotFoundException if nothing matches the XPATH query
     * @throws XpathException if an XPATH exception is caught
     */
    public static Node getNode(String xpath_expression, Document doc, boolean newdoc) throws NotFoundException {
        return getNode(xpath_expression, (Node) doc, newdoc);
    }
    
    /**
     * Returns the node corresponding to the given xpath expression
     * @param xpath_expression the XPATH expression
     * @param n the node to search
     * @param newdoc if set to true, the resulting node will be duplicated and put in its own document
     * @return the node corresponding to the given xpath expression
     * @throws NotFoundException if nothing matches the XPATH expression
     * @throws XpathException if an XPATH exception is caught
     */
    public static Node getNode(String xpath_expression, Node n, boolean newdoc) throws NotFoundException {
        Node node = getNode(xpath_expression, n);

        if(node==null)
        	throw new NotFoundException("Cant find matching nodes for XPATH expression '"+xpath_expression+"'");
        
        if(newdoc && node!=null)
        	node = duplicateNode(node);
        return node;
    }
    
    /**
     * Returns the combined attributes of multiple elements retrieved from an XPATH query
     * @param xpath_expression the XPATH expression
     * @param doc the DOM document
     * @return the attributes and their values in a ArrayList: 0:1st Attr name, 1:1st attr value, 2:2nd attr name,3:2nd attr value...
     * @throws NotFoundException if nothing matches the given xpath expression
     * @throws XpathException if an XPATH exception is caught
     */
    public static ArrayList<String> getAttributeListFromElements(String xpath_expression, Document doc) throws NotFoundException{
    	ArrayList<String> table = new ArrayList<String>();
    	
    	NodeList list = getNodeList(xpath_expression, doc);
    	if(list!=null) {
			for(int i = 0; i < list.getLength(); i++) {
				NamedNodeMap nnp = list.item(i).getAttributes();
				for (int j = 0; j < nnp.getLength(); j++) { 
					table.add(nnp.item(j).getNodeName());
					table.add(nnp.item(j).getNodeValue());
				}
			}
    	}
		return table;	
    }
    
    /**
     * Returns the combined attributes of multiple elements retrieved from an XPATH query
     * @param xpath_expression the XPATH expression
     * @param n the node to be searched
     * @return the attributes and their values in a ArrayList: 0:1st Attr name, 1:1st attr value, 2:2nd attr name,3:2nd attr value...
     * @throws NotFoundException if nothing matches the given xpath expression
     * @throws XpathException if an XPATH exception is caught 
     */
    public static List<String> getAttributeListFromElements(String xpath_expression, Node n) throws NotFoundException {
    	ArrayList<String> table = new ArrayList<String>();
    	
    	NodeList list = getNodeList(xpath_expression, n);
    	if(list!=null) {
			for(int i = 0; i < list.getLength(); i++) {
				NamedNodeMap nnp = list.item(i).getAttributes();
				for (int j = 0; j < nnp.getLength(); j++) { 
					table.add(nnp.item(j).getNodeName());
					table.add(nnp.item(j).getNodeValue());
				}
			}
    	}
		return table;	
    }    
    
    /**
     * Returns the attributes of a single element retrieved from an XPATH query
     * @param xpath_expression the XPATH expression
     * @param doc the DOM document
     * @return the attributes and their values in a ArrayList: 0:1st Attr name, 1:1st attr value, 2:2nd attr name,3:2nd attr value...   
     * @throws NotFoundException if nothing matches the given xpath expression
     * @throws XpathException if an XPATH exception is caught 
     */
    public static List<String> getAttributeListFromElement(String xpath_expression, Document doc) throws NotFoundException{
    	ArrayList<String> table = new ArrayList<String>();
    	
		Node node = getNode(xpath_expression, doc, false);
    	if(node!=null) {
			NamedNodeMap nnp = node.getAttributes();
			for (int i = 0; i < nnp.getLength(); i++) {
				table.add(nnp.item(i).getNodeName());
				table.add(nnp.item(i).getNodeValue());
			}
    	}
		return table;	
    }
    
    /**
     * Returns the attributes of a single element retrieved from an XPATH query
     * @param xpath_expression the XPATH expression
     * @param n the node to be search
     * @return the attributes and their values in a ArrayList: 0:1st Attr name, 1:1st attr value, 2:2nd attr name,3:2nd attr value...
     * @throws NotFoundException if nothing matches the given xpath expression
     * @throws XpathException if an XPATH exception is caught   
     */
    public static ArrayList<String> getAttributeListFromElement(String xpath_expression, Node n) throws NotFoundException {
    	ArrayList<String> table = new ArrayList<String>();
    	
		Node node = getNode(xpath_expression, n, false);
    	if(node!=null) {
			NamedNodeMap nnp = node.getAttributes();
			for (int i = 0; i < nnp.getLength(); i++) {
				table.add(nnp.item(i).getNodeName());
				table.add(nnp.item(i).getNodeValue());
			}
    	}
		return table;	
    }
    
    /**
     * Returns an element's attribute using an XPATH query and the attribute's name 
     * @param xpath_expression the XPATH expression
     * @param attr_name the name of the attribute whose value is to be returned
     * @param doc the DOM document
     * @return the value of the attribute
     * @throws XpathException if an XPATH exception is caught
     * @throws NotFoundException if nothing matches the given xpath expression
     */
    public static String getAttributeFromName(String xpath_expression, String attr_name, Document doc) throws NotFoundException, XpathException {
    	Node n;
    	NamedNodeMap nm;
    	if((nm=getNode(xpath_expression, doc, false).getAttributes())!=null)
    		if((n=nm.getNamedItem(attr_name))!=null)
    			return n.getNodeValue();
    	throw new NotFoundException("Cannot find attribute "+attr_name);
    }
    
    /**
     * Returns an element's attribute using an XPATH query and the attribute's name 
     * @param xpath_expression the XPATH expression
     * @param attr_name the name of the attribute whose value is to be returned
     * @param n the node
     * @return the value of the attribute
     * @throws XPathException if an xpath expression is caught 
     * @throws NotFoundException if nothing matches the given xpath expression
     */
    public static String getAttributeFromName(String xpath_expression, String attr_name, Node n) throws XPathExpressionException, DOMException, ParserConfigurationException, NotFoundException {
    	Node node;
    	NamedNodeMap nm;
   		if((nm=getNode(xpath_expression, n, false).getAttributes())!=null)
   			if((node=nm.getNamedItem(attr_name))!=null)
   				return node.getNodeValue();
    	throw new NotFoundException("Cannot find attribute "+attr_name);
    }
    
    /**
     * Set an element's existing attribute using an XPATH query and the attribute's name 
     * @param xpath_expression the XPATH expression
     * @param attr_name the name of the attribute whose value is to be set
     * @param val the value to be set for this attribute
     * @param n the node
     * @return the value of the attribute
     * @throws NotFoundException if nothing matches the given xpath expression
     * @throws XPathException if an xpath exceptino is caught
     */
    public static void setAttributeFromName(String xpath_expression, String attr_name, String val, Node n) throws NotFoundException {
    	Node node;
    	NamedNodeMap nm;
   		if((nm=getNode(xpath_expression, n, false).getAttributes())!=null)
   			if((node=nm.getNamedItem(attr_name))!=null) {
   				node.setNodeValue(val);
   				return;
   			}
    	throw new NotFoundException("Cannot find attribute "+attr_name);
    }
    
    /**
     * Returns the node set corresponding to the given xpath expression. If nothing matches the given
     * xpath expression, a <code>NotFoundException</code> is thrown.
     * @param xpath_expression the XPATH expression
     * @param n the node to be searched
     * @return the node set corresponding to the given xpath expression
     * @throws NotFoundException if nothing matches the given xpath expression
     * @throws XpathException if an XPATH exception is caught
     */
    public static NodeList getNodeList(String xpath_expression, Node n) throws NotFoundException {
        NodeList nodelist = null;
        
        try {
			nodelist = (NodeList) xpath.evaluate(xpath_expression, n, XPathConstants.NODESET);
			if(nodelist==null)
				throw new NotFoundException("Nothing matches xpath query '"+xpath_expression+"'");
		} catch (XPathExpressionException e) {
			System.err.println("Xpath expression: "+xpath_expression);
			System.err.println("Node: "+toString(n));
			e.printStackTrace();
			throw new XpathException(e);
		}

        return nodelist;
    }
    
    /**
     * Returns the node set corresponding to the given xpath expression. If no nodes match the given
     * xpath expression, a <code>NotFoundException</code> is thrown.
     * @param xpath_expression the XPATH expression
     * @param doc the DOM document
     * @return the node set corresponding to the given xpath expression
     * @throws NotFoundException if nothing matches the given xpath expression
     * @throws XpathException if an XPATH exception is caught
     */
    public static NodeList getNodeList(String xpath_expression, Document doc) throws NotFoundException {
        return getNodeList(xpath_expression, (Node) doc);
    }

    /**
     * Returns the value of the node corresponding to the given xpath expression
     * @param xpath_expression the XPATH expression
     * @param doc the document
     * @return the value of the node corresponding to the given xpath expression or null if there is no
     * such node.
     * @throws NotFoundException if nothing matches the given xpath expression
     * @throws XPathException if an xpath exception is caught 
     */
    public static String getTextValue(String xpath_expression, Document doc) throws NotFoundException {
        try {
			if(evaluate(xpath_expression, doc)==true)
				return (String) xpath.evaluate(xpath_expression, doc, XPathConstants.STRING);
			else
				throw new NotFoundException("The xpath expression doesnt resolve to anything");
		} catch (XPathExpressionException e) {
			System.err.println("Xpath expression: "+xpath_expression);
			System.err.println("Document: "+toString(doc));
			e.printStackTrace();
			throw new XpathException(e);
		}
    }

    /**
     * Evaluates the given xpath expression.
     * Returns true if the given xpath expression can be found on this document. False if not.
     * This method is mainly used when we don't know if an element is present in the document.
     * @param xpath_expression the expression to evaluate
     * @param doc the DOM document
     * @return true if the exression can be evaluated, false if not.
     * @throws XPathExpressionException 
     */
    private static boolean evaluate(String xpath_expression, Document doc) 
    	throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        String result = (String) xpath.evaluate(xpath_expression, doc, XPathConstants.STRING);

        if (result.length() == 0)
        	return false;

        return true;
    }

    /**
     * @see java.lang.Object#toString()
     * @param doc the DOM document
     * @throws TransformerFactoryConfigurationError 
     * @Returns a String representation of the DOM
     */
    public static String toString(Document doc) {
    	if(doc==null) return "";
    	
    	StringWriter writer = new StringWriter();
    	OutputFormat format = new OutputFormat(doc);
        format.setLineWidth(65);
        format.setIndenting(true);
        format.setIndent(4);
        XMLSerializer serializer = new XMLSerializer(writer, format);
        try {
			serializer.serialize(doc);
		} catch (IOException e) {
			System.out.println("Cant toString() this DOM doc !");;
			e.printStackTrace();
		}
        return writer.getBuffer().toString();
    }
    
    /**
     * @see java.lang.Object#toString()
     * @param n the node
     * @throws TransformerFactoryConfigurationError 
     * @Returns a String representation of the DOM
     */
    public static String toString(Node n) {
		return toString(createDocument(n));
    }
    
    /**
     * This method takes an XML document as a string and formats it nicely
     * @param n the XML string
     * @Returns a nicely formatted XML document as a string
     * @throws SALDocumentException if the given string cant be parsed to an XML document
     */
    public static String toString(String n) throws SALDocumentException {
    	return toString(createDocument(n));
    }

    /**
     * This method is used to generate a result from a source.
     * @param source
     * @param result
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    public static void transform(Source source, Result result)
        throws TransformerFactoryConfigurationError, TransformerException {
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);
    }
}

