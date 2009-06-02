package jcu.sal.client.gui.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jcu.sal.common.CommandFactory;
import jcu.sal.common.cml.ArgumentType;
import jcu.sal.common.cml.CMLArgument;
import jcu.sal.common.cml.CMLConstants;
import jcu.sal.common.cml.CMLDescription;
import jcu.sal.common.cml.ResponseType;
import jcu.sal.common.cml.CMLDescription.SamplingBounds;

public class CommandDataPane implements ActionListener{
	
	private static final long serialVersionUID = -3929136251479602868L;
	private ClientView view;
	private JPanel mainPane, dataPane;
	private JButton send;
	private JLabel title;
	private Hashtable<String, ArgumentValueHolder> argValues;
	private CMLDescription cml;
	private Context current;
	public static final String INTERVAL_NAME="INTERVAL_SLIDER";
	
	public CommandDataPane(ClientView v){
		view = v;
		dataPane = new JPanel(new SpringLayout());
		mainPane = new JPanel();
		send = new JButton("Send command");
		title = new JLabel("Command detail");
		argValues = new Hashtable<String, ArgumentValueHolder>();
	}
	
	public void init(){
		send.addActionListener(this);
		
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPane.add(title);
		dataPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPane.add(dataPane);
		send.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPane.add(send);
	}
	
	public JPanel getPanel(){
		return mainPane;
	}
	
