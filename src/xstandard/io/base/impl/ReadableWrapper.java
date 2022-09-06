package xstandard.io.base.impl;

import java.io.IOException;
import xstandard.io.base.iface.ReadableStream;

public class ReadableWrapper implements ReadableStream {

	protected ReadableStream in;

	public ReadableWrapper(ReadableStream readable) {
		in = readable;
	}

	@Override
	public int getPosition() throws IOException {
		return in.getPosition();
	}

	@Override
	public int read() throws IOException {
		return in.read();
	}

	@Override
	public int skipBytes(int amount) throws IOException {
		return in.skipBytes(amount);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return in.read(b, off, len);
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

	@Override
	public int getLength() {
		return in.getLength();
	}
}
