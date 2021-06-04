package ctrmap.stdlib.io.base.impl.ext;

import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.fs.accessors.DiskFile;
import ctrmap.stdlib.io.IOCommon;
import ctrmap.stdlib.io.base.impl.IOStreamWrapper;
import ctrmap.stdlib.io.base.iface.IOStream;
import java.io.EOFException;

import java.io.IOException;

public class BufferedIOStream extends IOStreamWrapper {

	private byte[] buffer;

	private int bIdx = 0;
	private int bStmPos = 0;

	private boolean bInitialized = false;
	private boolean bWritten = false;

	/**
	 * Creates a buffered IO stream handle of an IOStream with the default buffer size.
	 * @param strm An IOStream.
	 */
	public BufferedIOStream(IOStream strm) {
		this(strm, 128); //The buffer size is smaller than on Buffered***Streams, because we tend to seek often
	}

	/**
	 * Creates a buffered IO stream handle of an IOStream with a given buffer size.
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
	 * @throws IOException 
	 */
	private void flushBuffer() throws IOException {
		if (bWritten) {
			IOCommon.debugPrint("Flushing " + bIdx + " buf bytes at " + Integer.toHexString(bStmPos));
			writeBase(bStmPos, buffer, 0, bIdx);
			bWritten = false;
		}
	}

	/**
	 * Advances the stream position of a buffer if its capacity has been exceeded.
	 * If the buffer is uninitialized, it is force-refilled, but stream position remains unchanged.
	 * @throws IOException 
	 */
	private void refillBufferIfOver() throws IOException {
		boolean isBufOver = bIdx >= buffer.length;
		boolean isBufUnder = !bInitialized;
		if (isBufOver || isBufUnder) {
			if (isBufOver) {
				IOCommon.debugPrint(this + " | Buffer size is over !! - " + Integer.toHexString(bStmPos));
				//Flush the existing buffer if exceeded
				flushBuffer();
				bStmPos += buffer.length;
			} else {
				IOCommon.debugPrint(this + " | Buffer size is under !! - " + Integer.toHexString(bStmPos));
				//No existing buffer - just read out the data and set the initialized flag
				bStmPos = 0;
				bInitialized = true;
			}
			IOCommon.debugPrint("Refilling buffer from pos " + Integer.toHexString(getPositionBase()));
			readBase(buffer, 0, buffer.length);
			bIdx = 0;
		}
	}

	
	@Override
	public int read() throws IOException {
		refillBufferIfOver();
		return buffer[bIdx++] & 0xFF;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		refillBufferIfOver();
		
		//Bytes available to read from the current buffer
		int availForReadBuf = buffer.length - bIdx;
		//An accumulator for the return value
		int readTotal = availForReadBuf;
		
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
				readTotal += readBase(buffer, 0, buffer.length);
				System.arraycopy(buffer, 0, b, off + availForReadBuf, leftToRead);
				bStmPos += buffer.length;
				bIdx = leftToRead;
				return readTotal;
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
		return bStmPos + bIdx;
	}

	private int getPositionBase() throws IOException {
		return super.getPosition();
	}

	@Override
	public void seek(int position) throws IOException {
		if (position < 0) {
			throw new EOFException("Negative seek offset! - " + Integer.toHexString(position));
		}
		if (bInitialized && position < bStmPos + buffer.length && position > bStmPos) {
			IOCommon.debugPrint("Seeking bufferless to " + Integer.toHexString(position));
			bIdx = position - bStmPos;
		} else {
			flushBuffer();
			bStmPos = position;
			bIdx = 0;
			seekBase(position);
			IOCommon.debugPrint("Seeking buffered to " + Integer.toHexString(position));
			bInitialized = true;
			readBase(buffer, 0, buffer.length);
		}
	}

	@Override
	public int skipBytes(int amount) throws IOException {
		seek(getPosition() + amount);
		return amount;
	}

	private void seekBase(int position) throws IOException {
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

	private void writeBase(int where, byte[] b, int off, int len) throws IOException {
		seekBase(where);
		IOCommon.debugPrint("Writing " + len + " bytes at " + Integer.toHexString(super.getPosition()));
		super.write(b, off, len);
	}
}
