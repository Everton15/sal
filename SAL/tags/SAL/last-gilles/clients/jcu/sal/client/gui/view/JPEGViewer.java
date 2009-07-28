package jcu.sal.client.gui.view;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import jcu.sal.common.Response;
import jcu.sal.common.StreamID;
import jcu.sal.common.exceptions.ClosedStreamException;
import jcu.sal.common.exceptions.SensorControlException;

public class JPEGViewer extends WindowAdapter implements ResponseHandler{	

	/**
	 * How often do we refresh the frame rate (in milliseconds)
	 */
	private static int FPS_REFRESH = 1 * 1000;
	
	private StreamID sid;
	private JFrame frame;
	private JLabel image;
	private String title;
	private long start = 0;
	private int n = 0;
	private ClientView view;
	private Context context;
	
	/**
	 * This method builds a JPEG viewer 
	 * @param c the context object
	 */
	public JPEGViewer(Context c, ClientView v){
		view = v;
		context = c;
		sid = null;
		title = c.getProtocolConfiguration().getType()+" - "+c.getSMLDescription().getID();
		frame = new JFrame(title);
		image = new JLabel();
		frame.getContentPane().add(image);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		frame.addWindowListener(this);
	}

	public void setImage(byte[] b) {
		n++;
		image.setIcon(new ImageIcon(b));
		if(start==0){
    		start = System.currentTimeMillis();
    		frame.setSize(image.getIcon().getIconWidth(), image.getIcon().getIconHeight());
		}else if(System.currentTimeMillis()>start+FPS_REFRESH) {
			frame.setTitle(title + " - FPS: "+ (((float) 1000*n/(System.currentTimeMillis()-start))  ));
			start = System.currentTimeMillis();
			n = 0;
		}
	}
	
	private void terminateStream(){
		if(sid!=null)
			view.terminateStream(context.getAgent(), sid);
	}
	
	@Override
	public void setStreamID(StreamID s){
		sid = s;
	}
	
	@Override
	public StreamID getStreamID() {
		return sid;
	}
	
	@Override
    public void close() {
		terminateStream();
    	SwingUtilities.invokeLater(new Runnable(){
    		public void run(){
    			frame.dispose();    			
    		}
    	});
    }

	@Override
	public void collect(Response r){
		try {
			setImage(r.getBytes());
		} catch (SensorControlException e) {
			view.addLog("Stream from sensor "+r.getSID()+" terminated");
			if(!e.getClass().equals(ClosedStreamException.class))
				view.addLog("Error: "+e.getMessage());
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		terminateStream();		
	}

}
