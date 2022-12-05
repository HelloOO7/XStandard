package xstandard.io.base.impl.ext;

import xstandard.io.IOCommon;
import xstandard.io.base.impl.IOStreamWrapper;
import xstandard.io.base.iface.IOStream;
import java.io.EOFException;

import java.io.IOException;

public class BufferedIOStream extends IOStreamWrapper {

	private byte[] buffer;

	private int bIdx = 0;
	private int bIdxMax = 0;
	private long bStmPos = 0;
	private int streamLimit = 0;

	private boolean bInitialized = false;
	private boolean bWritten = false;

	/**
	 * Creates a buffered IO stream handle of an IOStream with the default buffer size.
	 *
	 * @param strm An IOStream.
	 */
	public BufferedIOStream(IOStream strm) {
		this(strm, 128); //The buffer size is smaller than on Buffered***Streams, because we tend to seek often
	}

	/**
	 * Creates a buffered IO stream handle of an IOStream with a given buffer size.
	 *
	 * @param strm An IOStream.
	 * @param bufferSize The size of the BufferedIOStream buffer.
	 */
	public BufferedIOStream(IOStream strm, int bufferSize) {
		super(strm);
		if (bufferSize < 1) {
			throw new IllegalArgumentException("Buffer size can not be 0!");
		}

		buffer = new byte[bufferSize];
	}

	/**
	 * Writes the current buffer data to its associated stream position.
	 *
	 * @throws IOException
	 */
	private void flushBuffer() throws IOException {
		if (bWritten) {
			int amount = Math.max(bIdx, bIdxMax);
			IOCommon.debugPrint("Flushing " + amount + " buf bytes at " + Long.toHexString(bStmPos));
			writeBase(bStmPos, buffer, 0, amount);
			bWritten = false;
		}
	}

	/**
	 * Advances the stream position of a buffer if its capacity has been exceeded. If the buffer is uninitialized, it is force-refilled, but stream position remains unchanged.
	 *
	 * @throws IOException
	 */
	private void refillBufferIfOver() throws IOException {
		boolean isBufOver = bIdx >= buffer.length;
		boolean isBufUnder = !bInitialized;
		if (isBufOver || isBufUnder) {
			if (isBufOver) {
				IOCommon.debugPrint(this + " | Buffer size is over !! - " + Long.toHexString(bStmPos));
				//Flush the existing buffer if exceeded
				flushBuffer();
				bStmPos += buffer.length;
			} else {
				IOCommon.debugPrint(this + " | Buffer size is under !! - " + Long.toHexString(bStmPos));
				//No existing buffer - just read out the data and set the initialized flag
				bStmPos = 0;
				bInitialized = true;
			}
			IOCommon.debugPrint("Refilling buffer from pos " + Integer.toHexString(getPositionBase()));
			streamLimit = readBase(buffer, 0, buffer.length);
			bIdx = 0;
			bIdxMax = 0;
		}
	}

	@Override
	public int read() throws IOException {
		refillBufferIfOver();
		if (bIdx >= streamLimit) {
			throw new EOFException("Can not read at " + getPosition() + " - out of bounds!!");
		}
		return buffer[bIdx++] & 0xFF;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		refillBufferIfOver();

		//Bytes available to read from the current buffer
		int availForReadBuf = streamLimit - bIdx;
		//An accumulator for the return value
		int readTotal = availForReadBuf;
		if (availForReadBuf == 0 || streamLimit == -1) {
			return -1;
		}		
		if (availForReadBuf < 0) {
			throw new EOFException("Can not read " + len + " bytes at 0x" + Integer.toHexString(getPosition()) + " - out of bounds!!");
		}

		if (len <= availForReadBuf) {
			//Read directly from the buffer
			System.arraycopy(buffer, bIdx, b, off, len);
			readTotal = len;
			bIdx += len;
		} else {
			//First, drain the remaining buffer data and flush the buffer
			System.arraycopy(buffer, bIdx, b, off, availForReadBuf);
			flushBuffer();

			//Bytes left to read after the previous operation
			int leftToRead = len - availForReadBuf;

			if (leftToRead < buffer.length) {
				//If the byte remainder fits into a buffer, we can read the data out into it and use the rest for further reads
				int pos = getPosition() + availForReadBuf;
				seekBase(pos);
				int bufferFilled = readBase(buffer, 0, buffer.length);
				int actuallyLeftToRead = Math.min(leftToRead, bufferFilled);
				readTotal += actuallyLeftToRead;
				System.arraycopy(buffer, 0, b, off + availForReadBuf, actuallyLeftToRead);
				bStmPos = pos;
				bIdx = actuallyLeftToRead;
				bIdxMax = bIdx;
				IOCommon.debugPrint("Reading over !! NewPos " + Long.toHexString(bStmPos));
			} else {
				//Otherwise, read the data straight from the stream and impl-seek to the resulting position (which will force-refill the buffer)
				int readAfter = readBase(b, off + availForReadBuf, len - availForReadBuf);
				if (readAfter > 0) {
					readTotal += readAfter;
				}
				seek(getPositionBase());
			}
		}
		return readTotal;
	}

	@Override
	public void write(int v) throws IOException {
		refillBufferIfOver();
		buffer[bIdx++] = (byte) v;
		bWritten = true;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		refillBufferIfOver();
		bWritten = true;
		int availForWriteBuf = buffer.length - bIdx;
		if (len <= availForWriteBuf) {
			System.arraycopy(b, off, buffer, bIdx, len);
			bIdx += len;
		} else {
			System.arraycopy(b, off, buffer, bIdx, availForWriteBuf);
			bIdx += availForWriteBuf;
			flushBuffer();
			super.write(b, off + availForWriteBuf, len - availForWriteBuf);
			seek(getPositionBase());
		}
	}

	@Override
	public int getPosition() {
		return (int)(bStmPos + bIdx);
	}

	private int getPositionBase() throws IOException {
		return super.getPosition();
	}

	@Override
	public void seek(long position) throws IOException {
		if (position < 0) {
			throw new EOFException("Negative seek offset! - " + Long.toHexString(position));
		}
		if (bInitialized && position < bStmPos + buffer.length && position > bStmPos) {
			IOCommon.debugPrint("Seeking bufferless to " + Long.toHexString(position));
			bIdxMax = Math.max(bIdx, bIdxMax);
			bIdx = (int)(position - bStmPos);
		} else {
			flushBuffer();
			bStmPos = position;
			bIdx = 0;
			bIdxMax = 0;
			seekBase(position);
			IOCommon.debugPrint("Seeking buffered to " + Long.toHexString(position));
			bInitialized = true;
			streamLimit = readBase(buffer, 0, buffer.length);
		}
	}

	@Override
	public int skipBytes(int amount) throws IOException {
		seek(getPosition() + amount);
		return amount;
	}

	private void seekBase(long position) throws IOException {
		super.seek(position);
	}

	@Override
	public void close() throws IOException {
		flushBuffer();
		super.close();
	}

	@Override
	public int getLength() {
		return Math.max(getPosition(), super.getLength());
	}

	private int readBase(byte[] b, int off, int len) throws IOException {
		return super.read(b, off, len);
	}

	private void writeBase(long where, byte[] b, int off, int len) throws IOException {
		seekBase(where);
		IOCommon.debugPrint("Writing " + len + " bytes at " + Integer.toHexString(super.getPosition()));
		super.write(b, off, len);
	}
	
	public byte[] DEBUG_getBuffer() {
		return buffer;
	}
	
	public long DEBUG_getBIdx() {
		return bIdx;
	}
}
