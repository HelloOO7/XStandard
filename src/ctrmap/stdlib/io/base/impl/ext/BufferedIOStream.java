package ctrmap.stdlib.io.base.impl.ext;

import ctrmap.stdlib.io.IOCommon;
import ctrmap.stdlib.io.base.impl.IOStreamWrapper;
import ctrmap.stdlib.io.base.iface.IOStream;

import java.io.IOException;

public class BufferedIOStream extends IOStreamWrapper {

    private BufferManager mng;

    public BufferedIOStream(IOStream strm) {
        this(strm, 128); //The buffer size is smaller than on Buffered***Streams, because we tend to seek often
    }

    public BufferedIOStream(IOStream strm, int bufferSize) {
        super(strm);
        if (bufferSize < 1){
            throw new IllegalArgumentException("Buffer size can not be 0!");
        }

        mng = new BufferManager(bufferSize);
        try {
            mng.buffer.changeStreamPos(0, this);
        }
        catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    @Override
    public int read() throws IOException {
        ensureBufferData(1);
        return mng.buffer.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
		/*for (int i = off; i < off + len; i++){
			b[i] = (byte)read();
		}
		if (true){
			return len; 
		}*/
        if (ensureBufferData(len)){
            return mng.buffer.read(b, off, len);
        }
        else {
            int drainedAmount = mng.buffer.drain(b, off); //after drain, the buffer will be auto-refilled on next read
            int readAmount = super.read(b, off + drainedAmount, len - drainedAmount);
            return drainedAmount + readAmount;
        }
    }

    @Override
    public void write(int v) throws IOException {
        mng.buffer.write(v);
        flushFullBufferIfApplicable();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        /*for (int i = off; i < off + len; i++){
            write(b[i]);
        }
        if (true){
            return;
        }*/
        int fitLen = Math.min(len, mng.buffer.data.length - mng.buffer.position);
        mng.buffer.write(b, off, fitLen);
        flushFullBufferIfApplicable();
        if (len > fitLen){
            int diff = len - fitLen;
            super.write(b, off + fitLen, diff);
            mng.buffer.changeStreamPos(this.getPosition(), this);
        }
    }

    private void flushFullBufferIfApplicable() throws IOException {
        if (mng.buffer.position >= mng.buffer.data.length) {
            mng.buffer.resetAndRefill(this);
        }
    }

    @Override
    public int getPosition(){
        return mng.buffer.streamPosition + mng.buffer.position;
    }

    @Override
    public void seek(int position) throws IOException {
        if (mng.buffer.isStreamPositionInBuffer(position)){
            mng.buffer.changeStreamPos(position, this);
        }
        else {
            mng.buffer.changeStreamPos(position, this);
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
        mng.buffer.flushBuffer(this);
    }

    @Override
    public int getLength() {
        return Math.max(mng.buffer.streamPosition + mng.buffer.position, super.getLength());
    }

    private int readBase(byte[] b, int off, int len) throws IOException {
        return super.read(b, off, len);
    }

    private void writeBase(int where, byte[] b, int off, int len) throws IOException {
        int pos = super.getPosition();
        super.seek(where);
        //IOCommon.debugPrint("Writing " + len + " bytes at " + Integer.toHexString(super.getPosition()));
        super.write(b, off, len);
        super.seek(pos);
    }

    private boolean ensureBufferData(int count) throws IOException {
        if (!mng.buffer.fits(count)){
            if (count <= mng.buffer.data.length){
                mng.buffer.resetAndRefill(this);
            }
            else {
                return false;
            }
        }
        return true;
    }

    private static class BufferManager {
        private Buffer buffer;

        public BufferManager(int bufferSize){
            buffer = new Buffer(bufferSize);
        }

        private static class Buffer {
            public int streamPosition = Integer.MAX_VALUE;

            private int position;
            private byte[] data;

            private boolean written = false;

            public Buffer(int size){
                data = new byte[size];
                position = size;
            }

            public boolean isStreamPositionInBuffer(int pos){
                return pos < streamPosition + data.length && pos >= streamPosition;
            }

            public boolean fits(int count){
                return position + count <= data.length;
            }

            public void resetAndRefill(BufferedIOStream strm) throws IOException {
                IOCommon.debugPrintf("BufferedIOStream::BufferManager | Refilling buffer from pos %08X to pos %08X", streamPosition, streamPosition + data.length);
                flushBuffer(strm);
                if (position < data.length){
                    System.arraycopy(data, position, data, 0, data.length - position);
                    strm.readBase(data, data.length - position, position);
                }
                else {
                    strm.readBase(data, 0, data.length);
                }
                position = 0;
                streamPosition += data.length;
            }

            public void changeStreamPos(int newStreamPos, BufferedIOStream strm) throws IOException {
                if (isStreamPositionInBuffer(newStreamPos)){
                    IOCommon.debugPrintf("BufferedIOStream::BufferManager | Desired stream position %08X is in buffer, setting rel pos to %08X, buffer pos %08X", newStreamPos, (newStreamPos - streamPosition), streamPosition);
                    position = newStreamPos - streamPosition;
                }
                else {
                    IOCommon.debugPrintf("BufferedIOStream::BufferManager | Desired stream position %08X is not in buffer, resetting", newStreamPos);
                    flushBuffer(strm);
                    position = data.length;

                    streamPosition = newStreamPos;
                    position = 0;
                    strm.seekBase(newStreamPos);
                    strm.readBase(data, 0, data.length);
                }
            }

            public void flushBuffer(BufferedIOStream strm) throws IOException {
                if (written){
                    IOCommon.debugPrint("BufferedIOStream::BufferManager | Flushing buffer... will write " + position + " bytes at " + Integer.toHexString(streamPosition));
                    written = false;
                    strm.writeBase(streamPosition, data, 0, position);
                }
            }

            public int read(){
                return data[position++] & 0xFF;
            }

            public void write(int v){
                data[position++] = (byte)v;
                written = true;
            }

            public void write(byte[] b, int off, int len){
                System.arraycopy(b, off, data, position, len);
                position += len;
            }

            public int read(byte[] b, int off, int len){
                len = Math.min(len, data.length - position);
                System.arraycopy(data, position, b, off, len);
                position += len;
                return len;
            }

            public int drain(byte[] b, int off){
                int len = data.length - position;
                System.arraycopy(data, position, b, off, len);
                position += len;
                return len;
            }
        }
    }
}
