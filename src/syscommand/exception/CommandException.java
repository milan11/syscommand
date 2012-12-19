package syscommand.exception;

/**
 * Base command execution exception.
 * Errors handled on command executions:
 * - invalid command exit status
 * - input/output errors
 * - thread / command running termination
 */
public class CommandException extends Exception {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates the exception with the specified message.
	 * @param message user readable message
	 */
	public CommandException(String message) {
		super(message);
	}
	
	/**
	 * Creates the exception with the specified message and original exception.
	 * @param message user readable message
	 * @param cause the original exception
	 */
	public CommandException(String message, Throwable cause) {
		super(message, cause);
	}

}
