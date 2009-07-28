package jcu.sal.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import jcu.sal.common.Slog;

import org.apache.log4j.Logger;

public class PlatformHelper {
	
	private static Logger logger = Logger.getLogger(PlatformHelper.class);
	static {
		Slog.setupLogger(logger);
	}

	/**
	 * Tries unloading a module. Note that this method will work only 
	 * if the user has right to run "/usr/bin/sudo /sbin/rmmod". This method
	 * checks that the module is loaded before 
	 * @param module the name of the module to be removed
	 * @return whether the operation succeeded
	 */
	public static boolean rmModule(String module){
		boolean ok=true;
		Process p = null;
		if(PlatformHelper.isModuleLoaded(module)) {
			try {
				p = createProcess("/usr/bin/sudo /sbin/rmmod " + module);
				if(p.waitFor()!=0) {
					String err = null;
					BufferedReader b = new BufferedReader(new InputStreamReader(p.getErrorStream()));
					while ((err=b.readLine())!=null)				
						logger.error(err);
					logger.error("Return status of rmmod !=0 ");
					ok=false;
					b.close();
				}
			} catch (InterruptedException e) {
				logger.error("Interrupted while waiting for rmmod to complete");
				ok=false;
			} catch (IOException e) {
				logger.error("cant run the rmmod command");
				ok=false;
			} finally {
				if(p!=null) p.destroy();
			}
		} //else
			//logger.debug("Module " + module + " not loaded, cant remove it...");
		return ok;
	}
	
