package jcu.sal.client.gui.view;

import java.awt.CardLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class ActionPanel{
	
	public static String SENSOR_CONTROL_PANE_TITLE = "Sensor control";
	public static String PROTOCOL_CONTROL_PANE_TITLE = "Protocol control";
	public static String AGENT_CONTROL_PANE_TITLE = "Agent control";
	public static String EMPTY_CONTROL_PANE_TITLE = "";
	

	private static final long serialVersionUID = -3389916952429009472L;
	
	private ClientView view;
	private JLabel title;
	private SensorControlPane sensorPane;
	private JPanel controlPane, emptyPane, mainPane;

	public ActionPanel(ClientView v){
		super();
		
		view = v;
		controlPane = new JPanel(new CardLayout());
		mainPane = new JPanel();
		emptyPane = new JPanel();
		title = new JLabel();
		
		sensorPane = new SensorControlPane(view);
	}
	
	public void init(){
		sensorPane.init();
		controlPane.add(sensorPane.getPanel(),SENSOR_CONTROL_PANE_TITLE);
		controlPane.add(emptyPane,EMPTY_CONTROL_PANE_TITLE);
		
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));
		
		title.setHorizontalAlignment(SwingConstants.CENTER);
		mainPane.add(title);

		
		mainPane.add(Box.createGlue());
		mainPane.add(controlPane);
		
		mainPane.setBorder(BorderFactory.createEmptyBorder(7, 7, 0, 0));
		
		switchToEmptyPane();
	}
	
	public JPanel getPanel(){
		return mainPane;
	}
	
	/**
	 * This method triggers the display of information (commands, ...)
	 * related to a sensor given its {@link Context} object.
	 * @param the context of the sensor element. If <code>null</code> the panel data is reset.
	 */
	public void displaySensor(Context c){
		switchToSensorPane();
		sensorPane.displaySensor(c);
	}
	
	/**
	 * This method displays the SensorPanel in this action panel.
	 */
	public void switchToSensorPane(){
		switchToPane(SENSOR_CONTROL_PANE_TITLE);
	}
	
	/**
	 * This method displays the SensorPanel in this action panel.
	 */
	public void switchToEmptyPane(){
		switchToPane(EMPTY_CONTROL_PANE_TITLE);
	}
	
	private void switchToPane(String t){
		title.setText(t);
		((CardLayout)controlPane.getLayout()).show(controlPane, t);
	}
	
}
