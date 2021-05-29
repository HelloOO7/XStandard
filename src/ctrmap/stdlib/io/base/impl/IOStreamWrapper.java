package ctrmap.stdlib.io.base.impl;

import ctrmap.stdlib.io.base.iface.IOStream;

import java.io.IOException;

public class IOStreamWrapper implements IOStream {

    protected IOStream io;

    public IOStreamWrapper(IOStream strm){
        this.io = strm;
    }
	
	public IOStream getBaseStream(){
		return io;
	}

    @Override
    public int read() throws IOException {
        return io.read();
    }

    @Override
    public int skipBytes(int amount) throws IOException {
        return io.skipBytes(amount);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return io.read(b, off, len);
    }

    @Override
    public int getPosition() throws IOException {
        return io.getPosition();
    }

    @Override
    public void seek(int position) throws IOException {
        io.seek(position);
    }

    @Override
    public int getLength() {
        return io.getLength();
    }

    @Override
    public void write(int i) throws IOException {
        io.write(i);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        io.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        io.close();
    }
	
	@Override
	public byte[] toByteArray(){
		return io.toByteArray();
	}
}
