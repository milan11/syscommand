package syscommand.concrete;

import java.io.File;

import syscommand.SingleCommand;

public class Mv extends SingleCommand {
	
	public Mv(File source, File destination) {
		setCommand("mv");
		addLastArg(source.getPath());
		addLastArg(destination.getPath());
	}

}
