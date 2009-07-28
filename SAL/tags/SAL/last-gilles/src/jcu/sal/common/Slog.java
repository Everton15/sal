/**
 * 
 */
package jcu.sal.common;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * @author gilles
 * Takes care of setting up the logger defined in each class
 * in the same way (with the same appender, pattern and log level
 *
 */
public class Slog {
	
	//static String pattern="%c{1}.%M(%F:%L) %r - %m%n";
	static String pattern="%c{1}.%M(%F:%L) %r - [ %t ]: %m%n";
	
	public static void setupLogger(Logger l) {
		if(!l.getAllAppenders().hasMoreElements() ) {
			//l.setAdditivity(false);
			l.setLevel(Level.ALL);
			//l.setLevel(Level.DEBUG);
			//l.setLevel(Level.ERROR);
			l.addAppender(new ConsoleAppender(new PatternLayout(pattern)));
		}

	}
}
