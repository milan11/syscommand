package syscommand.internal;

import syscommand.exception.CommandException;

/**
 * Returned by methods which create and run commands to allow the caller to wait for the command end and to get the exit status.
 */
public interface CommandResultWaiting {
	
	/**
	 * Waits for command end and returns the status.
	 * @return exit status
	 * @throws CommandException on invalid exit status or some execution errors (input/output, terminating...)
	 */
	int waitAndGetResult() throws CommandException;
	
}
