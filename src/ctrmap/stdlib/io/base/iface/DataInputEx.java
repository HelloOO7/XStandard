package ctrmap.stdlib.io.base.iface;

import ctrmap.stdlib.io.util.StringIO;

import java.io.DataInput;
import java.io.IOException;

public interface DataInputEx extends DataInput, ReadableStream {
	public int readInt24() throws IOException;
	
    public default String readString() throws IOException {
        return StringIO.readString(this);
    }
	
	public default String readStringUTF16() throws IOException {
        return StringIO.readStringUTF16(this);
    }

    public default String readPaddedString(int size) throws IOException {
        return StringIO.readPaddedString(this, size);
    }
	
	public default long readUnsignedInt() throws IOException {
		return (long)readInt() & 0xFFFFFFFFL;
	}
	
	public default int readUnsignedInt24() throws IOException {
		return readInt24() & 0xFFFFFF;
	}
	
	@Override
	public default int readUnsignedShort() throws IOException {
        return readShort() & 0xFFFF;
    }

	@Override
    public default int readUnsignedByte() throws IOException {
        return readByte() & 0xFF;
    }

	@Override
    public default float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

	@Override
    public default double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

	@Override
    public default char readChar() throws IOException {
        return (char)readShort();
    }

	@Override
    public default boolean readBoolean() throws IOException {
        return readByte() != 0;
    }
}