	/**
	 * This method is called whenever this panel should display
	 * command data for the new command, given in argument 
	 * @param c the {@link CMLDescription} object of the new command.
	 * @param s the sensor ID where the command belongs
	 * If <code>null</code> the panel is simply cleared.
	 */
	public void displayCommandData(CMLDescription c, Context ctx){
		dataPane.removeAll();
		dataPane.repaint();

		if(c!=null){
			int i=0;
			
			i += addDescription(c.getShortDesc());
			i += addRetType(c.getResponseType());
			i += addArgs(c.getArguments());
			if(c.isStreamable())
				i += addInterval(c.getSamplingBounds());
			
			SpringLayoutHelper.makeCompactGrid(dataPane,
					i, 2, //rows, cols
	                6, 6,        //initX, initY
	                6, 6);       //xPad, yPad
			send.setEnabled(true);
			mainPane.validate();
			mainPane.repaint();
		} else
			send.setEnabled(false);
		cml = c;
		current = ctx;
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(send)){
			Hashtable<String,String> values = new Hashtable<String,String>();
			//put entered values in a table
			for(String name: argValues.keySet())
				values.put(name, argValues.get(name).getValue());
						
			//send table to view
			view.sendCommand(values, cml, current);
			
		}
	}
	
	private int addDescription(String d){
		dataPane.add(new JLabel("Description:"));
		JTextArea t = new JTextArea(d);
		t.setEditable(false);
		JScrollPane s = new JScrollPane(t);
		s.setPreferredSize(new Dimension(200,75));
		s.setMaximumSize(new Dimension(800,75));
		dataPane.add(s);
		return 1;
	}
	
	private int addRetType(ResponseType r){
		dataPane.add(new JLabel("Return Type:"));
		dataPane.add(new JLabel(r.getType()));
		if(!r.getType().equals(CMLConstants.RET_TYPE_VOID)){
			dataPane.add(new JLabel("Content Type:"));
			dataPane.add(new JLabel(r.getContentType()));
			dataPane.add(new JLabel("Unit:"));
			dataPane.add(new JLabel(r.getUnit()));
			return 3;
		} else 
			return 1;
	}
	
	private int  addInterval(SamplingBounds b){
		ArgumentValueHolder v = new IntervalArgument(b);
		JLabel l = new JLabel("Sampling frequency");
		dataPane.add(l);
		argValues.put(CommandDataPane.INTERVAL_NAME, v);
		l.setLabelFor(v.getComponent());
		dataPane.add(v.getComponent());
		return 1;
	}
	
	private int addArgs(List<CMLArgument> args){
		int nb=0;
		argValues.clear();

		for(CMLArgument arg: args){
			ArgumentValueHolder v;
			if(arg.getType().equals(ArgumentType.IntegerArgument) || 
				arg.getType().equals(ArgumentType.FloatArgument)	)
				v = createNumberArgHolder(arg);
			else if(arg.getType().equals(ArgumentType.ListArgument))
				v = createListArgHolder(arg);
			else //if(arg.getType().equals(ArgumentType.StringArgument))
				v = createStringArgHolder(arg);
			
			JLabel l = new JLabel(arg.getName());
			dataPane.add(l);
			argValues.put(arg.getName(), v);
			l.setLabelFor(v.getComponent());
			dataPane.add(v.getComponent());
			
			nb++;
		}
		return nb;
	}
	
	private ArgumentValueHolder createNumberArgHolder(CMLArgument arg){
		if(arg.hasBounds()){
			if (arg.getType().equals(ArgumentType.FloatArgument))
				return new BoundedArgument(arg.getMinFloat(), arg.getMaxFloat(), arg.getStepFloat(), arg.getDefaultValue());
			else
				return new BoundedArgument(arg.getMinInt(), arg.getMaxInt(), arg.getStepInt(), arg.getDefaultValue());
		}else
			return new StringArgument(arg.getDefaultValue());
	}
	
	private ArgumentValueHolder createListArgHolder(CMLArgument arg){
		return new ListArgument(arg.getList(), arg.getDefaultValue());
	}
		
	private ArgumentValueHolder createStringArgHolder(CMLArgument arg){
		return new StringArgument(arg.getDefaultValue());
	}
	
	private static class BoundedArgument implements ArgumentValueHolder, ChangeListener{
		private JPanel mainPane;
		private JSlider slider;
		private JTextField text;
		private JLabel label;
		private float minf,maxf,stepf;
		/**
		 * 0: int, 1:float
		 */
		private enum Type {INT, FLOAT};
		private Type type;

		private BoundedArgument(Type t){
			type = t;
			mainPane = new JPanel();
			mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.LINE_AXIS));
		}
		
		public BoundedArgument(float min, float max, float step, String def){
			this(Type.FLOAT);
			minf = min;
			maxf = max;
			stepf = step;
			text = new JTextField(def);
			text.setPreferredSize(new Dimension(100,30));
			text.setMaximumSize(new Dimension(400,30));
			text.setAlignmentX(Component.CENTER_ALIGNMENT);
			mainPane.add(text);
			label = new JLabel("Min: "+min+" , Max: "+max+" , Step: "+step);
			label.setAlignmentX(Component.CENTER_ALIGNMENT);
			mainPane.add(label);			
		}
		
		public BoundedArgument(int min, int max, int step, String def){
			this(Type.INT);
			slider = new JSlider(SwingConstants.HORIZONTAL, min, max, min);
			slider.setAlignmentX(Component.CENTER_ALIGNMENT);
			slider.setSnapToTicks(true);
			if(def!=null) slider.setValue(Integer.parseInt(def));
			slider.addChangeListener(this);
			mainPane.add(slider);
			label = new JLabel("Value: "+String.valueOf(slider.getValue()));
			label.setMinimumSize(new Dimension(100,30));
			label.setMaximumSize(new Dimension(200,30));
			label.setSize(label.getMinimumSize());
			label.setPreferredSize(label.getMinimumSize());
			label.setAlignmentX(Component.CENTER_ALIGNMENT);
			mainPane.add(label);
		}
		
		@Override
		public void stateChanged(ChangeEvent e) {
			 label.setText("Value: "+slider.getValue());			
		}

		@Override
		public String getValue() {
			if(type.equals(Type.FLOAT)){
				if(!validateFloat())
					return null;
				
				return text.getText();
			} else
				return String.valueOf(slider.getValue());
		}
		
		public JPanel getComponent(){
			return mainPane;
		}
		
		private boolean validateFloat(){
			float val;
			try{val = Float.parseFloat(text.getText());}
			catch(NumberFormatException e){
				JOptionPane.showMessageDialog(mainPane,
						"The value "+text.getText()+" is not a float",
						"Incorrect value",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}

			if(val<minf) {
				JOptionPane.showMessageDialog(mainPane,
						"The value "+text.getText()+" is below the minimum "+minf,
						"Incorrect value",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			if(val>maxf) {
				JOptionPane.showMessageDialog(mainPane,
						"The value "+text.getText()+" is above the maximum "+maxf,
						"Incorrect value",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			if((val-minf)%stepf!=0){
				JOptionPane.showMessageDialog(mainPane,
						"The value "+text.getText()+" is not a multiple of the step "+stepf,
						"Incorrect value",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
		}
	}
	
	
	private static class ListArgument implements ArgumentValueHolder{
		private JComboBox list;
		private Vector<String> ids;
		private Vector<String> values;
		
		public ListArgument(Map<String,String> val, String def){
			ids = new Vector<String>();
			values = new Vector<String>();
			for(String n: val.keySet()){
				ids.add(n);
				values.add(val.get(n));
			}
			list = new JComboBox(values);
			list.setMinimumSize(new Dimension(100,30));
			list.setMaximumSize(new Dimension(200,30));
			list.setSize(list.getMinimumSize());
			list.setPreferredSize(list.getMinimumSize());
			if(def!=null)
				list.setSelectedItem(val.get(def));
		}

		@Override
		public String getValue() {
			return ids.get(list.getSelectedIndex());
		}
		
		public JComboBox getComponent(){
			return list;
		}
	}
	
	private static class StringArgument implements ArgumentValueHolder{
		private JTextField text;
		
		public StringArgument(String def){
			text = new JTextField(def);
			text.setPreferredSize(new Dimension(200,30));
			text.setMaximumSize(new Dimension(800,30));
		}

		@Override
		public String getValue() {
			return text.getText();
		}
		
		public JTextField getComponent(){
			return text;
		}	
	}
	
	private static class IntervalArgument implements ArgumentValueHolder, ChangeListener, ActionListener{
		private static final String ONCE_LABEL="Once";
		private static final String CONTINUOUS_LABEL="Continuous";
		private static final String CUSTOM_LABEL="Predefined";
		
		private JPanel mainPane, dataPane, buttonPane;
		private JSlider slider;
		private JRadioButton once, continuous, custom;
		private JLabel label;


		public IntervalArgument(SamplingBounds b){
		
			mainPane = new JPanel();
			dataPane = new JPanel();
			buttonPane = new JPanel();
			mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.LINE_AXIS));
			dataPane.setLayout(new BoxLayout(dataPane, BoxLayout.PAGE_AXIS));
			buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
			
			once = new JRadioButton(ONCE_LABEL);
			once.setActionCommand(ONCE_LABEL);
			once.addActionListener(this);
			continuous = new JRadioButton(CONTINUOUS_LABEL);
			continuous.setActionCommand(CONTINUOUS_LABEL);
			continuous.addActionListener(this);				
			custom = new JRadioButton(CUSTOM_LABEL);
			custom.setActionCommand(CUSTOM_LABEL);
			custom.addActionListener(this);
			ButtonGroup group = new ButtonGroup();
			group.add(custom);
			group.add(continuous);
			group.add(once);
			
			if(!b.isContinuous())
				continuous.setEnabled(false);
			
			once.setSelected(true);
			buttonPane.add(custom);
			buttonPane.add(continuous);
			buttonPane.add(once);
			dataPane.add(buttonPane);
			
			slider = new JSlider(SwingConstants.HORIZONTAL, b.getMin(), b.getMax(), b.getMin());
			slider.setAlignmentX(Component.CENTER_ALIGNMENT);
			slider.addChangeListener(this);
			if(b.getStep()!=1){
				slider.setMinorTickSpacing(b.getStep());
				slider.setPaintTicks(false);
			}			
			slider.setSnapToTicks(true);
			slider.setEnabled(false);
			dataPane.add(slider);
			
			label = new JLabel("Value: run once");
			label.setMinimumSize(new Dimension(100,30));
			label.setMaximumSize(new Dimension(200,30));
			label.setSize(label.getMinimumSize());
			label.setPreferredSize(label.getMinimumSize());
			label.setAlignmentX(Component.CENTER_ALIGNMENT);
			mainPane.add(dataPane);
			mainPane.add(label);
		}
		
		@Override
		public void stateChanged(ChangeEvent e) {
			label.setText("Value: "+slider.getValue()+" ms");		
		}

		@Override
		public String getValue() {
			if(once.isSelected()){
				return String.valueOf(CommandFactory.ONLY_ONCE);
			} else if(continuous.isSelected()){
				return String.valueOf(CommandFactory.CONTINUOUS_STREAM);
			} else {
				return String.valueOf(slider.getValue());
			}
		}
		
		public JPanel getComponent(){
			return mainPane;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand().equals(ONCE_LABEL)){
				label.setText("Value: once");
				slider.setEnabled(false);
			} else if(e.getActionCommand().equals(CONTINUOUS_LABEL)){
				label.setText("Value: continuous");
				slider.setEnabled(false);				
			} else if(e.getActionCommand().equals(CUSTOM_LABEL)){
				label.setText("Value: "+slider.getValue()+" ms");
				slider.setEnabled(true);
			} 
		}
	}
}
