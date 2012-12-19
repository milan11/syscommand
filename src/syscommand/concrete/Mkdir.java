package syscommand.concrete;

import java.io.File;

import syscommand.SingleCommand;

public class Mkdir extends SingleCommand {
	
	public Mkdir(File dir) {
		setCommand("mkdir");
		addLastArg(dir.getPath());
	}
	
	public Mkdir createParents() {
		addArg_switch("p");
		return this;
	}
	
}
