package syscommand.exception;

import syscommand.CommandBase;

/**
 * Thrown when an executed command exited with invalid exit status (not 0 or some exit status specified as valid by {@link CommandBase#addValidExitStatus(int)})
 */
public class InvalidExitStatusException extends CommandException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Exit status returned by the process.
	 */
	private int status;
	
	/**
	 * Creates the exception with the information about the exit status.
	 * @param status exit status returned by the process
	 */
	public InvalidExitStatusException(int status) {
		super("Invalid exit status: " + status);
		this.status = status;
	}
	
	/**
	 * Gets the exit status returned by the process.
	 * @return exit status
	 */
	public int getStatus() {
		return status;
	}

}
