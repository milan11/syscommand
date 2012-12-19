package syscommand;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import syscommand.exception.CommandException;
import syscommand.exception.InvalidExitStatusException;
import syscommand.internal.CommandResultWaiting;
import syscommand.internal.CommandRunningContext;
import syscommand.internal.StreamCopyRunnable;

/**
 * One command with arguments.
 */
public class SingleCommand extends CommandBase {
	
	/**
	 * Command (first string in the list) and arguments (other strings).
	 */
	private List<String> commandAndArgs = new LinkedList<String>();
	
	/**
	 * Last arguments (will be used after the argument list specified in {@link #commandAndArgs}).
	 */
	private List<String> lastArgs = new LinkedList<String>();
	
	// overriding
	/**
	 * Sudo overriding for this command. If null, sudo setting is not overridden (the sudo setting from the command context is used).
	 * If not null, it is specified by this value if this command is executed using sudo. 
	 */
	protected Boolean overrideSudo = null;
	
	/**
	 * Chroot environment override for this command.
	 * If null, chroot setting is not overridden (the chroot setting from the command context is used).
	 * If not null, chroot setting is overridden to the specified chroot directory and the chroot setting from the command context will not be used.
	 */
	protected File overrideChroot_enable = null;
	
	/**
	 * Chroot environment disabling for this command.
	 * If true, chroot environment is disabled regardless of the chroot setting from the command context.
	 */
	protected boolean overrideChroot_disable = false;
	
	/**
	 * Working directory override for this command.
	 * If null, working directory setting is not overridden (the working directory setting from the command context is used).
	 * If not null, working directory setting is overridden to the specified working directory and the working directory setting from the command context will not be used.
	 */
	protected File overrideWorkingDir_enable = null;
	
	/**
	 * Explicit working directory setting disabling for this command.
	 * If true, working directory of the current java process will be used regardless of the working directory setting from the command context.
	 */
	protected boolean overrideWorkingDir_disable = false;
	
	/**
	 * Creates new single command. The command is not yet specified and the argument list is empty.
	 */
	public SingleCommand() {
	}
	
	/**
	 * Creates new single command. The argument list is empty.
	 * @param command command string
	 */
	public SingleCommand(String command) {
		commandAndArgs.add(command);
	}
	
	/**
	 * Creates new single command with arguments.
	 * @param commandAndArgs first string is the command string, other strings are the command arguments
	 */
	public SingleCommand(String... commandAndArgs) {
		Collections.addAll(this.commandAndArgs, commandAndArgs);
	}
	
	/**
	 * Sets the command.
	 * @param command command string
	 * @return this command
	 * @throws IllegalStateException if the command has been already set (by this method or by one of the constructors which set the command)
	 */
	public SingleCommand setCommand(String command) {
		if (commandAndArgs.size() > 0) {
			throw new IllegalStateException("Command already set");
		}
		
		commandAndArgs.add(command);
		return this;
	}
	
	/**
	 * Appends the arguments to the list of arguments.
	 * @param args list of arguments being added
	 * @return this command
	 * @throws IllegalStateException if the command string has not been set (by {@link #setCommand(String)} or one of the constructors which set the command)
	 */
	public SingleCommand addArgs(String... args) {
		if (commandAndArgs.size() == 0) {
			throw new IllegalStateException("Command not set");
		}
		
		Collections.addAll(commandAndArgs, args);
		return this;
	}
	
	/**
	 * Appends the arguments in the form of one switch with parameters.
	 * Added arguments will have the following form: -name param1 param2
	 * @param name switch name
	 * @param params switch parameters
	 * @return this command
	 * @throws IllegalStateException if the command string has not been set (by {@link #setCommand(String)} or one of the constructors which set the command)
	 */
	public SingleCommand addArg_switch(String name, String... params) {
		if (commandAndArgs.size() == 0) {
			throw new IllegalStateException("Command not set");
		}
		
		commandAndArgs.add("-" + name);		
		Collections.addAll(commandAndArgs, params);
		return this;
	}
	
