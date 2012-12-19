package syscommand.concrete;

import java.io.File;

import syscommand.SingleCommand;

public class Mount extends SingleCommand {
	
	public Mount(File source, File target) {
		setCommand("mount");
		addLastArg(source.getPath());
		addLastArg(target.getPath());
	}
	
	public Mount(String source, File target) {
		setCommand("mount");
		addLastArg(source);
		addLastArg(target.getPath());
	}
		
	public Mount specifyType(String type) {
		addArg_switch("t", type);
		return this;
	}
	
	public Mount bind() {
		addArg_switch("o", "bind");
		return this;
	}
	
	public Mount loop() {
		addArg_switch("o", "loop");
		return this;
	}
	
}
