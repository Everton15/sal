package jcu.sal.plugins.protocols.sunspot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.microedition.io.Connector;

import jcu.sal.common.Slog;

import org.apache.log4j.Logger;

import com.sun.spot.io.j2me.radiostream.RadiostreamConnection;
import com.sun.spot.peripheral.TimeoutException;

public class SALSpotConnectionImpl implements ISALSpotConnection, Runnable{
	private static Logger logger = Logger.getLogger(SALSpotConnectionImpl.class);
	static {Slog.setupLogger(logger);}	
	
    public final static String HELLO_FROM_BS = "HelloSALSpot";
    public final static String HELLO_TO_BS = "HelloSALBS";
    public final static int SOCKET_TIMEOUT = 1000;
    //how many ping replies can we missed before closing the connection
    //in units of SOCKET_TIMEOUT seconds
    public final static int PING_TIMEOUT = 5;
    private final static String DELIM = "@";
    private final static String CLOSE = "Close";
    private final static String PING = "Ping";
    private final static String CMD_DO = "DO";
    private final static String CMD_STOP = "STOP";
    private final static String REPLY_DATA = "REPLY";
    private final static String REPLY_ERROR = "ERROR";
    private final static List<String> usedPorts = new Vector<String>();
    private RadiostreamConnection conn;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String address;
    private int port;
    private SpotResponseHandler client;
    private Thread dataListener;
    private PingingThread pingThread;
    private boolean stop;
    private State state;
    private int missedPings;

    public SALSpotConnectionImpl(String url){
        address = url.substring(url.indexOf("//") + 2, url.lastIndexOf(':'));
        port = Integer.parseInt(url.substring(url.lastIndexOf(':') + 1));
        state = new State();
    }

    /**
     * This method opens the connection to the base station
     * @throws java.io.IOException if there is an error opening
     * the connection, or if the client is not set.
     */
    public synchronized void open(SpotResponseHandler c) throws IOException {
        if (!state.connect()) {
            return;
        }

        client = c;
        String url = "radiostream://" + address + ":" + port;
        conn = (RadiostreamConnection) Connector.open(url);
        conn.setTimeout(SOCKET_TIMEOUT);
        dis = conn.openDataInputStream();
        dos = conn.openDataOutputStream();
        doHanshake();
        stop = false;
        dataListener = new Thread(this, "SALSpot listener");
        dataListener.start();
        pingThread = new PingingThread(this);
        pingThread.start();
    }

    /**
     * This method completes the initial handshake with a SAL spot
     * and opens the connection.
     * @throws IOException if the handshake cant be completed.
     */
    private void doHanshake() throws IOException {
		int i=3;
		while(i-->0){
			try {
				//logger.debug("Sending hello to spot");
				dos.writeUTF(HELLO_FROM_BS);
				dos.flush();
				if(dis.readUTF().equals(HELLO_TO_BS)){
					//logger.debug("Handshake succeeded: connected to spot "+address);
					return;
				}
			}catch (TimeoutException e){ 
			}catch (IOException e) {
				logger.error("Handshake failed: got exception waiting for hello ("
						+e.getMessage()+")");
				throw new IOException("handshake failed", e);
			}
		}
		logger.error("Failed to complete handshake - no Hello from spot");
		close();
		throw new IOException("handshake failed");
    }

    /**
     * This method returns the port number used by the connection
     * to the spot
     * @return the port number
     */
    public int getPort() {
        return port;
    }

    /**
     * This method returns address of the spot
     * @return the address of the spot
     */
    public String getRemoteAddress() {
        return address;
    }


    /**
     * This method terminates this connection to the spot
     */
    @Override
    public synchronized void terminate() {
        if (!state.terminate()) {
            return;
        }
        logger.debug("Sending CLOSE to SALSpot "+address+":"+port);
        try {
            sendData(CLOSE);
        } catch (IOException e) {
        	logger.error("Error sending CLOSE: " + e.getMessage());
        }
        stopDataListener();
        close();
        //logger.debug("SAL Spot "+address+":"+port+" connection closed");
    }
    
	@Override
	public void sendDoCommand(String sensor, String cmd, int freq)
		throws IOException{
		try{
			sendData(CMD_DO+" "+sensor+" "+cmd+" "+freq);
		} catch (IOException e){
			logger.error("Error sending DO command '"+CMD_DO+" "+sensor+" "+cmd+" "+freq+"' - closing");
			stop = true;
			throw e;
		}
	}
	
