package xstandard.io.base.impl.access;

import xstandard.io.base.iface.IOStream;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

public class MemoryStream implements IOStream {

	protected byte[] buffer;
	protected int position;
	protected int limit;

	public MemoryStream() {
		this(4096);
	}

	public MemoryStream(byte[] buffer) {
		this.buffer = buffer;
		limit = buffer.length;
	}

	public MemoryStream(int initialCapacity) {
		buffer = new byte[initialCapacity];
	}

	protected void ensureCapacity(int cap) {
		if (cap > buffer.length) {
			int newCapacity = buffer.length;

			if (newCapacity != 0) {
				while (newCapacity < cap) {
					newCapacity *= 2;
				}
			} else {
				newCapacity = cap;
			}

			buffer = Arrays.copyOf(buffer, newCapacity);
		}
	}

	@Override
	public byte[] toByteArray() {
		return Arrays.copyOf(buffer, limit);
	}

	@Override
	public int read() throws IOException {
		if (position < limit) {
			return buffer[position++] & 0xFF;
		}
		throw new EOFException("Tried to read at position 0x" + Integer.toHexString(position) + ", but buffer is only 0x" + Integer.toHexString(limit) + " bytes!");
	}

	@Override
	public int skipBytes(int amount) throws IOException {
		amount = Math.min(amount, limit - position);
		position += amount;
		return amount;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int end = position + len;
		if (end <= limit) {
			System.arraycopy(buffer, position, b, off, len);
			position += len;
			return len;
		} else if (position != limit) {
			int diff = limit - position;
			System.arraycopy(buffer, position, b, off, diff);
			position = limit;
			return diff;
		} else {
			//throw new EOFException("Tried to read 0x" + Integer.toHexString(len) + " bytes at position 0x" + Integer.toHexString(position) + ", but buffer is only " + Integer.toHexString(buffer.length) + " bytes!");
			return -1;
		}
	}

	@Override
	public void write(int i) throws IOException {
		ensureCapacity(position + 1);
		buffer[position++] = (byte) i;
		updateLimit();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		ensureCapacity(position + len);
		try {
			System.arraycopy(b, off, buffer, position, len);
			position += len;
		} catch (ArrayIndexOutOfBoundsException ex) {
			System.err.println("FUCK ! Could not arraycopy to MemoryStream !! off " + off + ", len " + len + ", buffer pos " + (position - len) + " input cap " + b.length + " buffer cap " + buffer.length);
		}
		updateLimit();
	}

	@Override
	public int getPosition() throws IOException {
		return position;
	}

	@Override
	public void seek(long position) throws IOException {
		if (position < 0) {
			throw new EOFException("Negative seek offset ! ! " + Long.toHexString(position));
		}
		this.position = (int) position;
	}

	@Override
	public int getLength() {
		return limit;
	}

	protected void updateLimit() {
		if (position > limit) {
			limit = position;
		}
	}

	@Override
	public void close() throws IOException {

	}

	@Override
	public void setLength(long length) throws IOException {
		int ilength = (int) length;
		limit = ilength;
		ensureCapacity(ilength);
	}
}
