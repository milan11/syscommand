package syscommand.concrete;

import java.io.File;

import syscommand.SingleCommand;

public class Rmdir extends SingleCommand {
	
	public Rmdir(File dir) {
		setCommand("rmdir");
		addLastArg(dir.getPath());
	}
	
}
