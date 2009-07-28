package jcu.sal.client.gui.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class TextResponseFrame{
	private static TextResponseFrame frame = new TextResponseFrame(); 
	private JFrame mainFrame;
	private JPanel mainPane;
	private List<StreamData> streams;
	
	public static TextResponseFrame getFrame(){
		return frame;
	}
	
	private TextResponseFrame(){
		streams = new Vector<StreamData>();
		mainFrame = new JFrame("Text stream viewer");
		mainPane = new JPanel();
		JScrollPane scroll = new JScrollPane(mainPane);
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));
		mainFrame.getContentPane().add(scroll);
		mainFrame.setSize(650, 300);
		//mainFrame.pack();
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}

	
	public synchronized StreamData addStream(ResponseHandler r){
		StreamData d = new StreamData(r); 
		streams.add(d);
		mainPane.add(d.getPanel());
		mainPane.validate();
		mainPane.repaint(); 
		if(!mainFrame.isVisible())
			mainFrame.setVisible(true);
		return d;
	}
	
	public synchronized void removeStream(StreamData d){
		streams.remove(d);
		mainPane.remove(d.getPanel());
		mainPane.validate();
		mainPane.repaint(); 
		if(streams.size()==0)
			mainFrame.setVisible(false);
	}
	
	public synchronized void close(){
		for(StreamData d: new Vector<StreamData>(streams))
			d.closeStream();		
			
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				mainFrame.dispose();    			
			}
		});
	}
	
	public static class StreamData implements ActionListener{
		private JPanel mainPane;
		private JButton close;
		private JLabel id, value;
		private ResponseHandler r;
		public StreamData(ResponseHandler r){
			this.r = r;
			mainPane = new JPanel();
			mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.LINE_AXIS));
			close = new JButton("Close stream");
			close.addActionListener(this);
			id = new JLabel();
			value = new JLabel("Value: ");
			Dimension d = new Dimension(200, (int)value.getSize().getHeight());
			value.setMinimumSize(d);
			value.setPreferredSize(d);
			value.setSize(d);
			mainPane.add(close);
			mainPane.add(Box.createHorizontalGlue());
			mainPane.add(id);
			mainPane.add(Box.createHorizontalGlue());
			mainPane.add(value);
		}
		
		public JPanel getPanel(){
			return mainPane;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			closeStream();
		}
		
		public void closeStream(){
			r.close();
		}
		
		public void setValue(String v){
			value.setText("Value: "+v);
		}
		
		public void setID(String i){
			id.setText(i);
		}
	}
}