	/**
	 * Appends the arguments in the form of one long switch with parameters.
	 * Added arguments will have the following form: --name param1 param2
	 * @param name switch name
	 * @param params switch parameters
	 * @return this command
	 * @throws IllegalStateException if the command string has not been set (by {@link #setCommand(String)} or one of the constructors which set the command)
	 */
	public SingleCommand addArg_longSwitch(String name, String... params) {
		if (commandAndArgs.size() == 0) {
			throw new IllegalStateException("Command not set");
		}
		
		commandAndArgs.add("--" + name);		
		Collections.addAll(commandAndArgs, params);
		return this;
	}
	
	/**
	 * Appends one argument in the form of a key and a value.
	 * No characters are escaped in key or value, so you have to handle the case if the key or value contain the = character.
	 * Added argument will have the following form: key=value
	 * @param key key string
	 * @param value value string
	 * @return this command
	 * @throws IllegalStateException if the command string has not been set (by {@link #setCommand(String)} or one of the constructors which set the command)
	 */
	public SingleCommand addArg_equal(String key, String value) {
		if (commandAndArgs.size() == 0) {
			throw new IllegalStateException("Command not set");
		}
		
		commandAndArgs.add(key + "=" + value);
		return this;
	}
	
	/**
	 * Appends one argument in the form of a switch with a name and a value.
	 * No characters are escaped in key or value, so you have to handle the case if the key or the value contain the = character.
	 * Added argument will have the following form: -key=value
	 * @param name switch name
	 * @param value value string
	 * @return this command
	 * @throws IllegalStateException if the command string has not been set (by {@link #setCommand(String)} or one of the constructors which set the command)
	 */
	public SingleCommand addArg_switchEqual(String name, String value) {
		if (commandAndArgs.size() == 0) {
			throw new IllegalStateException("Command not set");
		}
		
		commandAndArgs.add("-" + name + "=" + value);
		return this;
	}
	
	/**
	 * Appends one argument in the form of a long switch with a name and a value.
	 * No characters are escaped in key or value, so you have to handle the case if the name or the value contain the = character.
	 * Added argument will have the following form: --name=value
	 * @param name switch name
	 * @param value value string
	 * @return this command
	 * @throws IllegalStateException if the command string has not been set (by {@link #setCommand(String)} or one of the constructors which set the command)
	 */
	public SingleCommand addArg_longSwitchEqual(String name, String value) {
		if (commandAndArgs.size() == 0) {
			throw new IllegalStateException("Command not set");
		}
		
		commandAndArgs.add("--" + name + "=" + value);
		return this;
	}
	
	/**
	 * Adds an argument to the end of the group of arguments which are to be specified as last arguments. 
	 * @param arg argument to add to the group of last arguments
	 * @return this command
	 */
	public SingleCommand addLastArg(String arg) {
		lastArgs.add(arg);
		return this;
	}
	
	/**
	 * Enables command execution using sudo. This command will be executed using sudo regardless of the sudo setting in the command context.
	 * @return this command
	 */
	public CommandBase overrideSudo_enable() {
		overrideSudo = true;
		return this;
	}
	
	/**
	 * Disables command execution using sudo. This command will not be executed using sudo regardless of the sudo setting in the command context.
	 * @return this command
	 */
	public CommandBase overrideSudo_disable() {
		overrideSudo = false;
		return this;
	}
	
	/**
	 * Enables chroot. The specified chroot directory will be used regardless of the chroot setting in the command context.
	 * @param chrootDir chroot directory to use
	 * @return this command
	 */
	public CommandBase overrideChroot_enable(File chrootDir) {
		overrideChroot_enable = chrootDir;
		overrideChroot_disable = false;
		return this;
	}
	
	/**
	 * Disables chroot. Chroot will not be used regardless of the chroot setting in the command context.
	 * @return this command
	 */
	public CommandBase overrideChroot_disable() {
		overrideChroot_enable = null;
		overrideChroot_disable = true;
		return this;
	}
	
	/**
	 * Overrides working directory.
	 * @param workingDir working directory to use regardless of the working directory setting in the command context
	 * @return this command
	 */
	public CommandBase overrideWorkingDir_enable(File workingDir) {
		overrideWorkingDir_enable = workingDir;
		overrideWorkingDir_disable = false;
		return this;
	}
	
