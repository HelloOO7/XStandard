package ctrmap.stdlib.io.base.iface;

import ctrmap.stdlib.io.util.StringIO;

import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

public interface DataOutputEx extends DataOutput, WriteableBase {

	public void writeInt24(int value) throws IOException;

	public default void writeString(String str) throws IOException {
		StringIO.writeString(this, str);
	}
	
	public default void writeByteLengthString(String str) throws IOException {
		StringIO.writeByteLengthString(this, str);
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
		writeByte((byte) (b ? 1 : 0));
	}

	public default void writeBytes(byte... bytes) throws IOException {
		write(bytes);
	}

	public default void writeShorts(int... shorts) throws IOException {
		for (int val : shorts) {
			writeShort(val);
		}
	}

	public default void writeInts(int... ints) throws IOException {
		for (int val : ints) {
			writeInt(val);
		}
	}

	public default void writeFloats(float... floats) throws IOException {
		for (float val : floats) {
			writeFloat(val);
		}
	}
	
	public default void writeBytes(List<Byte> list) throws IOException {
		for (byte val : list) {
			write(val);
		}
	}
	
	public default void writeShorts(List<Short> list) throws IOException {
		for (short val : list) {
			writeShort(val);
		}
	}

	public default void writeInts(List<Integer> list) throws IOException {
		for (int val : list) {
			writeInt(val);
		}
	}

	public default void writeFloats(List<Float> list) throws IOException {
		for (float val : list) {
			writeFloat(val);
		}
	}
}
