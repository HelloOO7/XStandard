package ctrmap.stdlib.io.base.iface;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public interface ReadableStream extends Positioned, Closeable {

	public int read() throws IOException;

	public int skipBytes(int amount) throws IOException;

	public int read(byte[] b, int off, int len) throws IOException;

	public default void seekNext(int pos) throws IOException {
		int cpos = getPosition();
		if (pos >= cpos) {
			skipBytes(pos - cpos);
		} else {
			throw new EOFException("Can not seek back in a stream. (Current position " + Integer.toHexString(cpos) + ", seek " + Integer.toHexString(pos) + ")");
		}
	}
	
	public default byte[] readBytes(int amount) throws IOException {
		byte[] arr = new byte[amount];
		read(arr);
		return arr;
	}

	public default int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	public default void align(int amount) throws IOException {
		if (getPosition() % amount != 0) {
			skipBytes(amount - getPosition() % amount);
		}
	}

	public default InputStream getInputStream() {
		return new ReadableStreamInputStream(this);
	}

	public static class ReadableStreamInputStream extends InputStream {

		private ReadableStream stm;

		public ReadableStreamInputStream(ReadableStream stm) {
			this.stm = stm;
		}

		@Override
		public int read() throws IOException {
			return stm.read();
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return stm.read(b, off, len);
		}
	}
}
