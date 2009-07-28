package jcu.sal.plugins.protocols.sunspot;

/**
 * This class encapsulates the result of a command.
 * @author gilles
 *
 */
public class StreamData {
	private String data;
	private StreamException error;
	
	/**
	 * This method builds a new {@link StreamData} object with the given string
	 * @param d the data
	 */
	public StreamData(String d){
		data = d;
		error = null;
	}
	
	/**
	 * This method builds a new {@link StreamData} object which will carry
	 * a stream closed message. It will be thrown when the recipient invokes the
	 * {@link #getData()} method.
	 * @param t
	 */
	public StreamData(StreamClosedException e){
		error = e;
	}
	
	/**
	 * This method builds a new {@link StreamData} object which will carry
	 * the given exception. It will be thrown when the recipient invokes the 
	 * {@link #getData()} method.
	 * @param t
	 */
	public StreamData(StreamException e){
		error = e;
	}
	
	/**
	 * this method returns the data carried in this object
	 * @return the data carried in this object
	 * @throws StreamClosedException if the stream was closed
	 * @throws StreamException if there was an error and the stream has closed.
	 */
	public String getData() throws StreamClosedException, StreamException{
		if(error!=null)
			throw error;
		
		return data;
	}

}
