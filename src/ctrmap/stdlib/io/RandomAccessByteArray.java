package ctrmap.stdlib.io;

import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.io.base.LittleEndianIO;
import ctrmap.stdlib.io.iface.SeekableDataOutput;
import ctrmap.stdlib.io.iface.SeekableDataInput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * A class to imitate RandomAccessFile for byte array streams
 */
public class RandomAccessByteArray extends LittleEndianIO {

	private RandomAccessBAOS.Wrapper baos;
	private RandomAccessBAIS.Wrapper bais;

	public RandomAccessByteArray(byte[] ba) {
		this(new RandomAccessBAIS.Wrapper(ba));
	}

	public RandomAccessByteArray() {
		this(new byte[0]);
	}

	private RandomAccessByteArray(RandomAccessBAIS.Wrapper bais) {
		this(bais, new RandomAccessBAOS.Wrapper(new RandomAccessBAOS(bais.bais_handle)));
	}

	private RandomAccessByteArray(RandomAccessBAIS.Wrapper bais, RandomAccessBAOS.Wrapper baos) {
		super(bais, baos);
		this.bais = bais;
		this.baos = baos;
	}

	public RandomAccessByteArray(InputStream strm) {
		this(FSUtil.readStreamToBytes(strm));
	}

	public InputStream getBaseInputStream() {
		return bais.bais_handle;
	}

	public OutputStream getBaseOutputStream() {
		return baos.out;
	}

	public byte[] toByteArray() {
		return baos.toByteArray();
	}

	@Override
	public int length() throws IOException {
		return baos.out.lastOffset;
	}

	public static class RandomAccessBAIS extends ByteArrayInputStream {

		public RandomAccessBAIS(byte[] bytes) {
			super(bytes);
		}

		public void seek(int addr) throws IOException {
			pos = addr;
		}

		public int getPosition() throws IOException {
			return pos;
		}

		private byte[] getBuffer() {
			return buf;
		}

		private void setBuffer(byte[] buf) {
			this.buf = buf;
			this.count = buf.length;
		}
		
		@Override
		public int read(byte[] b, int off, int len){
			return super.read(b, off, len);
		}

		public static class Wrapper extends DataInputStream implements SeekableDataInput {

			private RandomAccessBAIS bais_handle;

			public Wrapper(byte[] bytes) {
				this(new RandomAccessBAIS(bytes));
			}

			public Wrapper(RandomAccessBAIS bais) {
				super(bais);
				bais_handle = bais;
			}

			@Override
			public void seek(int addr) throws IOException {
				bais_handle.seek(addr);
			}

			@Override
			public int getPosition() throws IOException {
				return bais_handle.getPosition();
			}
		}
	}

	public static class RandomAccessBAOS extends ByteArrayOutputStream {

		private int lastOffset = -1;
		private CloseListener cl;
		private RandomAccessBAIS subIS;

		public RandomAccessBAOS() {
			super();
		}

		public RandomAccessBAOS(byte[] b) {
			this();
			if (b != null) {
				buf = b;
				lastOffset = buf.length;
			}
		}

		public RandomAccessBAOS(RandomAccessBAIS subIS) {
			this(subIS.getBuffer());
			this.subIS = subIS;
		}

		public RandomAccessBAOS(byte[] b, CloseListener cl) {
			this(b);
			this.cl = cl;
		}

		public void seek(int off) {
			lastOffset = Math.max(lastOffset, size()); //store this in case the stream didn't return so that we can trim the byte array according to it
			super.count = off;
		}

		@Override
		public void write(int value) {
			byte[] oldBuf = buf;
			super.write(value);
			if (buf != oldBuf && subIS != null) {
				subIS.setBuffer(buf);
			}
		}

		@Override
		public void write(byte[] bytes, int off, int len) {
			byte[] oldBuf = buf;
			super.write(bytes, off, len);
			if (buf != oldBuf && subIS != null) {
				subIS.setBuffer(buf);
			}
		}

		@Override
		public byte[] toByteArray() {
			if (lastOffset == -1 || lastOffset < count) {
				lastOffset = count;
			}
			return Arrays.copyOf(super.buf, lastOffset);
		}

		@Override
		public void close() throws IOException {
			super.close();
			if (cl != null) {
				cl.onClose(toByteArray());
			}
		}

		private static class Wrapper extends DataOutputStream implements SeekableDataOutput {

			private RandomAccessBAOS out;

			public Wrapper() {
				this(new RandomAccessBAOS());
			}

			public Wrapper(byte[] bytes) {
				this(new RandomAccessBAOS(bytes));
			}

			public Wrapper(RandomAccessBAOS out) {
				super(out);
				this.out = out;
			}

			@Override
			public void seek(int off) {
				out.seek(off);
			}

			@Override
			public int getPosition() {
				return out.size();
			}

			public byte[] toByteArray() {
				return out.toByteArray();
			}
		}

		public static interface CloseListener {

			public void onClose(byte[] buf);
		}
	}
}