	/**
	 * Check if a single module is loaded 
	 * @param module the name of the module
	 */
	public static boolean isModuleLoaded(String module){
		try {
			PlatformHelper.getFieldFromFile("/proc/modules", module, 1, null, false);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Tries loading a single module, assuming the user has enough rights to run
	 * "/usr/bin/sudo /sbin/modprobe". This function checks if the module 
	 * is already loaded before.
	 * @param module the name of the module to be loaded
	 * @return whether the operation succeeded
	 */
	public static boolean loadModule(String module){
		boolean ok=true;
		Process p = null;
		if(!PlatformHelper.isModuleLoaded(module)) {
			try {
				p = createProcess("/usr/bin/sudo /sbin/modprobe " + module);
				if(p.waitFor()!=0) {
					String err = null;
					BufferedReader b = new BufferedReader(new InputStreamReader(p.getErrorStream()));
					while ((err=b.readLine())!=null)				
						logger.error(err);
					logger.error("Return status of modprobe !=0 ");
					ok=false;
					b.close();
				}
			} catch (InterruptedException e) {
				logger.error("Interrupted while waiting for moprobed to complete");
				ok=false;
			} catch (IOException e) {
				logger.error("cant run the modprobe command");
				ok=false;
			} finally {
				if (p!=null) p.destroy();
			}
		} //else
			//logger.debug("Module " + module + " already loaded");
		return ok;
	}
	
	/**
	 * Tries loading a couple of modules, assuming the user has enough rights to run
	 * "/usr/bin/sudo /sbin/modprobe". This function checks if the modules 
	 * are already loaded before.
	 * @param module an array of string containing the names of modules to be loaded
	 * @return whether something went wrong loading one of the modules
	 */
	public static boolean loadModules(String[] modules) {
		boolean ok=true;
		for (int i = 0; i < modules.length; i++) {
			if(!PlatformHelper.loadModule(modules[i]))
				ok=false;
		}
		return ok;
	}
	
	/**
	 * Tries unloading a couple of modules, assuming the user has enough rights to run
	 * "/usr/bin/sudo /sbin/rmmod". This function checks if the modules 
	 * are already loaded before.
	 * @param module an array of string containing the names of modules to be unloaded
	 * @return whether something went wrong unloading one of the modules
	 */
	public static boolean unloadModules(String[] modules){
		boolean ok=true;
		for (int i = 0; i < modules.length; i++) {
			if(!PlatformHelper.rmModule(modules[i]))
				ok=false;
		}
		return ok;
	}
	
	/**
	 * Creates a new instance of a process from a command line
	 * @param cmdline the command to be run with any arguments it needs
	 * @return the new process
	 * @throws IOException if there is a problem creating the new process
	 */
	public static Process createProcess(String cmdline) throws IOException {
		return Runtime.getRuntime().exec(cmdline);
	}
	
	/**
	 * captures the standard output and error channel of a command. If the wait argument is true, 
	 * then this method also waits for the process to exit and will return its exit status 
	 * @param cmdline the command to be run with any arguments it needs
	 * @return an array of 2 or 3 BufferedReaders [0]: stdout, [1]: stderr,
	 * [2]: exit value (only if wait = true)
	 * @throws IOException if there is a problem creating the new process
	 */
	public static ProcessOutput captureOutputs(String cmdline, boolean wait) throws IOException {
		BufferedReader[] b = new BufferedReader[3];
		Process p = PlatformHelper.createProcess(cmdline);
		b[0] = new BufferedReader(new InputStreamReader(p.getInputStream()));
		b[1] = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		if(wait){
			try {
				b[2] = new BufferedReader(new StringReader(String.valueOf(p.waitFor())));
			} catch (InterruptedException e) {}
		}
		return new ProcessOutput(b, p);
	}
	
	/**
	 * Gets the PID(s) of a process(es) whose name(s) matches a given pattern
	 * @param pattern the pattern
	 * @return the PIDs
	 * @throws IOException 
	 */
	public static List<Integer> getPid(String pattern) throws IOException {
		List<Integer> pids = new ArrayList<Integer>();		
		Process p = null;

		p = createProcess("pgrep " + pattern);
		try { p.waitFor(); } catch (InterruptedException e) {}
		BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
		while(b.ready()) { pids.add(Integer.parseInt(b.readLine())); }
		p.destroy();
		return pids;
		
	}
	
	/**
	 * Kill processes whose names match a given pattern
	 * @param pattern the pattern
	 * @return the number of processes killed
	 */
	public static int killProcesses(String pattern) {
		int n = 0;
		Process p = null;
		try {
			n = getPid(pattern).size()-1;
			p = createProcess("pkill " + pattern);
			p.waitFor();
		} catch (IOException e) {
			logger.error("cant run the pkill command");
		} catch (InterruptedException e) {
			logger.error("interrupted while running the pkill command");
		} finally {
			if(p!=null) p.destroy();
		}
		
		return n;
	}
	
	/**
	 * Check if a process is currently running and return its arguments if it is running.
	 * If more than one process matches the pattern, the return value contains the
	 * arguments of all matching processes
	 * @param pattern the pattern used to match processes
	 * @return the arguments
	 */
	public static Hashtable<String,String> getRunningProcessArgs(String pattern)  {
		Hashtable<String,String> args = new Hashtable<String,String>();  
		List<Integer> pids;
		BufferedReader b = null;
		int instance = 0;

		try {
			pids = getPid(pattern);
			Iterator<Integer> iter = pids.iterator();
			while(iter.hasNext()) {
				//reads /proc/pid/cmdline and add it to args
				b =  new BufferedReader(new FileReader("/proc/" + String.valueOf(iter.next()) + "/cmdline"));
				String[] t = b.readLine().split("\00");
				String name = t[0] + "(instance " + String.valueOf(++instance) + ")";
				for (int i = 1; i < t.length; i++) 
					args.put(name, t[i]);
			}

		} catch (IOException e) {
			logger.error("Error getting process args");
			e.printStackTrace();
		}
		return args;
	}

	/**
	 * Returns a field from a specific line in a text file
	 * @param file the file to be searched
	 * @param pattern the pattern in the line to be searched
	 * @param field the field number whose contents is to be returned  (starting at 1)
	 * @param delim the delimiter (if null, then a space is assumed)
	 * @param translate whether to translate tabs to spaces
	 * @return the field itself
	 * @throws IOException if something went wrong opening the file, reading from it or finding the pattern
	 */
	public static String getFieldFromFile(String file, String pattern, int field, String delim, boolean translate) throws IOException {
		return getFieldFromBuffer(new BufferedReader(new FileReader(new File(file))), pattern, field, delim, translate); 
	}
	
	/**
	 * Returns a field from a specific line in a text file
	 * @param file the file to be searched
	 * @param line the line number (starting at 1)
	 * @param field the field number whose contents is to be returned  (starting at 1)
	 * @param delim the delimiter (if null, then a space is assumed)
	 * @param translate whether to translate tabs to spaces
	 * @return the field itself
	 * @throws IOException if something went wrong opening the file, reading from it or finding the pattern
	 */
	public static String getFieldFromFile(String file, int line , int field, String delim, boolean translate) throws IOException {
		return getFieldFromBuffer(new BufferedReader(new FileReader(new File(file))), line, field, delim, translate); 
	}
	
	/**
	 * Returns a field from a specific line in the output of a command
	 * @param file the file to be searched
	 * @param pattern the pattern to be searched for in each line
	 * @param field the field number whose contents is to be returned  (starting at 1)
	 * @param delim the delimiter (if null, then a space is assumed)
	 * @param translate whether to translate tabs to spaces
	 * @return the field itself
	 * @throws IOException
	 */
	public static String getFieldFromCommand(String cmd, String pattern, int field, String delim, boolean translate) throws IOException {
		ProcessOutput c = captureOutputs(cmd, true);
		String ret = getFieldFromBuffer(c.getBuffers()[0], pattern, field, delim, translate);
		c.destroyProcess();
		return ret;
	}

	/**
	 * Returns a field from a specific line in the output of a command
	 * @param file the file to be searched
	 * @param pattern the pattern in the line to be searched
	 * @param field the field number whose contents is to be returned  (starting at 1)
	 * @param delim the delimiter (if null, then a space is assumed)
	 * @param translate whether to translate tabs to spaces
	 * @return the field itself
	 * @throws IOException
	 */
	public static String getFieldFromCommand(String cmd, int line, int field, String delim, boolean translate) throws IOException {
		ProcessOutput c = captureOutputs(cmd, true);
		String ret = getFieldFromBuffer(c.getBuffers()[0], line, field, delim, translate);
		c.destroyProcess();
		return ret;
	}
	
	/**
	 * Returns a field from a specific line in a BufferedReader
	 * @param b the buffer to be searched
	 * @param pattern the pattern in the line to be searched
	 * @param field the field number whose contents is to be returned  (starting at 1)
	 * @param delim the delimiter (if null, then a space is assumed)
	 * @param translate whether to translate tabs to spaces
	 * @return the field itself
	 * @throws IOException
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public static String getFieldFromBuffer(BufferedReader b, String pattern, int field, String delim, boolean translate) throws IOException, ArrayIndexOutOfBoundsException{
		String result = "";
		
		/* find matching line */
		if(pattern != null) {
			while((result  = b.readLine())!= null) 
				if(result.contains(pattern)) break; 
		} else
			result  = b.readLine();
		
		if(result!=null) {
			return getFieldFromLine(result, field, delim, translate);
		}

		throw new IOException();
	}
	
	/**
	 * Returns an array of fields from  specific lines in a BufferedReader given a pattern
	 * @param b the buffer to be searched
	 * @param pattern the pattern in the line to be searched
	 * @param field the field number whose contents is to be returned  (starting at 1)
	 * @param delim the delimiter (if null, then a space is assumed)
	 * @param translate whether to translate tabs to spaces
	 * @return the field itself
	 * @throws IOException
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public static ArrayList<String> getFieldsFromBuffer(BufferedReader b, String pattern, int field, String delim, boolean translate) throws IOException, ArrayIndexOutOfBoundsException{
		String temp = "";
		ArrayList<String> result = new ArrayList<String>();
		
		/* find matching line */
		while((temp  = b.readLine())!= null) 
			if(temp.contains(pattern))
				result.add(getFieldFromLine(temp, field, delim, translate));
		return result;
	}
	
	
	/**
	 * Returns a field from a specific line in a BufferedReader
	 * @param b the buffer to be searched
	 * @param line the line number 
	 * @param field the field number whose contents is to be returned (starting at 1)
	 * @param delim the delimiter (if null, then a space is assumed)
	 * @param translate whether to translate tabs to spaces
	 * @return the field itself
	 * @throws IOException if the line number is set to 0, or is greater than the number of lines in the buffer
	 */
	public static String getFieldFromBuffer(BufferedReader b, int line, int field, String delim, boolean translate) throws IOException {
		if(line<=0)
			throw new IOException("Invalid line number");
		
		String result=null;
		
		/* find  line */
		while( (line-- > 0) && ((result  = b.readLine())!= null) ); 

		
		if(result!=null) {
			result =  getFieldFromLine(result, field, delim, translate);
			return result;
		}
		else {
			throw new IOException("Read past the end of buffer");
		}
	}
	
