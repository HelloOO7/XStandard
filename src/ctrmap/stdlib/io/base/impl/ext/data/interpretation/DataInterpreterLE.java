package ctrmap.stdlib.io.base.impl.ext.data.interpretation;

import java.io.IOException;
import ctrmap.stdlib.io.base.iface.ReadableStream;
import ctrmap.stdlib.io.base.iface.WriteableStream;

public class DataInterpreterLE implements IDataInterpreter {

	private byte[] temp = new byte[8];
	private int[] b = new int[8];

	private void readTemp(ReadableStream stm, int amount) throws IOException {
		stm.read(temp, 0, amount);
		for (int i = 0; i < amount; i++) {
			b[i] = temp[i] & 0xFF;
		}
	}

	@Override
	public long readLong(ReadableStream stm) throws IOException {
		readTemp(stm, Long.BYTES);
		return b[0] | (b[1] << 8) | (b[2] << 16) | (b[3]) << 24 | ((long) b[4] << 32) | ((long) b[5] << 40) | ((long) b[6] << 48) | ((long) b[7] << 56);
	}

	@Override
	public int readInt(ReadableStream stm) throws IOException {
		readTemp(stm, Integer.BYTES);
		return b[0] | (b[1] << 8) | (b[2] << 16) | (b[3]) << 24;
	}

	@Override
	public int readInt24(ReadableStream stm) throws IOException {
		return stm.read() | (stm.read() << 8) | (((byte) stm.read()) << 16);
	}

	@Override
	public short readShort(ReadableStream stm) throws IOException {
		//Original Java DataInputStream does not use buffering, so I assume it's faster like this for 2 bytes
		return (short) (stm.read() | (stm.read() << 8));
	}

	@Override
	public byte readByte(ReadableStream stm) throws IOException {
		return (byte) stm.read();
	}

	@Override
	public void writeLong(WriteableStream stm, long value) throws IOException {
		temp[0] = (byte) value;
		temp[1] = (byte) (value >>> 8);
		temp[2] = (byte) (value >>> 16);
		temp[3] = (byte) (value >>> 24);
		temp[4] = (byte) (value >>> 32);
		temp[5] = (byte) (value >>> 40);
		temp[6] = (byte) (value >>> 48);
		temp[7] = (byte) (value >>> 56);
		stm.write(temp, 0, Long.BYTES);
	}

	@Override
	public void writeInt(WriteableStream stm, int value) throws IOException {
		temp[0] = (byte) value;
		temp[1] = (byte) (value >>> 8);
		temp[2] = (byte) (value >>> 16);
		temp[3] = (byte) (value >>> 24);
		stm.write(temp, 0, Integer.BYTES);
	}

	@Override
	public void writeInt24(WriteableStream stm, int value) throws IOException {
		stm.write(value);
		stm.write(value >>> 8);
		stm.write(value >>> 16);
	}

	@Override
	public void writeShort(WriteableStream stm, int value) throws IOException {
		stm.write(value);
		stm.write(value >>> 8);
	}

	@Override
	public void writeByte(WriteableStream stm, int value) throws IOException {
		stm.write(value);
	}
}