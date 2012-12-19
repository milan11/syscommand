package syscommand.concrete;

import java.io.File;

import syscommand.SingleCommand;

public class Cp extends SingleCommand {

	public Cp(File source, File destination) {
		setCommand("cp");
		addLastArg(source.getPath());
		addLastArg(destination.getPath());
	}
	
	public Cp preserveAllAttributes() {
		addArg_longSwitchEqual("preserve", "all");
		return this;
	}
	
	public Cp recursive() {
		addArg_switch("r");
		return this;
	}

}