	/**
	 * Returns a field from a line
	 * @param line the line to be searched 
	 * @param field the field number whose contents is to be returned (starting at 1)
	 * @param delim the delimiter (if null, then a space is assumed)
	 * @param translate whether to translate tabs to spaces
	 * @return the field
	 */
	public static String getFieldFromLine(String line, int field, String delim, boolean translate) throws ArrayIndexOutOfBoundsException{
		StringBuilder sb = new StringBuilder();
		
		/* Translate tabs to space if needed */
		if(translate)
			line = line.replace("\t", " ");

		/* remove multiple spaces */
		char prev = line.charAt(0);
		sb.append(prev);
		for(int i = 1; i<line.length(); i++) {
			if((line.charAt(i) != prev) || (prev != ' ')) {
				prev = line.charAt(i);
				sb.append(prev);
			}
		}
		line = sb.toString().trim();

		if(delim == null)
			delim = " ";

		/* Get the required field */
		return line.split(delim)[field-1];
	}
	
	/**
	 * This method read the entire first line from a file and returns it
	 * @param f the file to be read
	 * @return the line read
	 * @throws IOException if an error occurs
	 */
	public static String readFromFile (String f) throws IOException{
		return getFieldFromFile(f, 1, 1, null, false);
	}
	
	/**
	 * Returns whether a file exists and is readable
	 * @param f the full path to the file
	 * @return whether the file exists and is readable
	 */
	public static boolean isFileReadable(String f){
		return new File(f).canRead();
	}
	
