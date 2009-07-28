package jcu.sal.client.gui.view;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class SensorControlPane{

	private static final long serialVersionUID = -3678056141437194190L;
	private ClientView view;
	private CommandListPane cmdPane;
	private CommandDataPane cmdDataPane;
	private DescriptionPane descriptionPane;

	/**
	 * containerPane contains cmdPane and cmdDataPane
	 */
	private JPanel containerPane;
	private JPanel mainPane;
	
	public SensorControlPane(ClientView v){
		super();
		mainPane = new JPanel();
		view = v;
		cmdDataPane = new CommandDataPane(view);
		cmdPane = new CommandListPane(view, cmdDataPane);
		descriptionPane = new DescriptionPane();
		containerPane = new JPanel();


	}
	
	public void init(){
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));
		
		descriptionPane.init();		
		//mainPane.add(descriptionPane.getPanel());
		
		containerPane.setLayout(new BoxLayout(containerPane, BoxLayout.LINE_AXIS));
		cmdPane.init();
//		cmdPane.getPanel().setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(Color.red),
//                cmdPane.getPanel().getBorder()));

		cmdDataPane.init();
//		cmdDataPane.getPanel().setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(Color.red),
//                cmdDataPane.getPanel().getBorder()));

		cmdPane.getPanel().setAlignmentX(Component.RIGHT_ALIGNMENT);
		containerPane.add(cmdPane.getPanel());
		containerPane.add(Box.createHorizontalStrut(20));
		JScrollPane listScroller = new JScrollPane(cmdDataPane.getPanel());
		//listScroller.setPreferredSize(new Dimension(250, 80));
		containerPane.add(listScroller);
		
		mainPane.add(containerPane);
	}
	
	public JPanel getPanel(){
		return mainPane;
	}
	
	/**
	 * This method updates this panel with the information relative
	 * to the given sensor
	 * @param c the {@link Context} object of the sensor
	 * whose information is to be displayed. If <code>null</code> the panel data is reset.
	 */
	public void displaySensor(Context c){			
		cmdPane.displayCommand(c);
	}
	
	public void setDescription(Context c){
		//SMLDescription sml = c.getSMLDescription();
		
	}

	private class DescriptionPane{
		private JLabel id, protocolName, protocolType;
		private JPanel mainPane;
		
		public DescriptionPane(){
			id = new JLabel("Sensor ID:");
			protocolName = new JLabel("Protocol name:");
			protocolType = new JLabel("Protocol type:");
			mainPane = new JPanel();
		}
		
		public void init(){
			id.setMinimumSize(new Dimension( 150, (int) id.getMinimumSize().getHeight()));
			protocolName.setMinimumSize(new Dimension( 150, (int) id.getMinimumSize().getHeight()));
			protocolType.setMinimumSize(new Dimension( 150, (int) id.getMinimumSize().getHeight()));

			
			mainPane.setLayout(new SpringLayout());
			mainPane.add(id);
			mainPane.add(protocolName);
			mainPane.add(protocolType);
			mainPane.add(new JLabel());
			SpringLayoutHelper.makeGrid(mainPane, 2, 2, 10, 10, 5, 5);
		}
		
		public JPanel getPanel(){
			return mainPane;
		}
		
		public void printSize(){
			System.out.println("Min: "+id.getMinimumSize().width+"x"+id.getMinimumSize().height);
			System.out.println("Cur: "+id.getSize().width+"x"+id.getSize().height);
			System.out.println("Max: "+id.getMaximumSize().width+"x"+id.getMaximumSize().height);
		}
	}

}
