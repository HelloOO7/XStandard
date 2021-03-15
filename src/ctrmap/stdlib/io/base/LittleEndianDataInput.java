package ctrmap.stdlib.io.base;

import ctrmap.stdlib.io.iface.DataInputEx;
import ctrmap.stdlib.io.iface.PositionedDataInput;
import ctrmap.stdlib.io.util.BitUtils;
import ctrmap.stdlib.io.util.StringUtils;
import java.io.DataInput;
import java.io.IOException;

/**
 *
 */
public class LittleEndianDataInput implements PositionedDataInput, DataInputEx {

	protected DataInput dis;
	protected int position;

	public LittleEndianDataInput(DataInput dis) {
		this.dis = dis;
	}

	@Override
	public int readInt() throws IOException {
		position += 4;
		return Integer.reverseBytes(dis.readInt());
	}

	public int readUInt24() throws IOException {
		position += 3;
		return BitUtils.readUInt24LE(dis);
	}
	
	public int readInt24() throws IOException {
		return BitUtils.signExtend(readUInt24(), 24);
	}

	public int read4Bytes() throws IOException {
		position += 4;
		return dis.readInt();
	}

	@Override
	public short readShort() throws IOException {
		position += 2;
		return Short.reverseBytes(dis.readShort());
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return Short.toUnsignedInt(readShort());
	}

	public long readUnsignedInt() throws IOException {
		return Integer.toUnsignedLong(readInt());
	}

	public short read2Bytes() throws IOException {
		position += 2;
		return dis.readShort();
	}

	@Override
	public byte readByte() throws IOException {
		position++;
		return dis.readByte();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return readByte() & 0xFF;
	}

	public int read() throws IOException {
		position++;
		return dis.readUnsignedByte();
	}

	public void read(byte[] b) throws IOException {
		position += b.length;
		dis.readFully(b);
	}

	public String readString() throws IOException {
		return StringUtils.readString(this);
	}

	@Override
	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	public long skip(long n) throws IOException {
		long skipped = dis.skipBytes((int) n);
		position += skipped;
		return skipped;
	}
	
	@Override
	public void align(int amount) throws IOException {
		if (position % amount != 0){
			skip(amount - position % amount);
		}
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		read(b);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		position += len;
		dis.readFully(b, off, len);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return (int) skip(n);
	}

	@Override
	public boolean readBoolean() throws IOException {
		return readByte() > 0;
	}

	@Override
	public char readChar() throws IOException {
		return (char) readShort();
	}

	@Override
	public long readLong() throws IOException {
		position += 8;
		return Long.reverseBytes(dis.readLong());
	}

	@Override
	public double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	@Override
	public String readLine() throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String readUTF() throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	protected boolean closeEnabled = true;
	
	public void setCloseEnabled(boolean v){
		closeEnabled = v;
	}

	@Override
	public int getPosition() throws IOException {
		return position;
	}
}
