package jcu.sal.plugins.protocols.sunspot;

public class StreamClosedException extends StreamException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3428406672686598872L;

	public StreamClosedException(){
		super();
	}
	
	public StreamClosedException(String m){
		super(m);
	}

	public StreamClosedException(Throwable t){
		super(t);
	}
	
	public StreamClosedException(String m, Throwable t){
		super(m,t);
	}
}
