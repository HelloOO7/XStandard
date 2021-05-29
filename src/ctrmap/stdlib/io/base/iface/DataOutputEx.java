package ctrmap.stdlib.io.base.iface;

import ctrmap.stdlib.io.util.StringIO;

import java.io.DataOutput;
import java.io.IOException;

public interface DataOutputEx extends DataOutput, WriteableBase {
	public void writeInt24(int value) throws IOException;
	
    public default void writeString(String str) throws IOException {
        StringIO.writeString(this, str);
    }
	
	public default void writeStringUTF16(String str) throws IOException {
        StringIO.writeStringUTF16(this, str);
    }
	
	public default void writeStringUnterminated(String str) throws IOException {
        StringIO.writeStringUnterminated(this, str);
    }

    public default void writePaddedString(String str, int size) throws IOException {
        StringIO.writePaddedString((DataOutput) this, str, size);
    }

    public default void writeEnum(Enum enm) throws IOException {
        write(enm.ordinal());
    }
	
	@Override
	public default void writeFloat(float f) throws IOException {
        writeInt(Float.floatToIntBits(f));
    }

	@Override
    public default void writeDouble(double d) throws IOException {
        writeLong(Double.doubleToLongBits(d));
    }

	@Override
    public default void writeChar(int c) throws IOException {
        writeShort(c);
    }

	@Override
    public default void writeBoolean(boolean b) throws IOException {
        writeByte((byte)(b ? 1 : 0));
    }
}
