package ctrmap.stdlib.io.base;

import ctrmap.stdlib.io.iface.SeekableDataOutput;
import ctrmap.stdlib.io.iface.SeekableDataInput;
import ctrmap.stdlib.io.util.BitUtils;
import java.io.Closeable;
import java.io.IOException;

public abstract class LittleEndianIO extends IOStream {

	protected LittleEndianIO() {

	}

	public void mirrorTo(LittleEndianIO target) {
		target.dis = dis;
		target.dos = dos;
		target.closeable = closeable;
	}

	public LittleEndianIO(SeekableDataInput dis, SeekableDataOutput dos) {
		this(dis, dos, null);
	}

	public LittleEndianIO(SeekableDataInput dis, SeekableDataOutput dos, Closeable closeable) {
		super(dis, dos, closeable);
	}

	@Override
	public int readInt() throws IOException {
		return Integer.reverseBytes(read4Bytes());
	}

	public int readUInt24() throws IOException {
		int r = BitUtils.readUInt24LE(dis);
		syncOutPos();
		return r;
	}

	public int readInt24() throws IOException {
		return BitUtils.signExtend(readUInt24(), 24);
	}

	public int read4Bytes() throws IOException {
		int r = dis.readInt();
		syncOutPos();
		return r;
	}

	@Override
	public short readShort() throws IOException {
		return Short.reverseBytes(read2Bytes());
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return Short.toUnsignedInt(readShort());
	}

	public long readUnsignedInt() throws IOException {
		return Integer.toUnsignedLong(readInt());
	}

	public short read2Bytes() throws IOException {
		short r = dis.readShort();
		syncOutPos();
		return r;
	}

	@Override
	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	@Override
	public char readChar() throws IOException {
		return (char) readShort();
	}

	@Override
	public long readLong() throws IOException {
		long r = Long.reverseBytes(dis.readLong());
		syncOutPos();
		return r;
	}

	@Override
	public String readLine() throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String readUTF() throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeInt(int v) throws IOException {
		write4Bytes(Integer.reverseBytes(v));
	}

	public void write4Bytes(int v) throws IOException {
		dos.writeInt(v);
		syncInPos();
	}

	@Override
	public void writeShort(int v) throws IOException {
		write2Bytes(Short.reverseBytes((short) v));
	}

	public void write2Bytes(short v) throws IOException {
		dos.writeShort(v);
		syncInPos();
	}
	
	@Override
	public void writeChar(int v) throws IOException {
		dos.writeChar(Character.reverseBytes((char) v));
		syncInPos();
	}

	@Override
	public void writeLong(long v) throws IOException {
		dos.writeLong(Long.reverseBytes(v));
		syncInPos();
	}

	@Override
	public void writeDouble(double v) throws IOException {
		writeLong(Double.doubleToLongBits(v));
	}

	@Override
	public void writeUTF(String s) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
