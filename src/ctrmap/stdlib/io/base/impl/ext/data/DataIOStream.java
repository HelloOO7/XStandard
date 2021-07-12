package ctrmap.stdlib.io.base.impl.ext.data;

import ctrmap.stdlib.io.IOCommon;
import ctrmap.stdlib.io.base.impl.IOStreamWrapper;
import ctrmap.stdlib.io.base.impl.ext.data.interpretation.IDataInterpreter;
import ctrmap.stdlib.io.base.iface.DataInputEx;
import ctrmap.stdlib.io.base.iface.DataOutputEx;
import ctrmap.stdlib.io.base.iface.IOStream;
import ctrmap.stdlib.io.base.impl.access.FileStream;
import ctrmap.stdlib.io.base.impl.access.MemoryStream;
import ctrmap.stdlib.io.util.StringIO;
import java.io.File;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Stack;

public class DataIOStream extends IOStreamWrapper implements DataInputEx, DataOutputEx {

	private ByteOrder order;
	private IDataInterpreter interpreter;

	private Stack<Integer> baseAddresses = new Stack<>();
	private int currentBase = 0;

	private Stack<Integer> checkpoints = new Stack<>();

	public DataIOStream(IOStream strm) {
		this(strm, IOCommon.DEFAULT_BYTE_ORDER);
	}

	public DataIOStream(IOStream strm, ByteOrder order) {
		super(strm);

		order(order);
	}

	public DataIOStream(byte[] bytes) {
		this(new MemoryStream(bytes));
	}

	public DataIOStream() {
		this(new MemoryStream());
	}

	public DataIOStream(File f) {
		this(FileStream.create(f));
	}

	public final void order(ByteOrder order) {
		this.order = order;
		interpreter = IOCommon.getInterpreterForByteOrder(order);
	}

	public ByteOrder order() {
		return order;
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		read(b);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		read(b, off, len);
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

	public int readAddress() throws IOException {
		return currentBase + readInt();
	}

	public String readStringWithAddress() throws IOException {
		return StringIO.readStringWithAddress(this);
	}

	public String readByteLengthString() throws IOException {
		return StringIO.readByteLengthString(this);
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
	public void writeByte(int v) throws IOException {
		interpreter.writeByte(this, v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		interpreter.writeShort(this, v);
	}

	@Override
	public void writeInt24(int value) throws IOException {
		interpreter.writeInt24(io, value);
	}

	@Override
	public void writeInt(int v) throws IOException {
		interpreter.writeInt(this, v);
	}

	public void writeAddress(int addr) throws IOException {
		if (addr == 0) {
			writeInt(addr);
		} else {
			writeInt(addr + currentBase);
		}
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
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void seek(int position) throws IOException {
		if (position == 0 && currentBase > 0) {
			throw new NullPointerException("Tried to seek to a null pointer ! !");
		}
		super.seek(position - currentBase);
	}

	public void seekAndSeek(int position) throws IOException {
		seekAndSeek(position, 0);
	}

	public void seekAndSeek(int position, int offset) throws IOException {
		seek(position);
		seek(readInt() + offset);
	}

	public void seekUnbased(int position) throws IOException {
		super.seek(position);
	}

	@Override
	public int getPosition() throws IOException {
		return super.getPosition() + currentBase;
	}

	public int getPositionUnbased() throws IOException {
		return super.getPosition();
	}

	@Override
	public int getLength() {
		return super.getLength() + currentBase;
	}

	public void resetBase() {
		currentBase = baseAddresses.pop();
	}

	public void setBase(int base) {
		baseAddresses.push(currentBase);
		currentBase = base;
	}

	public void setBaseHere() throws IOException {
		setBase(-getPositionUnbased());
	}

	public void checkpoint() throws IOException {
		checkpoints.push(getPositionUnbased());
	}

	public void resetCheckpoint() throws IOException {
		if (!checkpoints.empty()) {
			seekUnbased(checkpoints.pop());
		}
	}
}
