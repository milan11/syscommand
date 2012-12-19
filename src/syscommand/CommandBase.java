package syscommand;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import syscommand.exception.CommandException;
import syscommand.internal.CommandRunningContext;
import syscommand.internal.NullOutputStream;
import syscommand.internal.StreamCopyRunnable;

/**
 * Base class of all commands.
 */
public abstract class CommandBase {
	
	/**
	 * Stream providing input data for the command.
	 */
	private InputStream input = null;
	
	/**
	 * Set of exit statuses which do not cause an exception. Filled with 0 value in constructor.
	 */
	protected Set<Integer> validExitStatuses = new HashSet<Integer>();
	
	/**
	 * Creates command base and sets 0 as one of the valid exit statuses.
	 */
	public CommandBase() {
		validExitStatuses.add(0);
	}
	
	/**
	 * Adds new exit status which has to be considered valid (so no exception will be thrown if this command returns such status).
	 * @param status valid exit status
	 * @return this command
	 */
	public CommandBase addValidExitStatus(int status) {
		validExitStatuses.add(status);
		return this;
	}
	
	/**
	 * Sets a string to be used as the input data for the command.
	 * @param s string to create input data from (will be converted to bytes using UTF-8 encoding)
	 * @return this command
	 * @throws IllegalStateException if the input has been already set
	 */
	public CommandBase input_string(String s) {
		if (input != null) {
			throw new IllegalStateException("Input already set");
		}
		
		byte[] bytes = null;
		try {
			bytes = s.getBytes("UTF-8");
		} catch (UnsupportedEncodingException u) {
			throw new RuntimeException(u);
		}
		input = new ByteArrayInputStream(bytes);
		return this;
	}
	
	/**
	 * Runs the command, waits for exit and ignores its output (data written to the standard output).
	 * @param context command context with logging and execution environment settings
	 * @throws CommandException if the command creating or running failed (this include input/output exceptions, invalid exit status, terminating etc.)
	 */
	public void run_noout(CommandContext context) throws CommandException {
		run_noout_internal(context);
	}
	
	/**
	 * Runs the command, waits for exit and returns its output (data written to the standard output) as bytes.
	 * @param context command context with logging and execution environment settings
	 * @return output data
	 * @throws CommandException if the command creating or running failed (this include input/output exceptions, invalid exit status, terminating etc.)
	 */
	public byte[] run_raw(CommandContext context) throws CommandException {
		return run_toBytes_internal(context);
	}
	
	/**
	 * Runs the command, waits for exit and returns its output (data written to the standard output) as a string.
	 * @param context command context with logging and execution environment settings
	 * @return output data as string (output bytes are converted using UTF-8 encoding to string) 
	 * @throws CommandException if the command creating or running failed (this include input/output exceptions, invalid exit status, terminating etc.)
	 */
	public String run_rawstr(CommandContext context) throws CommandException {
		try {
			return new String(run_toBytes_internal(context), "UTF-8");
		} catch (UnsupportedEncodingException u) {
			throw new RuntimeException(u);
		}
	}
	
	/**
	 * Runs the command, waits for exit and returns its output (data written to the standard output) as a string array representing lines of the output.
	 * @param context command context with logging and execution environment settings
	 * @return output data as a string array (output bytes are converted using UTF-8 encoding to strings, strings are delimited by the newline character (\n, 10, 0xA) 
	 * @throws CommandException if the command creating or running failed (this include input/output exceptions, invalid exit status, terminating etc.)
	 */
	public String[] run_lines(CommandContext context) throws CommandException {
		try {
			return new String(run_toBytes_internal(context), "UTF-8").split("[\\n]+");
		} catch (UnsupportedEncodingException u) {
			throw new RuntimeException(u);
		}
	}
	
	/**
	 * Runs the command, waits for exit and returns its output (data written to the standard output) as a string array representing parts of the output separated by the null character.
	 * @param context command context with logging and execution environment settings
	 * @return output data as a string array (output bytes are converted using UTF-8 encoding to strings, strings are delimited by the null character (char. code 0) 
	 * @throws CommandException if the command creating or running failed (this include input/output exceptions, invalid exit status, terminating etc.)
	 */
	public String[] run_nullSeparated(CommandContext context) throws CommandException {
		String str = null;
		try {
			str = new String(run_toBytes_internal(context), "UTF-8");
		} catch (UnsupportedEncodingException u) {
			throw new RuntimeException(u);
		}
		
		if (str.length() > 0) {
			return str.split("[\\x{0}]+");
		} else {
			return new String[0];
		}
	}
	
	/**
	 * Runs the command, waits for exit and returns its output (data written to the standard output) as a string with whitespace characters at the beginning and end removed.
	 * @param context command context with logging and execution environment settings
	 * @return output data as string (output bytes are converted using UTF-8 encoding to string, whitespace characters at the beginning and end are removed)
	 * @throws CommandException if the command creating or running failed (this include input/output exceptions, invalid exit status, terminating etc.)
	 */
	public String run_str(CommandContext context) throws CommandException {
		try {
			return new String(run_toBytes_internal(context), "UTF-8").trim();
		} catch (UnsupportedEncodingException u) {
			throw new RuntimeException(u);
		}
	}
	
	/**
	 * Runs the command, waits for exit and returns its output (data written to the standard output) as a long value.
	 * @param context command context with logging and execution environment settings
	 * @return output data as a long value
	 * @throws CommandException if the command creating or running failed (this include input/output exceptions, invalid exit status, terminating etc.)
	 * @throws NumberFormatException if the output was not a valid long value
	 */
	public long run_long(CommandContext context) throws CommandException {
		try {
			return Long.parseLong(new String(run_toBytes_internal(context), "UTF-8").trim());
		} catch (UnsupportedEncodingException u) {
			throw new RuntimeException(u);
		}
	}
	
