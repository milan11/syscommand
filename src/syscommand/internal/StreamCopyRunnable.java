package syscommand.internal;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Runnable copying all data from the provided input stream to the provided output stream.
 * Allows getting copying status (if it was successful).
 * Closes both streams after copying.
 */
public class StreamCopyRunnable implements Runnable {
	
	/**
	 * Buffer size (in bytes) for stream copying.
	 */
	private static final int BUFFER_SIZE = 4 * 1024 * 1024;
	
	/**
	 * Input stream to copy from.
	 */
	private InputStream is;
	
	/**
	 * Output stream to copy to.
	 */
	private OutputStream os;
	
	/**
	 * If the copying was successful.
	 */
	private boolean success;
	
	/**
	 * Creates new stream copy runnable with the specified streams.
	 * @param is input stream to copy from
	 * @param os output stream to copy to
	 */
	public StreamCopyRunnable(InputStream is, OutputStream os) {
		this.is = is;
		this.os = os;
		this.success = false;
	}
	
	/**
	 * Copies from the input stream to the output stream and sets the status.
	 * Closes both streams after copying. 
	 */
	@Override
	public void run() {
		try {
			copy(is, os);
			
			success = true;
		} catch (Throwable t) {
			success = false;
		} finally {
			try {
				is.close();
			} catch (Throwable t) {
				// nothing
			}
			try {
				os.close();
			} catch (Throwable t) {
				// nothing
			}
		}
	}
	
	/**
	 * Gets copying status (if the copying was successful.
	 * @return copying status
	 */
	public boolean isSuccess() {
		return success;
	}
	
	/**
	 * Copies all date from an input stream to an output stream.
	 * @param is input stream to copy data from
	 * @param os input stream to copy data to
	 * @throws IOException if some input or output has failed
	 */
	private static void copy(InputStream is, OutputStream os) throws IOException {
		byte[] b = new byte[BUFFER_SIZE];
		int partLen;
		while ((partLen = is.read(b)) != -1) {
			os.write(b, 0, partLen);
		}
	}

}
