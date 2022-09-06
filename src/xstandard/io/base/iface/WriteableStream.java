package xstandard.io.base.iface;

import java.io.IOException;
import java.io.OutputStream;

public interface WriteableStream extends WriteableBase {
	public default void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}
	
	public default OutputStream getOutputStream(){
		return new WriteableStreamOutputStream(this);
	}
	
	public static class WriteableStreamOutputStream extends OutputStream {

		private WriteableStream stm;
		
		public WriteableStreamOutputStream(WriteableStream stm){
			this.stm = stm;
		}

		@Override
		public void write(int b) throws IOException {
			stm.write(b);
		}
		
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			stm.write(b, off, len);
		}
		
		@Override
		public void close() throws IOException {
			stm.close();
		}
	}
}
