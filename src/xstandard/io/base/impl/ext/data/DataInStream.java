package xstandard.io.base.impl.ext.data;

import xstandard.io.IOCommon;
import xstandard.io.base.impl.InputStreamReadable;
import xstandard.io.base.impl.ReadableWrapper;
import xstandard.io.base.impl.ext.data.interpretation.IDataInterpreter;
import xstandard.io.base.iface.DataInputEx;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import xstandard.io.base.iface.ReadableStream;
import xstandard.io.base.impl.access.MemoryStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataInStream extends ReadableWrapper implements DataInputEx {

	private IDataInterpreter interpreter;

	public DataInStream(byte[] bytes){
		this(new MemoryStream(bytes));
	}
	
	public DataInStream(File file){
		this(new BufferedInputStream(createFIS(file)));
	}
	
	private static FileInputStream createFIS(File f){
		try {
			return new FileInputStream(f);
		} catch (FileNotFoundException ex) {
			Logger.getLogger(DataInStream.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	
	public DataInStream(InputStream in) {
		this(new InputStreamReadable(in));
	}

	public DataInStream(ReadableStream in) {
		super(in);

		interpreter = IOCommon.getDefaultDataInterpreter();
	}

	@Override
	public void order(ByteOrder order) {
		interpreter = IOCommon.createInterpreterForByteOrder(order);
	}

	@Override
	public int read() throws IOException {
		return in.read();
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		in.read(b);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		in.read(b, off, len);
	}

	@Override
	public int skipBytes(int amount) throws IOException {
		return (int) in.skipBytes(amount);
	}

	@Override
	public byte readByte() throws IOException {
		return interpreter.readByte(this);
	}

	@Override
	public short readShort() throws IOException {
		return interpreter.readShort(this);
	}

	@Override
	public int readInt24() throws IOException {
		return interpreter.readInt24(this);
	}

	@Override
	public int readInt() throws IOException {
		return interpreter.readInt(this);
	}

	@Override
	public long readLong() throws IOException {
		return interpreter.readLong(this);
	}

	@Override
	public String readLine() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String readUTF() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return in.read(b, off, len);
	}

	@Override
	public int getPosition() throws IOException {
		return in.getPosition();
	}
	
	@Override
	public String toString() {
		return "DataInStream @ " + in;
	}
}
