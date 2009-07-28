package jcu.sal.components.EndPoints;

/**
 * Classes implementing this interface will have their deviceChange() method called whenever the devices they
 * registered for are plugged in or unplugged.
 * @author gilles
 *
 */
public interface DeviceListener {
	/**
	 * this method is called whenever watched-devices are unplugged / plugged
	 * @param n the number of watched-devices currently plugged in
	 * @param id the ID of the watched-devices which number changed
	 */
	public void adapterChange(int n, String id);

}
