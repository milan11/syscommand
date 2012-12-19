package syscommand.concrete;

import java.io.File;

import syscommand.SingleCommand;

public class Rm extends SingleCommand {
	
	public Rm(File file) {
		setCommand("rm");
		addLastArg(file.getPath());
	}
	
	public Rm(String file) {
		setCommand("rm");
		addLastArg(file);
	}
	
	public Rm recursive() {
		addArg_switch("r");
		return this;
	}
	
}
