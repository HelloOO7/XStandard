package xstandard.io.base.iface;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface IOStream extends ReadableStream, WriteableStream, Seekable, Closeable {
	public default byte[] toByteArray() {
		try {
			int pos = getPosition();
			byte[] bytes = new byte[getLength()];
			seek(0);
			read(bytes);
			seek(pos);
			return bytes;
		} catch (IOException ex) {
			Logger.getLogger(IOStream.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
