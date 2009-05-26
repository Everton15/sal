/**
 * <h1>HAL java filter package</h1>
 * This package contains the {@link au.edu.jcu.haldbus.filter.AbstractHalFilter} abstract base class, 
 * which must be derived by all HAL filters.
 * 
 * A filter contains:
 * <ul>
 * <li>a list of criteria an HAL object must match,</li>
 * <li>a <code>deviceAdded()</code> method that will be called when a 
 * DeviceAdded signal is received about an HAL object that matches the criteria,</li>
 * <li>a <code>deviceRemoved()</code> method that will be called when a 
 * DeviceRemoved signal is received about an HAL object that matches the criteria.</li>
 * </ul>
 * 
 * See the V4LHalFilter class in the examples package.
 */
package au.edu.jcu.haldbus.filter;


