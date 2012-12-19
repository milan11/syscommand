package syscommand;

import java.util.LinkedList;
import java.util.List;

/**
 * Base class for commands containing more subcommands.
 */
public abstract class CompositeCommand extends CommandBase {
	
	/**
	 * List of subcommands.
	 */
	protected List<CommandBase> list = new LinkedList<CommandBase>();
		
}
