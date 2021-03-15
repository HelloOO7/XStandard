package ctrmap.stdlib.io.base;

import ctrmap.stdlib.io.iface.DataOutputEx;
import ctrmap.stdlib.io.iface.PositionedDataOutput;
import ctrmap.stdlib.io.util.StringUtils;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LittleEndianDataOutput implements PositionedDataOutput, DataOutputEx{

	protected DataOutput dos;
	public int position;

	public LittleEndianDataOutput(OutputStream out) {
		dos = new DataOutputStream(out);
	}
	
	public LittleEndianDataOutput(DataOutput out) {
		dos = out;
	}

	@Override
	public void writeInt(int v) throws IOException {
		write4Bytes(Integer.reverseBytes(v));
	}

	public void write4Bytes(int v) throws IOException {
		position += 4;
		dos.writeInt(v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		write2Bytes(Short.reverseBytes((short)v));
	}

	public void write2Bytes(short v) throws IOException {
		position += 2;
		dos.writeShort(v);
	}

	@Override
	public void writeByte(int v) throws IOException {
		write(v);
	}

	@Override
	public void write(int v) throws IOException {
		position++;
		dos.write(v);
	}

	@Override
	public void write(byte[] b) throws IOException {
		position += b.length;
		dos.write(b);
	}

	public void writeString(String str) throws IOException {
		StringUtils.writeString(this, str);
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
	public int getPosition() throws IOException {
		return (int)position;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		position += len;
		dos.write(b, off, len);
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		write(v ? 1 : 0);
	}

	@Override
	public void writeChar(int v) throws IOException {
		position += 2;
		dos.writeChar(Character.reverseBytes((char)v));
	}

	@Override
	public void writeLong(long v) throws IOException {
		position += 8;
		dos.writeLong(Long.reverseBytes(v));
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
		for (int i = 0; i < s.length(); i++){
			writeChar(s.charAt(i));
		}
	}

	@Override
	public void writeUTF(String s) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public void writeStringUnterminated(String str) throws IOException{
		StringUtils.writeStringUnterminated(this, str);
	}
	
	protected boolean closeEnabled = true;
	
	public void setCloseEnabled(boolean v){
		closeEnabled = v;
	}
}
