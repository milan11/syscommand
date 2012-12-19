package syscommand.concrete;

import java.io.File;

import syscommand.SingleCommand;

public class Umount extends SingleCommand {
	
	public Umount(File dir) {
		setCommand("umount");
		addLastArg(dir.getPath());
	}
	
}
