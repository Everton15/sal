package jcu.sal.client.gui.view;

import java.awt.Component;

/**
 * This interface is implemented by classes which display
 * a GUI element depending on an argument type, from which the value of
 * the argument can be retrieved.
 * @author gilles
 *
 */
public interface ArgumentValueHolder {
	/**
	 * This method returns a textual value of the argument's
	 * value, or null if the entered value is not correct
	 * @return
	 */
	public String getValue();
	public Component getComponent();
}
