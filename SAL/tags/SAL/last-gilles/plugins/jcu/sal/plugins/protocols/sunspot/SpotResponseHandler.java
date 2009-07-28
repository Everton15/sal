package jcu.sal.plugins.protocols.sunspot;

/**
 * Classes implementing this interface can be used to handle data coming from
 * SAL spot. These classes handle the various responses received (REPLY_Ok, 
 * REPLY_ERROR, ...) and can be notified when the low level connection has 
 * closed
 * @author gilles
 *
 */
public interface SpotResponseHandler {
	
	/**
	 * This method is called to notify this handler that the low-level 
	 * connection has been closed.
	 */
	public void connectionClosed();
	
	/**
	 * This method is invoked every time a REPLY_DATA packet has been received
	 * by the low-level connection object and must be processed
	 * @param tokens the tokens in the message
	 */
	public void processReplyMessage(String[] tokens);

	/**
	 * This method is invoked every time a REPLY_ERROR packet has been received
	 * by the low-level connection object and must be processed
	 * @param tokens the tokens in the message
	 */
	public void processReplyErrorMessage(String[] tokens);
}