	/**
	 * Returns whether a file exists and is a file (not a directory)
	 * @param f the full path to the file
	 * @return whether the file exists
	 */
	public static boolean isFile(String f){
		return new File(f).exists() && !isDir(f);
	}
	
	/**
	 * Returns whether a directory exists and is readable
	 * @param d the full path to the directory
	 * @return whether the file exists and is readable
	 */
	public static boolean isDirReadable(String d){
		File f = new File(d);
		return (f.isDirectory() && f.canRead());
	}
	
	/**
	 * Returns whether a directory exists, is readable and writeable
	 * @param d the full path to the directory
	 * @return whether the file exists and is readable
	 */
	public static boolean isDirReadWrite(String d){
		File f = new File(d);
		return (f.isDirectory() && f.canRead() && f.canWrite());
	}
	
	/**
	 * Returns whether a directory exists
	 * @param d the full path to the directory
	 * @return whether the file exists and is readable
	 */
	public static boolean isDir(String d){
		File f = new File(d);
		return (f.isDirectory());
	}
	
	public static class ProcessOutput {
		BufferedReader[] b;
		Process p;
		public ProcessOutput(BufferedReader[] b, Process p){
			this.b = b;
			this.p = p;
		}
		public BufferedReader[] getBuffers() {
			return b;
		}
		public void destroyProcess() {
			try { p.getInputStream().close();}
			catch (IOException e) {}
			try { p.getOutputStream().close();}
			catch (IOException e) {}
			try { p.getErrorStream().close();}
			catch (IOException e) {}
			p.destroy();
		}	
	}
}
