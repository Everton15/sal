package jcu.sal.client.gui.view;

import java.io.IOException;

import jcu.sal.client.gui.view.TextResponseFrame.StreamData;
import jcu.sal.common.Response;
import jcu.sal.common.StreamID;
import jcu.sal.common.exceptions.ClosedStreamException;
import jcu.sal.common.exceptions.SensorControlException;


public class TextResponseHandler implements ResponseHandler{
	private ClientView view;
	private StreamID sid;
	private Context context;
	private StreamData d;
	
	public TextResponseHandler(Context c, ClientView v){
		view = v;
		context = c;
		sid = null;
		d = TextResponseFrame.getFrame().addStream(this);
	}

	@Override
	public void close() {
		TextResponseFrame.getFrame().removeStream(d);
		if(sid!=null)
			view.terminateStream(context.getAgent(), sid);
	}

	@Override
	public void collect(Response r) throws IOException {
		try {
			d.setValue(r.getString());			
		} catch (SensorControlException e) {
			d.setValue("Stream CLOSED");
			view.addLog("Stream from sensor "+r.getSID()+" terminated");
			if(!e.getClass().equals(ClosedStreamException.class))
				view.addLog("Error: "+e.getMessage());
		}	
	}

	@Override
	public void setStreamID(StreamID s) {
		sid = s;
		d.setID(s.getID());
	}
	
	@Override
	public StreamID getStreamID() {
		return sid;
	}

}
