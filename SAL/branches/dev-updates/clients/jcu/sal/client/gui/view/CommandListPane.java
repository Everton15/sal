package jcu.sal.client.gui.view;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jcu.sal.client.gui.ClientController;
import jcu.sal.common.cml.CMLDescription;
import jcu.sal.common.cml.CMLDescriptions;
import jcu.sal.common.exceptions.NotFoundException;
import jcu.sal.common.exceptions.SALDocumentException;

public class CommandListPane implements ListSelectionListener{

	private static final long serialVersionUID = -2116369411796825252L;
	
	private ClientView view;
	private ClientController controller;
	private JPanel panel;
	private JLabel cmdLabel;
	private DefaultListModel listModel;
	private JList list;
	private CommandDataPane cmdPane;
	private Context current;
	
	public CommandListPane(ClientView v, CommandDataPane d){
		super();
		panel = new JPanel();
		view = v;
		controller = v.getController();
		cmdPane = d;
		cmdLabel = new JLabel("Command list");

		listModel = new DefaultListModel();
		list = new JList(listModel);

	}
	
	public void init(){
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setMaximumSize(new Dimension(202, 820));
		
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.addListSelectionListener(this);
		
		cmdLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(cmdLabel);
		panel.add(Box.createVerticalStrut(15));
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setMinimumSize(new Dimension(300, 400));
		listScroller.setMaximumSize(new Dimension(350, Integer.MAX_VALUE));
		panel.add(listScroller);
	}
		
	public JPanel getPanel(){
		return panel;
	}
	
	
	/**
	 * This method updates the list of commands with the 
	 * list of commands for the given sensor. 
	 * @param c the {@link Context} object of the sensor whose command list is to be 
	 * displayed. If <code>null</code> the command list is simply cleared.
	 */
	public void displayCommand(Context c){
		CMLDescriptions cmls = null;
		List<CommandListLabel> l = new Vector<CommandListLabel>();
		listModel.clear();
		cmdPane.displayCommandData(null, null);
		if(c!=null) {
			
			//check if the Context has CML descriptions
			if(c.getCMLDescriptions()==null){
				//no it doesnt so retrieve them...
				try {
					cmls = new CMLDescriptions(controller.getCML(c.getAgent(), c.getSMLDescription().getID()));
				} catch (SALDocumentException e) {
					view.addLog("Error in the CML document for sensor '"+c.getSMLDescription().getID()+"'");
					e.printStackTrace();
					return;
				} catch (NotFoundException e) {
					view.addLog("No sensor with ID '"+c.getSMLDescription().getID()+"'");
					e.printStackTrace();
					return;
				}
				//and store them for next time
				c.setCMLDescriptions(cmls);
			} else
				//yes it does, so get them.
				cmls = c.getCMLDescriptions();
			
			for(CMLDescription cml : cmls.getDescriptions())
				l.add(new CommandListLabel(cml));
			
			Collections.sort(l);
			for(CommandListLabel cll: l)
				listModel.addElement(cll);
		
			
			panel.revalidate();
			panel.repaint();
		}
		current = c;
	}
	
	/**
	 * This method is called whenever a element in the command list (in cmdPane)
	 * is selected.
	 * @param e
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(!e.getValueIsAdjusting())
			if(list.getSelectedValue()!=null)
				cmdPane.displayCommandData(((CommandListLabel) list.getSelectedValue()).getCML(), current);
	}
	
	public static class CommandListLabel implements Comparable<CommandListLabel>{
		private CMLDescription cml;
		
		public CommandListLabel(CMLDescription c){
			cml = c;
		}
		
		public String toString(){
			return cml.getName();
		}
		
		public CMLDescription getCML(){
			return cml;
		}

		@Override
		public int compareTo(CommandListLabel o) {
			return cml.getName().compareTo(o.cml.getName());
		}
		
	}
}
