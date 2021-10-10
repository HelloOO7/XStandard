package ctrmap.stdlib.io.base.impl.ext;

import ctrmap.stdlib.io.base.iface.ReadableStream;
import ctrmap.stdlib.io.base.impl.ReadableWrapper;
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
	public int getPosition() throws IOException {
		return in.getPosition() - startPos;
	}
	
	@Override
	public int getLength() {
		return endPos - startPos;
	}
}
