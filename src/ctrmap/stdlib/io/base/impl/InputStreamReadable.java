package ctrmap.stdlib.io.base.impl;

import java.io.IOException;
import java.io.InputStream;
import ctrmap.stdlib.io.base.iface.ReadableStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InputStreamReadable implements ReadableStream {

	protected InputStream in;

	private int position = 0;

	public InputStreamReadable(InputStream in) {
		this.in = in;
	}

	@Override
	public int getPosition() throws IOException {
		return position;
	}

	@Override
	public int read() throws IOException {
		position++;
		return in.read();
	}

	@Override
	public int skipBytes(int amount) throws IOException {
		/*
		WORKAROUND
		
		The spec for InputStream.skip(int) permits not actually skipping the whole amount, which BufferedInputStream abuses in a way that the final amount is maximally the buffer size.
		*/
		
		int remaining = amount;
		int lastRemaining;
		
		while (remaining > 0){
			lastRemaining = remaining;
			remaining -= (int)in.skip(remaining);
			if (lastRemaining == remaining){
				break;
			}
		}
		
		int r = amount - remaining;
		
		position += r;
		return r;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int r = in.read(b, off, len);
		position += r;
		return r;
	}

	@Override
	public InputStream getInputStream() {
		return in;
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

	@Override
	public int getLength() {
		try {
			return in.available() + position;
		} catch (IOException ex) {
			Logger.getLogger(InputStreamReadable.class.getName()).log(Level.SEVERE, null, ex);
		}
		return 0;
	}
}
