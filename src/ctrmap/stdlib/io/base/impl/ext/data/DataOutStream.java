package ctrmap.stdlib.io.base.impl.ext.data;

import ctrmap.stdlib.io.IOCommon;
import ctrmap.stdlib.io.base.impl.OutputStreamWriteable;
import ctrmap.stdlib.io.base.impl.WriteableWrapper;
import ctrmap.stdlib.io.base.impl.ext.data.interpretation.IDataInterpreter;
import ctrmap.stdlib.io.base.iface.DataOutputEx;

import java.io.*;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import ctrmap.stdlib.io.base.iface.WriteableStream;
import ctrmap.stdlib.io.base.impl.access.MemoryStream;

public class DataOutStream extends WriteableWrapper implements DataOutputEx {

	private IDataInterpreter interpreter;

	public DataOutStream(){
		this(new MemoryStream());
	}
	
	public DataOutStream(OutputStream out) {
		this(new OutputStreamWriteable(out));
	}

	public DataOutStream(WriteableStream out) {
		super(out);

		interpreter = IOCommon.getDefaultDataInterpreter();
	}

	public void order(ByteOrder order) {
		interpreter = IOCommon.getInterpreterForByteOrder(order);
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
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void writeByte(int v) throws IOException {
		interpreter.writeByte(this, v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		interpreter.writeShort(this, v);
	}

	@Override
	public void writeInt24(int value) throws IOException {
		interpreter.writeInt24(this, value);
	}

	@Override
	public void writeInt(int v) throws IOException {
		interpreter.writeInt(this, v);
	}

	@Override
	public void writeLong(long v) throws IOException {
		interpreter.writeLong(this, v);
	}

	@Override
	public void writeBytes(String s) throws IOException {
		write(s.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public void writeChars(String s) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeUTF(String s) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getPosition() throws IOException {
		return out.getPosition();
	}
}
