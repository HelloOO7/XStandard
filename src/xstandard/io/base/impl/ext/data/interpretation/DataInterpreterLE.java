package xstandard.io.base.impl.ext.data.interpretation;

import java.io.IOException;
import xstandard.io.base.iface.ReadableStream;
import xstandard.io.base.iface.WriteableStream;

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
		return (((long) b[7] << 56)
			+ ((long) (b[6] & 255) << 48)
			+ ((long) (b[5] & 255) << 40)
			+ ((long) (b[4] & 255) << 32)
			+ ((long) (b[3] & 255) << 24)
			+ ((b[2] & 255) << 16)
			+ ((b[1] & 255) << 8)
			+ ((b[0] & 255) << 0));
	}

	@Override
	public int readSized(ReadableStream stm, int bytes) throws IOException {
		if (bytes == 1) {
			return stm.read();
		}
		readTemp(stm, bytes);
		int val = 0;
		for (int i = 0; i < bytes; i++) {
			val |= (b[i] & 0xFF) << (i << 3);
		}
		return val;
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
	
		
	@Override
	public void writeSized(WriteableStream stm, int value, int size) throws IOException {
		if (size == 1) {
			stm.write(value);
			return;
		}
		for (int i = 0; i < size; i++) {
			temp[i] = (byte)(value & 0xFF);
			value >>= 8;
		}
		stm.write(temp, 0, size);
	}
}
