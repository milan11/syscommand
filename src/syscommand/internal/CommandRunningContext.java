package syscommand.internal;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Holds objects for interaction with the running command (streams and an object for waiting for the command to exit)
 */
public class CommandRunningContext {
	
	/**
	 * Output stream to write data to the process (to its standard input).
	 */
	public OutputStream stdin;
	
	/**
	 * Input stream to read data from the process (from its standard output).
	 */
	public InputStream stdout;
	
	/**
	 * Object used for waiting for the command to exit and to get its exit status.
	 */
	public CommandResultWaiting res;
	
}
