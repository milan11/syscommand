package syscommand;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import syscommand.exception.CommandException;
import syscommand.exception.InvalidExitStatusException;
import syscommand.internal.CommandResultWaiting;
import syscommand.internal.CommandRunningContext;
import syscommand.internal.StreamCopyRunnable;

/**
 * Command which connects all its subcommands (output of a command to an input of the next command),
 * executes all the subcommands at once.
 * The exit status of the last command is considered as an exit status of the pipeline.
 */
public class Pipeline extends CompositeCommand {
	
	/**
	 * Adds a subcommand to the end of the pipeline.
	 * @param command some command
	 * @return this pipeline
	 */
	public Pipeline add(CommandBase command) {
		list.add(command);
		return this;
	}
	
	/**
	 * Adds a command with arguments specified by strings to the pipeline. This is a helper method doing the same as {@link #add(CommandBase)} without the need to construct the {@link SingleCommand}.
	 * @param commandAndArgs command string and arguments strings (each argument is one string)
	 * @return this pipeline
	 */
	public Pipeline add(String... commandAndArgs) {
		list.add(new SingleCommand(commandAndArgs));
		return this;
	}
	
	/**
	 * Connects the subcommands with streams and executes them.
	 * @param context command context used for getting logging and execution environment properties (note that each {@link SingleCommand} which is a subcommand of the pipeline can override these properties for its own execution) 
	 * @return command running context used to access streams (input stream of the first subcommand and output stream of the last subcommand) and to wait for the pipeline to exit
	 * @throws CommandException if the command creating or running failed (this include input/output exceptions, invalid exit status, terminating etc.)
	 */
	@Override
	protected CommandRunningContext run(final CommandContext context) throws CommandException {		
		CommandRunningContext result = new CommandRunningContext();
		
		int count = list.size();
		
		final List<CommandRunningContext> processes = new ArrayList<CommandRunningContext>(count);
		
		final List<StreamCopyRunnable> copyRunnables = new ArrayList<StreamCopyRunnable>(count * 2);
		final List<Thread> copyRunnableThreads = new ArrayList<Thread>(count * 2);
		
		CommandRunningContext previous = null;
		for (CommandBase command : list) {
			CommandRunningContext current = command.run(context);
			
			if (previous == null) {
				result.stdin = current.stdin;
			}
			
			if (previous != null) {
				// between processes
				StreamCopyRunnable r = new StreamCopyRunnable(previous.stdout, current.stdin);
				copyRunnables.add(r);
				Thread t = new Thread(r);
				copyRunnableThreads.add(t);
				t.start();
			}
			
			processes.add(current);
			
			previous = current;
		}
		
		result.stdout = previous.stdout;
		
		final CommandBase self = this;
		result.res = new CommandResultWaiting() {
			
			@Override
			public int waitAndGetResult() throws CommandException {
				try {
					Writer logWriter = context.getLogWriter();
					if (logWriter != null) {
						synchronized(logWriter) {
							logWriter.write("----------------------------\n");
							logWriter.write("PIPELINE: " + self.toString() + '\n');
						}
					}
				} catch (IOException e) {
					// nothing
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
				
				int lastStatus = 0;
				boolean wasException = false;
				for (int i = 0; i < processes.size(); ++i) {
					CommandRunningContext p = processes.get(i);
					
					try {
						lastStatus = p.res.waitAndGetResult();
					} catch (Exception ex) {
						wasException = true;
					}
				}
				
				if (wasException) {
					throw new CommandException("One or more commands in the pipeline failed");
				}
				
				if (! validExitStatuses.contains(lastStatus)) {
					if (! context.isIgnoreErrorStatus()) {
						throw new InvalidExitStatusException(lastStatus);
					}
				}
				
				return lastStatus;
			}
		};
			
		return result;
	}
	
	/**
	 * Returns the pipeline description as a string. The strings contains string descriptions for all the subcommands delimited by the | character.
	 * @return pipeline description string
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (CommandBase c : list) {
			if (first) {
				first = false;
			} else {
				sb.append(" | ");
			}
			sb.append(c.toString());
		}
		return sb.toString();
	}
	
}
