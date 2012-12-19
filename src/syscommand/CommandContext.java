package syscommand;
import java.io.File;
import java.io.Writer;

import syscommand.exception.InvalidExitStatusException;

/**
 * Stores log writer, working directory and other command execution environment properties.
 * Thread safety note: Do not modify the command context while it is being used by some commands in another thread.
 */
public class CommandContext {
	
	/**
	 * Current log writer or null, if no writer is set.
	 */
	private Writer logWriter;
	
	/**
	 * If error status ignoring is currently set.
	 */
	private boolean ignoreErrorStatus = false;
	
	/**
	 * If running using sudo is set.
	 */
	private boolean sudo = false;
	
	/**
	 * Current chroot directory or null, if no execution using chroot environment was set.
	 */
	private File chrootDir = null;
	
	/**
	 * Current working directory or null, if no working directory has been set (so the working directory of the current java process is used).
	 */
	private File workingDir = null;
	
	/**
	 * Default context:
	 * - without logging
	 * - not ignoring error exit status (error status other than 0 or other valid status specified by {@link CommandBase#addValidExitStatus(int)} will cause {@link InvalidExitStatusException})
	 * - without execution using sudo (if not overridden by the specific command)
	 * - without execution in chroot (if not overridden by the specific command)
	 * - without working directory specified (working directory of the current java process will be used, if not overridden by the specific command)
	 */
	public CommandContext() {		
	}
	
	/**
	 * Creates context with loging to the specified Writer. Other options will be set to defaults as specified in {@link #CommandContext()}.
	 * @param logWriter where to write executed commands and their exit statuses
	 */
	public CommandContext(Writer logWriter) {
		this.logWriter = logWriter;
	}
	
	/**
	 * Gets current log writer.
	 * @return log writer or null if it is not set
	 */
	public Writer getLogWriter() {
		return logWriter;
	}
	
	/**
	 * Sets current log writer.
	 * @param logWriter
	 */
	public void setLogWriter(Writer logWriter) {
		this.logWriter = logWriter;
	}
	
	/**
	 * After called, the invalid command exit status (indicating an error) will always be ignored (so no exit status will cause an exception).
	 * Note that {@link SingleCommand} provides a way of specifying valid exit statuses - it can be a better solution in most cases. 
	 */
	public void beginIgnoreErrorStatus() {
		ignoreErrorStatus = true;
	}
	
	/**
	 * After called, the invalid command exit status will not be ignored anymore.
	 * Note that {@link SingleCommand} provides a way of specifying valid exit statuses, this will determine if the status was valid after this method has been called. 
	 */
	public void endIgnoreErrorStatus() {
		ignoreErrorStatus = false;
	}
	
	/**
	 * Returns if the error status ignoring is currently set.
	 * @return if the error status ignoring is currently set
	 */
	public boolean isIgnoreErrorStatus() {
		return ignoreErrorStatus;
	}
	
	/**
	 * Begins execution of commands using sudo. Note that {@link SingleCommand} can override this for its execution.
	 */
	public void beginSudo() {
		sudo = true;
	}
	
	/**
	 * Ends execution of commands using sudo. Note that {@link SingleCommand} can override this for its execution.
	 */
	public void endSudo() {
		sudo = false;
	}
	
	/**
	 * Returns if the command execution using sudo is currently set.
	 * Note that {@link SingleCommand} can override this for its execution.
	 * @return if the command execution using sudo is currently set
	 */
	public boolean isSudo() {
		return sudo;
	}
	
	/**
	 * Begins execution of commands in a chroot environment. Note that {@link SingleCommand} can override this for its execution.
	 * @param chrootDir chroot environment base directory
	 */
	public void beginChroot(File chrootDir) {
		this.chrootDir = chrootDir;
	}
	
	/**
	 * Ends execution of commands in a chroot environment. Note that {@link SingleCommand} can override this for its execution.
	 */
	public void endChroot() {
		chrootDir = null;
	}
	
	/**
	 * Get current chroot directory. Returns null if the execution of commands in a chroot environment has been disabled.
	 * @return current chroot directory or null if the execution of commands in a chroot environment has been disabled
	 */
	public File getChrootDir() {
		return chrootDir;
	}
	
	/**
	 * Begins execution of commands in the specified working directory. Note that {@link SingleCommand} can override this for its execution.
	 * @param workingDir directory to use as a working directory for the commands
	 */
	public void beginWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}
	
	/**
	 * Ends execution of commands in the explicit working directory. This causes the commands to execute in the working directory of the current java process. Note that {@link SingleCommand} can override this for its execution.
	 */
	public void endWorkingDir() {
		workingDir = null;
	}
	
	/**
	 * Get current command working directory. Returns null if the working directory is not currently set (so the working directory of the current java process is used).
	 * @return current working directory or null if it is not set
	 */
	public File getWorkingDir() {
		return workingDir;
	}
	
}
