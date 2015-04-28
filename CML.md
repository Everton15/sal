This page present the Command Markup Language used by SAL to report on sensor hardware capabilities.

Capabilties are reported in terms of commands. A command bears a description, a unique identifier, called CID, a listing of arguments, a return type and possibly, any constraints imposed on allowed sampling frequencies.

# CML Description #
A CML description document is an XML document which describes a single command and is enclosed in a `CommandDescription` tag.

The `name`, `ShortDescription` and `LongDescription` are self-explanatory. The `Streaming` tag provides information on allowed sampling frequencies. Possible values must be within the minimum and maximum and must be a multiple of the step value if provided. The `continuous` attribute specifies whether or not the sensor support continuous streaming. Continuous streaming refers to a data stream where data is sent "when there is a change from the previous transmitted value". Not all sensor support this feature.


Example of a CML description for a single command:
```
<CommandDescription cid="1000" xmlns="http://sal.jcu.edu.au/schemas/CML">
    <Name>GetJPEGFrame</Name>
    <ShortDescription>Fetches a single JPEG-encoded frame</ShortDescription>
    <LongDescription/>
    <Streaming>
        <bounds continuous="true">
            <min>50</min>
            <max>10000</max>
            <step>0</step>
        </bounds>
    </Streaming>
    <Response contentType="image/jpeg" type="byte array" unit="none"/>
    <arguments>
        <Argument name="width" optional="false" type="int"/>
        <Argument name="height" optional="false" type="int"/>
        <Argument defaultValue="0" name="channel" optional="true" type="list">
            <list>
                <item id="0">Camera 1 - Camera</item>
            </list>
        </Argument>
        <Argument defaultValue="0" name="standard" optional="true" type="list">
            <list>
                <item id="3">NTSC</item>
                <item id="2">SECAM</item>
                <item id="1">PAL</item>
                <item id="0">Webcam</item>
            </list>
        </Argument>
        <Argument defaultValue="2" name="format" optional="true" type="list">
            <list>
                <item id="2">RGB24</item>
                <item id="1">YUYV</item>
                <item id="0">YUV420</item>
                <item id="19">BGR24</item>
                <item id="7">MJPEG</item>
            </list>
        </Argument>
        <Argument defaultValue="80" name="quality" optional="true" type="int">
            <bounds>
                <min>0.0</min>
                <max>100.0</max>
                <step>1.0</step>
            </bounds>
        </Argument>
    </arguments>
</CommandDescription>
```

Example of CML descriptions showing all commands command supported by a single sensor :
```
<?xml version="1.0" encoding="UTF-8"?>
<commandDescriptions xmlns="http://sal.jcu.edu.au/schemas/CML">
    <CommandDescription cid="1000">
        <Name>GetLoadAvg1</Name>
        <ShortDescription>Reads the 1-minute load average</ShortDescription>
        <LongDescription/>
        <Streaming>
            <bounds continuous="false">
                <min>500</min>
                <max>10000</max>
                <step>0</step>
            </bounds>
        </Streaming>
        <Response contentType="text/plain" type="float" unit="none"/>
        <arguments/>
    </CommandDescription>
    <CommandDescription cid="10">
        <Name>enable</Name>
        <ShortDescription>Enables the sensor</ShortDescription>
        <LongDescription/>
        <Response contentType="text/plain" type="void" unit="none"/>
        <arguments/>
    </CommandDescription>
    <CommandDescription cid="100">
        <Name>getReading</Name>
        <ShortDescription>Reads the 1-minute load average</ShortDescription>
        <LongDescription/>
        <Streaming>
            <bounds continuous="false">
                <min>500</min>
                <max>10000</max>
                <step>0</step>
            </bounds>
        </Streaming>
        <Response contentType="text/plain" type="float" unit="none"/>
        <arguments/>
    </CommandDescription>
    <CommandDescription cid="11">
        <Name>disable</Name>
        <ShortDescription>Disables the sensor</ShortDescription>
        <LongDescription/>
        <Response contentType="text/plain" type="void" unit="none"/>
        <arguments/>
    </CommandDescription>
</commandDescriptions>
```

# CML schema #

