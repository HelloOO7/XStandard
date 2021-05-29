package ctrmap.stdlib.io.base.impl;

import java.io.IOException;
import java.io.OutputStream;
import ctrmap.stdlib.io.base.iface.WriteableStream;

public class OutputStreamWriteable implements WriteableStream {

	protected OutputStream out;

	private int position = 0;

	public OutputStreamWriteable(OutputStream out) {
		this.out = out;
	}

	@Override
	public int getPosition() throws IOException {
		return position;
	}

	@Override
	public void write(int i) throws IOException {
		out.write(i);
		position++;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
		position += len;
	}

	@Override
	public void close() throws IOException {
		out.close();
	}

	@Override
	public int getLength() {
		return position; //an output stream has position == length IIRC
	}
	
	@Override
	public OutputStream getOutputStream(){
		return out;
	}
}
