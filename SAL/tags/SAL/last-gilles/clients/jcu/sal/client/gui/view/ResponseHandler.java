package jcu.sal.client.gui.view;

import java.io.IOException;

import jcu.sal.common.Response;
import jcu.sal.common.StreamID;
import jcu.sal.common.cml.StreamCallback;

/**
 * Object implementing this interface can be used as handlers for streamed data.
 * 
 * @author gilles
 *
 */
public interface ResponseHandler extends StreamCallback{
	/**
	 * This method is invoked on the handler whenever the handler must close
	 */
	public void close();
	
	/**
	 * This method is invoked by SAL to receive streamed data
	 */
	public void collect(Response r)  throws IOException ;
	
	/**
	 * This method is invoked on the handler whenever the stream ID
	 * for a stream is known.
	 * @param sid
	 */
	public void setStreamID(StreamID sid);
	
	/**
	 * This method returns the StreamID or null if there is none
	 * @return the StreamID or null if there is none 
	 */
	public StreamID getStreamID();
	
}
