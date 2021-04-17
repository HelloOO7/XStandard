package ctrmap.stdlib.io.base;

import ctrmap.stdlib.io.iface.CloseableDataInput;
import ctrmap.stdlib.io.iface.DataInputEx;
import ctrmap.stdlib.io.iface.DataOutputEx;
import ctrmap.stdlib.io.iface.IHasLength;
import ctrmap.stdlib.io.iface.SeekableDataOutput;
import ctrmap.stdlib.io.iface.SeekableDataInput;
import ctrmap.stdlib.io.util.StringUtils;
import java.io.Closeable;
import java.io.IOException;

public abstract class IOWrapper implements SeekableDataInput, SeekableDataOutput, Closeable, CloseableDataInput, DataInputEx, DataOutputEx, IHasLength {

	protected SeekableDataInput dis;
	protected SeekableDataOutput dos;
	protected Closeable closeable;
	private int base = 0;

	protected IOWrapper() {

	}
	
	public IOWrapper(IOWrapper toWrap) {
		dis = toWrap;
		dos = toWrap;
		closeable = toWrap;
	}
	
	public void setBase(int base){
		this.base = base;
	}

	public void mirrorTo(IOWrapper target) {
		target.dis = dis;
		target.dos = dos;
		target.closeable = closeable;
	}

	public IOWrapper(SeekableDataInput dis, SeekableDataOutput dos) {
		this(dis, dos, null);
	}

	public IOWrapper(SeekableDataInput dis, SeekableDataOutput dos, Closeable closeable) {
		this.dis = dis;
		this.dos = dos;
		this.closeable = closeable;
	}

	@Override
	public int readInt() throws IOException {
		int r = dis.readInt();
		syncOutPos();
		return r;
	}

	@Override
	public short readShort() throws IOException {
		short r = dis.readShort();
		syncOutPos();
		return r;
	}

	@Override
	public int readUnsignedShort() throws IOException {
		int r = dis.readUnsignedShort();
		syncOutPos();
		return r;
	}

	@Override
	public byte readByte() throws IOException {
		byte r = dis.readByte();
		syncOutPos();
		return r;
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return readByte() & 0xFF;
	}

	public int read() throws IOException {
		return readUnsignedByte();
	}

	public void read(byte[] b) throws IOException {
		dis.readFully(b);
		syncOutPos();
	}

	public String readString() throws IOException {
		return StringUtils.readString(this);
	}
	
	public String readStringUTF16() throws IOException {
		return StringUtils.readStringUTF16(this);
	}

	@Override
	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	public long skip(long n) throws IOException {
		long skipped = dis.skipBytes((int) n);
		syncOutPos();
		return skipped;
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		read(b);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		dis.readFully(b, off, len);
		syncOutPos();
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
		long r = dis.readLong();
		syncOutPos();
		return r;
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

	@Override
	public void writeInt(int v) throws IOException {
		dos.writeInt(v);
		syncInPos();
	}

	@Override
	public void writeShort(int v) throws IOException {
		dos.writeShort(v);
		syncInPos();
	}

	@Override
	public void writeByte(int v) throws IOException {
		write(v);
	}

	@Override
	public void write(int v) throws IOException {
		dos.write(v);
		syncInPos();
	}

	@Override
	public void write(byte[] b) throws IOException {
		dos.write(b);
		syncInPos();
	}

	public void writeString(String str) throws IOException {
		StringUtils.writeString(this, str);
	}
	
	public void writeStringUTF16(String str) throws IOException {
		StringUtils.writeStringUTF16(this, str);
	}

	@Override
	public void writeFloat(float v) throws IOException {
		writeInt(Float.floatToIntBits(v));
	}

	public void writeEnum(Enum e) throws IOException {
		write(e.ordinal());
	}

	@Override
	public void pad(int align) throws IOException {
		while (getPosition() % align != 0) {
			write(0);
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		dos.write(b, off, len);
		syncInPos();
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		write(v ? 1 : 0);
	}

	@Override
	public void writeChar(int v) throws IOException {
		dos.writeChar((char) v);
		syncInPos();
	}

	@Override
	public void writeLong(long v) throws IOException {
		dos.writeLong(v);
		syncInPos();
	}

	@Override
	public void writeDouble(double v) throws IOException {
		writeLong(Double.doubleToLongBits(v));
	}

	@Override
	public void writeBytes(String s) throws IOException {
		writeString(s);
	}

	@Override
	public void writeChars(String s) throws IOException {
		for (int i = 0; i < s.length(); i++) {
			writeChar(s.charAt(i));
		}
	}

	@Override
	public void writeUTF(String s) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void writeStringUnterminated(String str) throws IOException {
		write(str.getBytes("ASCII"));
	}

	protected void syncOutPos() throws IOException {
		if (dis != dos) {
			dos.seek(dis.getPosition());
		}
	}

	protected void syncInPos() throws IOException {
		if (dis != dos) {
			dis.seek(dos.getPosition());
		}
	}

	@Override
	public void seek(int addr) throws IOException {
		addr -= base;
		seekUnbased(addr);
	}
	
	public void seekUnbased(int addr) throws IOException {
		dis.seek(addr);
		dos.seek(addr);
	}


	public void seekAndSeek(int addr) throws IOException {
		seekAndSeek(addr, 0);
	}
	
	public void seekAndSeek(int addr, int reloc) throws IOException {
		seek(addr);
		seek(readInt() + reloc);
	}

	@Override
	public int getPosition() throws IOException {
		return dos.getPosition() + base;
	}
	
	public int getPositionUnbased() throws IOException {
		return dos.getPosition();
	}

	@Override
	public void close() throws IOException {
		if (closeable != null) {
			closeable.close();
		}
	}

	@Override
	public void align(int amount) throws IOException {
		if (getPosition() % amount != 0) {
			skip(amount - getPosition() % amount);
		}
	}

	@Override
	public abstract int length() throws IOException;
}
