package jcu.sal.plugins.protocols.sunspot;

public class StateException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -829363147285784392L;

	public StateException (){
		super();
	}
	
	public StateException (String m){
		super(m);
	}
	
	public StateException (String m, Throwable t){
		super(m, t);
	}
	
	public StateException (Throwable t){
		super(t);
	}
	

}