	/**
	 * Runs the command, waits for its exit while writing its output to a file. If the file already exists, it will be overwritten.
	 * @param context command context with logging and execution environment settings
	 * @param file file to write the command output to
	 * @throws CommandException if the command creating or running failed (this include input/output exceptions, invalid exit status, terminating etc.)
	 */
	public void run_writeTo(CommandContext context, File file) throws CommandException {
		run_toFile_internal(context, file, false);
	}
	
	/**
	 * Runs the command, waits for its exit while appending its output to a file. If the file already exists, the new data will be appended to the original data contained in the file.
	 * @param context command context with logging and execution environment settings
	 * @param file file to append the command output to
	 * @throws CommandException if the command creating or running failed (this include input/output exceptions, invalid exit status, terminating etc.)
	 */
	public void run_appendTo(CommandContext context, File file) throws CommandException {
		run_toFile_internal(context, file, true);
	}
	
	/**
	 * Runs the command and waits for exit. Ignores the command output.
	 * @param context command context with logging and execution environment settings
	 * @throws CommandException if the command creating or running failed (this include input/output exceptions, invalid exit status, terminating etc.)
	 */
	private void run_noout_internal(CommandContext context) throws CommandException {
		OutputStream os = null;
		try {
			os = new NullOutputStream();
			run_internal(context, os);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (Throwable t) {
					// nothing
				}
			}
		}
	}
	
	/**
	 * Runs the command and waits for exit. Returns the command output as a byte array.
	 * @param context command context with logging and execution environment settings
	 * @throws CommandException if the command creating or running failed (this include input/output exceptions, invalid exit status, terminating etc.)
	 */
	private byte[] run_toBytes_internal(CommandContext context) throws CommandException {
		ByteArrayOutputStream os = null;
		try {
			os = new ByteArrayOutputStream();
			run_internal(context, os);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (Throwable t) {
					// nothing
				}
			}
		}
		return os.toByteArray();
	}
	
	/**
	 * Runs the command, waits for its exit while writing the output to a file.
	 * @param context command context with logging and execution environment settings
	 * @param file file to write the command output to
	 * @param append if the new data have to be appended to the original file contents (if false, the file will be overwritten if it exists)
	 * @throws CommandException if the command creating or running failed (this include input/output exceptions, invalid exit status, terminating etc.)
	 */
	private void run_toFile_internal(CommandContext context, File file, boolean append) throws CommandException {
		OutputStream os = null;
		try {
			os = new FileOutputStream(file, append);
			run_internal(context, os);
		} catch (FileNotFoundException e) {
			throw new CommandException("Output file not found: " + file, e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (Throwable t) {
					// nothing
				}
			}
		}
	}
	
	/**
	 * Runs the command, waits for its exit while writing the output to a output stream.
	 * @param context command context with logging and execution environment settings
	 * @param os output stream to write the command output to
	 * @throws CommandException if the command creating or running failed (this include input/output exceptions, invalid exit status, terminating etc.)
	 */
	private void run_internal(CommandContext context, OutputStream os) throws CommandException {
		CommandRunningContext current = run(context);
		
		List<StreamCopyRunnable> copyRunnables = new ArrayList<StreamCopyRunnable>(2);
		List<Thread> copyRunnableThreads = new ArrayList<Thread>(2);
		
		if (input != null) {
			// from input to process
			StreamCopyRunnable r = new StreamCopyRunnable(input, current.stdin);
			copyRunnables.add(r);
			Thread t = new Thread(r);
			copyRunnableThreads.add(t);
			t.start();
		}
		
		{
			// from process to output
			StreamCopyRunnable r = new StreamCopyRunnable(current.stdout, os);
			copyRunnables.add(r);
			Thread t = new Thread(r);
			copyRunnableThreads.add(t);
			t.start();
		}
		
		for (Thread t : copyRunnableThreads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				throw new CommandException("Thread interrupted", e);
			}
		}
		
		for (StreamCopyRunnable r : copyRunnables) {
			
			if (! r.isSuccess()) {
				throw new CommandException("Error while copying data");
			}
		}
		
		current.res.waitAndGetResult();		
	}
	
	/**
	 * A method overriding this must:
	 * - create the system command
	 * - run the system command
	 * - return command running context for the command
	 * A method overriding this must not:
	 * - wait for the command to exit
	 * @param context command context with logging and environment properties which have to be used when creating and running the command
	 * @return command running context used to access input and output streams and to wait for the command to exit
	 * @throws CommandException if the command creating or running failed (this include input/output exceptions, invalid exit status, terminating etc.)
	 */
	protected abstract CommandRunningContext run(CommandContext context) throws CommandException;
	
	/**
	 * Creates a new pipeline which includes this command and the command specified as an argument. Next commands can be added to the created pipeline using the {@link Pipeline#add(CommandBase)} {@link Pipeline#add(String...)} method.
	 * @param c second command of the pipeline
	 * @return new created pipeline
	 */
	public Pipeline pipe(CommandBase c) {
		Pipeline p = new Pipeline();
		p.add(this);
		p.add(c);
		return p;
	}
	
	/**
	 * Creates a new pipeline which includes this command and the command with arguments specified as an argument. This is a helper method doing the same as {@link CommandBase#pipe(CommandBase)} without the need to construct the {@link SingleCommand}).
	 * @param commandAndArgs command string and arguments strings (each argument is one string)
	 * @return new created pipeline
	 */
	public Pipeline pipe(String... commandAndArgs) {
		Pipeline p = new Pipeline();
		p.add(this);
		p.add(new SingleCommand(commandAndArgs));
		return p;
	}
	
}
