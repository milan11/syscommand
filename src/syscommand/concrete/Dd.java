package syscommand.concrete;

import java.io.File;

import syscommand.SingleCommand;

public class Dd extends SingleCommand {

	public Dd() {
		setCommand("dd");
	}
	
	public Dd inputFile(File file) {
		addArg_equal("if", file.getPath());
		return this;
	}
	
	public Dd input_blockSize(long bytes) {
		addArg_equal("ibs", "" + bytes);
		return this;
	}
	
	public Dd input_skipBlocks(long count) {
		addArg_equal("skip", "" + count);
		return this;
	}
	
	public Dd input_blocksCount(long count) {
		addArg_equal("count", "" + count);
		return this;
	}
	
	public Dd outputFile(File file) {
		addArg_equal("of", file.getPath());
		return this;
	}
	
	public Dd output_blockSize(int bytes) {
		addArg_equal("obs", "" + bytes);
		return this;
	}
	
	public Dd output_seekBlocks(int count) {
		addArg_equal("seek", "" + count);
		return this;
	}
	
	public Dd output_notrunc_append() {
		addArg_equal("conv", "notrunc");
		addArg_equal("oflag", "append");
		return this;
	}
	
}
