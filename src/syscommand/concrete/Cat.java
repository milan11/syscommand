package syscommand.concrete;

import java.io.File;

import syscommand.SingleCommand;

public class Cat extends SingleCommand {

	public Cat() {
		setCommand("cat");
	}
	
	public Cat(File file) {
		this();
		addArgs(file.getPath());
	}
	
	public Cat file(File file) {
		addArgs(file.getPath());
		return this;
	}

}