	@Override
	public void sendStopCommand(String sensor, String cmd) throws IOException{
		try {
			sendData(CMD_STOP+" "+sensor+" "+cmd);
		} catch (IOException e){
			logger.error("Error sending STOP command '"+CMD_STOP+" "+sensor+" "+cmd+"' - closing");
			stop = true;
			throw e;
		}
	}
	
	@Override
	public void sendPing() throws IOException{
		try {
			sendData(PING);
		} catch (IOException e){
			logger.error("Error sending PING command - closing");
			stop = true;
			throw e;
		}
	}

	/**
	 * This method closes the data streams and the {@link RadiostreamConnection}
	 * objects. It then notifies threads {@link Object#wait()}ing on the 
	 * {@link #client}.
	 */
    private void close() {
        if (!state.close()) {
            return;
        }
        //logger.debug("Closing connection with SAL Spot");
        
        if(pingThread!=null)
        	pingThread.stop();

        //close outputstream
        synchronized (dos) {
            try {
                dos.close();
            } catch (IOException e) {}
        }

        //close input stream
        synchronized (dis) {
            try {
                dis.close();
            } catch (IOException e) {}
        }

        //close connection
        try {
            conn.close();
        } catch (IOException e) {}
        
        //removing port from used port list
        synchronized(usedPorts) {
        	//logger.debug("Removing port "+port+" from used list");
			usedPorts.remove(String.valueOf(port));
		}
        
        logger.debug("Connection with spot closed");
        client.connectionClosed();
    }

    /**
     * This low-level method reads raw data from the {@link #conn}ection 
     * and returns them,
     * or throws an exception ( {@link TimeoutException} or 
     * {@link IOException} )
     * @return the raw data read from the {@link #conn}ection.
     * @throws TimeoutException if the read operation times out
     * @throws IOException if there is an error reading from the connection
     */
    private String readData() throws TimeoutException, IOException {
        synchronized (dis) {
        	try{
        		return dis.readUTF();
        	}catch(TimeoutException e){
        		throw e;
        	} catch (IOException e){
        		throw e;
        	} catch (Throwable t){
        		//this catch clause is here because
        		//flush() can throw NullPointerException AFAIHaveExperimented
        		throw new IOException(t);
        	}
        }
    }

    /**
     * This low-level method write raw data to the {@link #conn}ection
     * after adding the delimiter {@link #DELIM},
     * or throws an {@link IOException}.
     * @return the raw data to be written to the {@link #conn}ection.
     * @throws IOException if there is an error writing the data
     */
    private void sendData(String s) throws IOException {
//    	if(!s.equals(PING))
//    		logger.debug("Sending "+DELIM + s + DELIM);
        synchronized (dos) {
        	try{
        		dos.writeUTF(DELIM + s + DELIM);
        		dos.flush();
        	} catch (Throwable t){
        		//this try-catch stuff is here because
        		//flush() can throw NullPointerException AFAIHaveExperimented
        		throw new IOException(t);
        	}
        }
    }

    /**
     * This method instructs the data listener thread to stop. It does NOT
     * wait until it finished.
     */
    private void stopDataListener() {
        synchronized (dataListener) {
            //stop thread
            if (dataListener.isAlive()) {
                stop = true;
                //commented out to prevent race condition:
                /*
                 * Finder thread:		Listener thread:
                 * addSpot				  close
                 *   removeSpot				removeSpot
                 *     terminate
                 *       join
                 */
//                try {
//                    dataListener.join();
//                } catch (InterruptedException e1) {
//                    e1.printStackTrace();
//                }
            }
        }
    }

    /**
     * This method process a string received form a sal spot.
     * @param d the string
     * @return <code>false</code> if there is an IO error, otherwise
     * true
     */  
	private boolean processReplyData(String data) {
		String[] commands = split(data, DELIM);
		String[] tokens;
		for(String cmd: commands){
			tokens = split(cmd, " ");
			if(tokens[0].equals(CLOSE)){
				logger.debug("Received close from spot - closing");
				stop=true;
				return true;
			} else if(tokens[0].equals(PING)){
				missedPings = 0;
				return true;
			} else if(tokens[0].equals(REPLY_DATA)){
				//logger.debug("Processing single cmd: "+cmd);
				client.processReplyMessage(tokens);
			} else if(tokens[0].equals(REPLY_ERROR)){
				//logger.debug("Processing single cmd: "+cmd);
				client.processReplyErrorMessage(tokens);
			} else
				logger.error("Unknown command: "+tokens[0]);
		}
		return true;
	}

