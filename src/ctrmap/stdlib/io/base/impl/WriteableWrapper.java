package ctrmap.stdlib.io.base.impl;


import java.io.IOException;
import ctrmap.stdlib.io.base.iface.WriteableStream;

public class WriteableWrapper implements WriteableStream {

    protected WriteableStream out;

    public WriteableWrapper(WriteableStream writeable) {
        out = writeable;
    }

    @Override
    public int getPosition() throws IOException {
        return out.getPosition();
    }

    @Override
    public void write(int i) throws IOException {
        out.write(i);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }
	
	@Override
	public void close() throws IOException {
		out.close();
	}

	@Override
	public int getLength() {
		return out.getLength();
	}
}
