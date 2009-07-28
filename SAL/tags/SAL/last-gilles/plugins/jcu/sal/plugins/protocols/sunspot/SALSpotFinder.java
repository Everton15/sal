package jcu.sal.plugins.protocols.sunspot;

import java.io.IOException;
import java.util.List;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;

import jcu.sal.common.Slog;

import org.apache.log4j.Logger;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.peripheral.TimeoutException;
import com.sun.squawk.util.Arrays;

/**
 * This class listens for broadcasts from {@link SALSpot}s, establishes a
 * connection with them and notifies the {@link BaseStation} when a new is 
 * found.
 * @author gilles
 *
 */
public class SALSpotFinder implements Runnable{
	private static Logger logger = Logger.getLogger(SALSpotFinder.class);
	static {Slog.setupLogger(logger);}
	
    private final static String I_M_A_SAL_SPOT="SALSpot";
    private final static String I_M_A_SAL_BS="SALBS";
	private static final int BROADCAST_PORT = 34;
	
	private Thread t;
	private static RadiogramConnection bsHandshake; //for establishing the connection
	private boolean stop;
	private Datagram dg;
	
	/**
	 * This method builds a new {@link SALSpotFinder} object. It must be started
	 * with a call to {@link #start()}.
	 */
	public SALSpotFinder(){
		stop = false;
        t=null;
	}
	
	/**
	 * This method starts the finder if it is not. If it is already started,
	 * this method does nothing.
	 * @throws IOException if the listener port for incoming broadcasts can not
	 * be opened  
	 */
	public synchronized void start() throws IOException{
		if(t==null){
			bsHandshake = (RadiogramConnection) Connector.open("radiogram://:"+BROADCAST_PORT);
			bsHandshake.setTimeout(1000);
	        dg = bsHandshake.newDatagram(bsHandshake.getMaximumLength());
			t = new Thread(this,"SALSpot finder");
			t.start();
		}
	}
	
	/**
	 * This method stops the finder if it is running. It not, it does nothing. 
	 */
	public synchronized void stop(){
		if(t!=null && t.isAlive()){
			stop=true;
			try {
				t.join();
			} catch (InterruptedException e) {}
			try {
				bsHandshake.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			t=null;
		}
	}
	
	/**
	 * This method returns the next available port or -1 if there is none
	 * @return the next available port or -1 if there is none
	 */
	private int getAvailablePort(){
		int i = 0;
		synchronized(SALSpotConnectionImpl.getUsedPorts()){
			List<String> list = SALSpotConnectionImpl.getUsedPorts(); 
			int[] used = new int[list.size()];
			for(String p: list)
				used[i++] = Integer.parseInt(p);				
			
			Arrays.sort(used);
			for(i=32;i<255;i++)
				if(Arrays.binarySearch(used, i)<0 && i!=BROADCAST_PORT){
					list.add(String.valueOf(i));
					return i;
				}
		}
		return -1;
	}
	
	/**
	 * This method sends a string out on the {@link #bsHandshake} connection.
	 * @param message the string to be sent
	 * @param adressSourceDG the {@link Datagram} which specifies the 
	 * destination address.
	 */
	private void sendReply(String message, Datagram adressSourceDG){
		Datagram dgreply;
        try {
            dgreply = bsHandshake.newDatagram(bsHandshake.getMaximumLength());
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        dgreply.reset();        // reset stream pointer
        dgreply.setAddress(adressSourceDG);
        try {
            dgreply.writeUTF(message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
        	bsHandshake.send(dgreply);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}
	
	@Override
	public void run() {
		int port;
		String url;
		SALSpotConnectionImpl c;
		SALSpot s;
		//logger.debug("SunSpot finder thread started");
		while(!stop){
			try {
				bsHandshake.receive(dg);
				if(dg.readUTF().indexOf(I_M_A_SAL_SPOT)!=-1){
					port = getAvailablePort();
					url = "radiostream://"+dg.getAddress()+":"+port;
					sendReply(I_M_A_SAL_BS+" "+port, dg);
					//logger.debug("Found SALSunSpot: "+url);
					c = new SALSpotConnectionImpl(url);
					try {
						s = new SALSpot(c,dg.getAddress());
						BaseStation.addSpot(s);
					} catch (IOException e){
						logger.error("Error opening the connection to the"
								+" sal spot");
					}
					
				}
			} catch(TimeoutException e){}
			catch (IOException e) {
				logger.error("Error listening for broadcasts: "+e.getMessage());
				stop=true;
			}
		}
//		logger.debug("SunSpot finder thread stopped");
	}	
}
