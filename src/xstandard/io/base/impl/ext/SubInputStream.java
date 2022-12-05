package xstandard.io.base.impl.ext;

import xstandard.io.base.iface.ReadableStream;
import xstandard.io.base.impl.ReadableWrapper;
import java.io.IOException;

public class SubInputStream extends ReadableWrapper {

	private final int startPos;
	private final int endPos;
	
	public SubInputStream(ReadableStream readable, int startPos, int endPos) {
		super(readable);
		this.startPos = startPos;
		this.endPos = endPos;
		try {
			readable.seekNext(startPos);
		} catch (IOException ex) {
			throw new RuntimeException("Could not seek to startPos of SubInputStream!");
		}
	}
	
	@Override
	public int available() throws IOException {
		return super.available() - startPos;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (len == 0) {
			return 0;
		}
		len = Math.min(len, getLength() - getPosition());
		if (len == 0) {
			return -1;
		}
		return super.read(b, off, len);
	}

	@Override
	public int getPosition() throws IOException {
		return in.getPosition() - startPos;
	}
	
	@Override
	public int getLength() {
		return endPos - startPos;
	}
}
