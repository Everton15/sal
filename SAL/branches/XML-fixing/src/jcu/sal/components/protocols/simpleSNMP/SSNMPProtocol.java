package jcu.sal.components.protocols.simpleSNMP;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.management.BadAttributeValueExpException;
import javax.naming.ConfigurationException;

import jcu.sal.common.CommandFactory.Command;
import jcu.sal.common.pcml.ProtocolConfiguration;
import jcu.sal.components.EndPoints.EthernetEndPoint;
import jcu.sal.components.protocols.AbstractProtocol;
import jcu.sal.components.protocols.ProtocolID;
import jcu.sal.components.sensors.Sensor;
import jcu.sal.utils.Slog;

import org.apache.log4j.Logger;

import uk.co.westhawk.snmp.pdu.BlockPdu;
import uk.co.westhawk.snmp.stack.AgentException;
import uk.co.westhawk.snmp.stack.PduException;
import uk.co.westhawk.snmp.stack.SnmpContext;
import uk.co.westhawk.snmp.stack.varbind;

public class SSNMPProtocol extends AbstractProtocol{
	/**
	 * The string used in PCML docs to represent this protocol 
	 */
	public static final String PROTOCOL_TYPE = "SSNMP";
	private static Logger logger = Logger.getLogger(SSNMPProtocol.class);
	static {Slog.setupLogger(logger);}
	private static String OID_START = "1.3";
	//Maximum number of OIDs to be detected automatically  
	private static int MAX_AUTODETECTED_OIDS = 1000;

	private String agent;
	private String comm_string;
	private int timeout;
	private SnmpContext s ;

	/**
	 * Construct the SSNMPProtocol object
	 * @throws ConfigurationException 
	 */
	public SSNMPProtocol(ProtocolID i, ProtocolConfiguration c) throws ConfigurationException {
		super(i,PROTOCOL_TYPE,c);		

//		Add to the list of supported EndPoint IDs
		supportedEndPointTypes.add(EthernetEndPoint.ETHERNETENDPOINT_TYPE);
	}

	
	/* (non-Javadoc)
	 * @see jcu.sal.components.Protocol#internal_parseConfig()
	 */
	@Override
	protected void internal_parseConfig() throws ConfigurationException {
		try {
			agent = getParameter("AgentIP");
			comm_string = getParameter("CommunityString");
		} catch (BadAttributeValueExpException e) {
			logger.error("Cant find 'AgentIP' / 'CommunityString' in AbstractProtocol config.");
			throw new ConfigurationException("SSNMP 'AgentIP' / 'CommunityString' config directives missing");
		}

		try { timeout = Integer.parseInt(getParameter("Timeout")); }
		catch (BadAttributeValueExpException e) { timeout=1500;}
		try { autoDetectionInterval = (getParameter("AutodetectOIDs").equals("1") || getParameter("AutodetectOIDs").equalsIgnoreCase("true")) ? -1 : 0;}
		catch (BadAttributeValueExpException e) {autoDetectionInterval=-1;}
		
		cmls = CMLDescriptionStore.getStore();
		logger.debug("SimpleSNMP protocol configured");
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.Protocol#internal_stop()
	 */
	@Override
	protected void internal_stop() {
		s.destroy();
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.AbstractProtocol#internal_start()
	 */
	@Override
	protected void internal_start() throws ConfigurationException{
		try {
			s = new SnmpContext(agent, 161);
			s.setCommunity(comm_string);
		} catch (IOException e) {
			logger.error("Cant create SNMP context");
			throw new ConfigurationException();
		}
	}

	/* (non-Javadoc)
	 * @see jcu.sal.components.Protocol#internal_remove()
	 */
	@Override
	protected void internal_remove() {
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_isSensorSupported(jcu.sal.components.sensors.Sensor)
	 */
	@Override
	protected boolean internal_isSensorSupported(Sensor sensor){
		//TODO improve me... maybe check that the OID exist and is valid
		//TODO with a GET PDU... only problem: it requires the Agent to
		//TODO be accessible...
		return true;	
	}

	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_probeSensor(jcu.sal.components.sensors.Sensor)
	 */
	@Override
	protected boolean internal_probeSensor(Sensor s) {
		try {
			getRawReading(s.getNativeAddress());
			logger.debug(s.toString()+" present");
			s.enable();
			return true;
		} catch (Exception e) {
			logger.error("couldnt probe sensor "+s.toString()+". Raised exception: "+e.getMessage());
		}
		s.disconnect();
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#internal_getCMLStoreKey(jcu.sal.components.sensors.Sensor)
	 */
	@Override
	protected String internal_getCMLStoreKey(Sensor s){
		return CMLDescriptionStore.SNMP_KEY;
	}
	
	/*
	 * (non-Javadoc)
	 * @see jcu.sal.components.protocols.AbstractProtocol#detectConnectedSensors()
	 */
	@Override
	protected List<String> detectConnectedSensors() {
		List<String> detected = new Vector<String>();
		varbind v=null;
		int limit=0;

		BlockPdu p = new BlockPdu(s);
		p.setPduType(BlockPdu.GETNEXT);
		p.addOid(OID_START);
		p.setRetryIntervals(new int[]{timeout});
		try {
			while(((v = p.getResponseVariableBinding())!=null) && (limit++<MAX_AUTODETECTED_OIDS)) {
				detected.add(v.getOid().toString());
				p = new BlockPdu(s);
				p.setPduType(BlockPdu.GETNEXT);
				p.addOid(v.getOid());
				p.setRetryIntervals(new int[]{timeout});
			}
		} catch (Exception e) {
			logger.debug("SNMP response timeout while retieving OIDs, could be the end of the SNMP walk");
		}
		return detected;
	}
	

	/*
	 * Command handling methods
	 */
	// TODO create an exception class for this instead of Exception
	public static String GET_READING_METHOD = "getReading";
	public  byte[] getReading(Command c, Sensor s) throws IOException{
		return getRawReading(s.getNativeAddress());
	}
	
	private byte[] getRawReading(String oid) throws IOException{

		varbind v=null;
		String ret=null;

		BlockPdu p = new BlockPdu(s);
		p.setPduType(BlockPdu.GET);
		p.addOid(oid);
		p.setRetryIntervals(new int[]{timeout});
		try {
			v = p.getResponseVariableBinding();
			if(v!=null) 
				ret = v.getValue().toString();
		} catch (AgentException e) {
			logger.error("SNMP response timeout while getting OID "+oid);

			throw new IOException("SNMP response timeout");
		} catch (PduException e) {
			logger.error("PDU exception while getting OID "+oid);
			throw new IOException("PDU exception");			
		}

		return ret.getBytes();
	}
}
