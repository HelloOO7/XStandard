package ctrmap.stdlib.io.base.impl.ext.data.interpretation;

import java.io.IOException;
import ctrmap.stdlib.io.base.iface.ReadableStream;
import ctrmap.stdlib.io.base.iface.WriteableStream;

public class DataInterpreterBE implements IDataInterpreter {

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
		return (((long) b[0] << 56)
			+ ((long) (b[1] & 255) << 48)
			+ ((long) (b[2] & 255) << 40)
			+ ((long) (b[3] & 255) << 32)
			+ ((long) (b[4] & 255) << 24)
			+ ((b[5] & 255) << 16)
			+ ((b[6] & 255) << 8)
			+ ((b[7] & 255) << 0));
	}

	@Override
	public int readSized(ReadableStream strm, int bytes) throws IOException {
		if (bytes == 1) {
			return strm.read();
		}
		readTemp(strm, bytes);
		int val = 0;
		for (int i = 0; i < bytes; i++) {
			val |= (b[i] & 0xFF);
			val <<= 8;
		}
		return val;
	}

	@Override
	public int readInt(ReadableStream stm) throws IOException {
		readTemp(stm, Integer.BYTES);
		return b[3] | (b[2] << 8) | (b[1] << 16) | (b[0]) << 24;
	}

	@Override
	public int readInt24(ReadableStream stm) throws IOException {
		return (((byte) stm.read()) << 16) | (stm.read() << 8) | stm.read();
	}

	@Override
	public short readShort(ReadableStream stm) throws IOException {
		//Original Java DataInputStream does not use buffering, so I assume it's faster like this for 2 bytes
		return (short) ((stm.read() << 8) | stm.read());
	}

	@Override
	public byte readByte(ReadableStream stm) throws IOException {
		return (byte) stm.read();
	}

	@Override
	public void writeLong(WriteableStream stm, long value) throws IOException {
		temp[7] = (byte) value;
		temp[6] = (byte) (value >>> 8);
		temp[5] = (byte) (value >>> 16);
		temp[4] = (byte) (value >>> 24);
		temp[3] = (byte) (value >>> 32);
		temp[2] = (byte) (value >>> 40);
		temp[1] = (byte) (value >>> 48);
		temp[0] = (byte) (value >>> 56);
		stm.write(temp, 0, Long.BYTES);
	}

	@Override
	public void writeInt(WriteableStream stm, int value) throws IOException {
		temp[3] = (byte) value;
		temp[2] = (byte) (value >>> 8);
		temp[1] = (byte) (value >>> 16);
		temp[0] = (byte) (value >>> 24);
		stm.write(temp, 0, Integer.BYTES);
	}

	@Override
	public void writeInt24(WriteableStream stm, int value) throws IOException {
		stm.write(value >>> 16);
		stm.write(value >>> 8);
		stm.write(value);
	}

	@Override
	public void writeShort(WriteableStream stm, int value) throws IOException {
		stm.write(value >>> 8);
		stm.write(value);
	}

	@Override
	public void writeByte(WriteableStream stm, int value) throws IOException {
		stm.write(value);
	}
}