```
<xsd:schema targetNamespace="http://sal.jcu.edu.au/schemas/CML"
        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
        xmlns:tns="http://sal.jcu.edu.au/schemas/CML"
        elementFormDefault="qualified" attributeFormDefault="unqualified"
        xmlns:jaxb="http://java.sun.com/xml/ns/jaxb"
        xmlns:xjc="http://java.sun.com/xml/ns/jaxb/xjc">
        
        <xsd:element name="commandDescriptions">
        	<xsd:complexType>
        		<xsd:sequence>
        			<xsd:element name="CommandDescription" type="tns:CommandDescription" minOccurs="1" maxOccurs="unbounded" />
        		</xsd:sequence>
        	</xsd:complexType>
        </xsd:element>
        
        <xsd:complexType name="CommandDescription">
        	<xsd:all>
        		<xsd:element name="Name" type="xsd:string" minOccurs="1" maxOccurs="1" />
	        	<xsd:element name="ShortDescription" type="xsd:string" minOccurs="1" maxOccurs="1" />
	        	<xsd:element name="LongDescription" type="xsd:string" minOccurs="0" maxOccurs="1" />
	        	<xsd:element name="Streaming" minOccurs="0" maxOccurs="1">
	        		<xsd:complexType>
	        			<xsd:all>
	        				<xsd:element name="bounds">
			        			<xsd:complexType>
									<xsd:all>
										<xsd:element name="min" type="xsd:int" />
										<xsd:element name="max" type="xsd:int" />
										<xsd:element name="step" type="xsd:int" minOccurs="0" />
									</xsd:all>
									<xsd:attribute name="continuous" type="xsd:boolean" use="required"/>
								</xsd:complexType>
			        		</xsd:element>
	        			</xsd:all>
	        		</xsd:complexType>
	        	</xsd:element>
	        	<xsd:element name="Response" type="tns:Response" minOccurs="1" maxOccurs="1" />
       			<xsd:element name="arguments" minOccurs="1" maxOccurs="1">
       				<xsd:complexType>
       					<xsd:sequence>
       						<xsd:element name="Argument" type="tns:Argument" minOccurs="0" maxOccurs="unbounded"/>
       					</xsd:sequence>
       				</xsd:complexType>
       			</xsd:element>
        	</xsd:all>
        	<xsd:attribute name="cid" type="xsd:string" use="required"/>
        </xsd:complexType>
        
		<xsd:complexType name="Response">
    	    <xsd:attribute name="type" type="xsd:string" use="required"/>
   			<xsd:attribute name="contentType" type="xsd:string" use="optional"/>
   			<xsd:attribute name="unit" type="xsd:string" use="optional"/>
   		</xsd:complexType>      
       
        <xsd:complexType name="Argument">
        	<!-- 
        		Choice of list, bound or nothing
        		if a new element is added, update the "type" attribute too
        		(right at the end of the choice)
        	 -->
        	<xsd:choice minOccurs="0">
        		<xsd:element name="list">
        			<xsd:complexType>
			        	<xsd:sequence minOccurs="1" maxOccurs="unbounded">
			        		<xsd:element name="item">
			        			<xsd:complexType>
			        				<xsd:simpleContent>
			        				<xsd:extension base="xsd:string">
			        					<xsd:attribute name="id" type="xsd:string"/>
			        				</xsd:extension>
			        				</xsd:simpleContent>
			        			</xsd:complexType>
			        		</xsd:element>
			        	</xsd:sequence>
		        	</xsd:complexType>
        		</xsd:element>
        		<xsd:element name="bounds">
        			<xsd:complexType>
						<xsd:all>
							<xsd:element name="min" type="xsd:string" />
							<xsd:element name="max" type="xsd:string" />
							<xsd:element name="step" type="xsd:string" minOccurs="0" />
						</xsd:all>
					</xsd:complexType>
        		</xsd:element>
        	</xsd:choice>
        	<!--
        		Update the following attribute to support more argument types 
        	 -->
        	<xsd:attribute name="type" use="required">
        		<xsd:simpleType>
					<xsd:restriction base="xsd:string">
						<xsd:enumeration value="int"/>
						<xsd:enumeration value="float"/>
						<xsd:enumeration value="string"/>
						<xsd:enumeration value="list"/>
						<xsd:enumeration value="button"/>
					</xsd:restriction>
				</xsd:simpleType>
        	</xsd:attribute>
        	<xsd:attribute name="name" type="xsd:string" use="required"/>
        	<xsd:attribute name="optional" type="xsd:boolean" use="optional" default="false"/>
        	<xsd:attribute name="defaultValue" type="xsd:string" use="optional"/>
        </xsd:complexType>
        
        
        
</xsd:schema>
```