	/**
	 * This method split a given string into tokens, using the specified
	 * delimiter
	 * @param msg the string to be split
	 * @param delim the delimiter
	 * @return an array of tokens
	 */
    public static String[] split(String msg, String delim) {
        StringTokenizer stk = new StringTokenizer(msg, delim);
        String[] result = new String[stk.countTokens()];
        for (int i = 0; stk.hasMoreTokens(); i++) {
            result[i] = stk.nextToken();
        }
        return result;
    }
    
    /**
     * This method returns the list of currently used ports. It s use MUST
     * be synchronized !
     * @return the list of currently used ports
     */
    public static List<String> getUsedPorts(){
    	return usedPorts;
    }

    /**
     * This method is run in a separate thread. It listens for raw incoming data
     * from a sal spot, removes the delimiters ({@link #DELIM}), and passes the 
     * string on to {@link #processReplyData(String)} for processing. If an
     * IO error occurs during reading on the connection, or during the 
     * processing, the thread closes the connection and exits   
     */
    public void run() {
        String data, leftOver = "";
        missedPings = 0;
        //logger.debug("SALSpot listener thread started");
        while (!stop) {
            try {
                data = leftOver + readData();
                //System.out.println("Received " + data);
                if (data.endsWith(DELIM)) {
                } else {
                	logger.debug("DATA DOES NOT END WITH " + DELIM);
                    leftOver =data.substring(data.lastIndexOf(DELIM.charAt(0)));
                    data = data.substring(0,
                            data.lastIndexOf(DELIM.charAt(0) - 1));
                    logger.debug("LEFTOVER: " + leftOver);
                    logger.debug("Processing first bit: " + data);
                }

                if (!processReplyData(data)) {
                	logger.error("Error processing reply" +
                            " - closing connection");
                    stop = true;
                }

            } catch (TimeoutException e) {
            	if((++missedPings)>=PING_TIMEOUT){
            		logger.debug("Ping timeout from spot " +
                            "- closing connection");
                    stop = true;
            	}
            } catch (IOException e) {
            	logger.error("IO Error reading from spot (" +
                        e.getMessage() + ")- closing connection");
                stop = true;
            }
        }
        close();
        //logger.debug("SALSpot listener thread exited");
    }

    private static class State {

        private static final int DISCONNECTED = 0;
        private static final int IDLE = 1;
        private static final int STREAMING = 2;
        private static final int TERMINATING = 3;
        private static final int CLOSED = 4;
        private int streamCount;
        private int state;

        public State() {
            state = DISCONNECTED;
            streamCount = 0;
        }

        public synchronized boolean connect() {
            switch (state) {
                case DISCONNECTED:
                    state = IDLE;
                    return true;
                default:
                    return false;
            }
        }

        public synchronized void startStream() {
            switch (state) {
                case IDLE:
                case STREAMING:
                    state = STREAMING;
                    streamCount++;
                    return;
                default:
                    throw new StateException("Not idle or streaming");
            }
        }

        public synchronized void stopStream() {
            switch (state) {
                case STREAMING:
                    if (--streamCount == 0) {
                        state = IDLE;
                    }
                    return;
                default:
                    throw new StateException("Not streaming");
            }
        }

        public synchronized boolean terminate() {
            switch (state) {
                case IDLE:
                case STREAMING:
                    state = TERMINATING;
                    return true;
                default:
                    return false;
            }
        }

        public synchronized boolean close() {
        	switch (state) {
            case IDLE:
            case STREAMING:
            case TERMINATING:
                state = CLOSED;
                return true;
            default:
                return false;
        	}
        }
    }
    
    private static class PingingThread implements Runnable {
    	private boolean stop;
    	private Thread thread;
    	private ISALSpotConnection conn;
    	private static final int PING_INTERVAL = 4000;
    	
    	public PingingThread(ISALSpotConnection c){
    		conn = c;
    	}
    	public synchronized void start(){
    		if(thread==null){
    			stop = false;
    			thread = new Thread(this, "Ping thread");
    			thread.start();
    		}
    	}
    	
    	public synchronized void stop(){
    		if(thread!=null) {
    			stop = true;
    			thread.interrupt();
    			thread = null;
    		}
    	}
    	
    	public void run(){
    		while(!stop && !Thread.interrupted()){
    			try {
    				//logger.debug("Pinging");
					conn.sendPing();
					Thread.sleep(PING_INTERVAL);
				} catch (IOException e) {
					stop = true;
				} catch (InterruptedException e) {
					stop = true;
				}
    		}
    		//logger.debug("Pinging thread exited");
    	}
    }
}
