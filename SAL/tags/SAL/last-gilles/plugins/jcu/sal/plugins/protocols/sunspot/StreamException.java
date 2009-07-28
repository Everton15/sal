package jcu.sal.plugins.protocols.sunspot;

/**
 * This class of exception is thrown whenever there is an error starting or 
 * stopping a stream
 * @author gilles
 *
 */
public class StreamException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7710989327642480128L;

	public StreamException(){
		super();
	}
	
	public StreamException(String m){
		super(m);
	}

	public StreamException(Throwable t){
		super(t);
	}
	
	public StreamException(String m, Throwable t){
		super(m,t);
	}
}