	/**
	 * Disables explicit working directory specification. The working directory of current java process will be used regardless of the working directory setting in the command context.
	 * @return this command
	 */
	public CommandBase overrideWorkingDir_disable() {
		overrideWorkingDir_enable = null;
		overrideWorkingDir_disable = true;
		return this;
	}
	
	/**
	 * Single command creation and running.
	 * @param context command context used for getting logging and execution environment properties (if not overridden by this command settings) 
	 * @return command running context used to access input and output streams and to wait for the command to exit
	 * @throws CommandException if the command creating or running failed (this include input/output exceptions, invalid exit status, terminating etc.)
	 */
	@Override
	protected CommandRunningContext run(final CommandContext context) throws CommandException {
		List<String> l = new LinkedList<String>();
		
		final boolean useSudo = (overrideSudo != null) ? overrideSudo : context.isSudo();
		final File chrootDir = (overrideChroot_disable) ? null : ((overrideChroot_enable != null) ? overrideChroot_enable : context.getChrootDir());
		final File workingDir = (overrideWorkingDir_disable) ? null : ((overrideWorkingDir_enable != null) ? overrideWorkingDir_enable : context.getWorkingDir());
		
		if (useSudo) {
			l.add("sudo");
		}
		
		if (chrootDir != null) {
			l.add("sudo");
			l.add("chroot");
			l.add(chrootDir.getPath());
		}
		
		l.addAll(commandAndArgs);
		l.addAll(lastArgs);
		
		ProcessBuilder processBuilder = new ProcessBuilder(l);
		if (workingDir != null) {
			processBuilder.directory(workingDir);
		}
		
		Process process = null;
		try {
			process = processBuilder.start();
		} catch (IOException e) {
			throw new CommandException("Unable to start process builder", e);
		}
		
		final Process process_f = process;
		
		CommandRunningContext result = new CommandRunningContext();
		result.stdin = process_f.getOutputStream();
		result.stdout = process_f.getInputStream();
		
		// stderr
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StreamCopyRunnable r = new StreamCopyRunnable(process_f.getErrorStream(), baos);
		new Thread(r).start();
		
		final CommandBase self = this;
		result.res = new CommandResultWaiting() {
			
			@Override
			public int waitAndGetResult() throws CommandException {
				int exitStatus;
				try {
					exitStatus = process_f.waitFor();
				} catch (InterruptedException e) {
					throw new CommandException("Thread interrupted", e);
				}
				
				try {
					Writer logWriter = context.getLogWriter();
					if (logWriter != null) {
						synchronized(logWriter) {
							context.getLogWriter().write("----------------------------\n");
							context.getLogWriter().write("  COMMAND: " + self.toString() + '\n');
							if (useSudo) {
								context.getLogWriter().write("  WITH SUDO" + '\n');
							}
							if (chrootDir != null) {
								context.getLogWriter().write("  WITH CHROOT: " + chrootDir.getPath() + '\n');
							}
							context.getLogWriter().write("  RETURNS: " + exitStatus + '\n');
							context.getLogWriter().write("-------\n");
							context.getLogWriter().write(new String(baos.toByteArray(), "UTF-8"));
							context.getLogWriter().write("-------\n");
						}
					}
				} catch (IOException e) {
					// nothing
				}
				
				if (! validExitStatuses.contains(exitStatus)) {
					if (! context.isIgnoreErrorStatus()) {
						throw new InvalidExitStatusException(exitStatus);
					}
				}
				
				return exitStatus;
			}
			
		};
		
		return result;
	}
	
	/**
	 * Gets the command with its arguments as a string. The command and arguments are delimited by spaces.
	 * @return command description string
	 */
	@Override
	public String toString() {
		List<String> l = new LinkedList<String>();
				
		l.addAll(commandAndArgs);
		l.addAll(lastArgs);
		
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String s : l) {
			if (first) {
				first = false;
			} else {
				sb.append(' ');
			}
			sb.append(s);
		}
		return sb.toString();
	}

}
