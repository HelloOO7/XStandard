package ctrmap.stdlib.io;

import ctrmap.stdlib.io.base.LittleEndianDataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Simple DataOutputStream implementation with automatic reversion of Java BE to
 * LE data.
 */
public class LittleEndianDataOutputStream extends LittleEndianDataOutput {

	private DataOutputStream dos_handle;

	public LittleEndianDataOutputStream(OutputStream out) {
		super(out);
		dos_handle = new DataOutputStream(out);
	}

	public void close() throws IOException {
		dos_handle.close();
	}

	public void flush() throws IOException {
		dos_handle.flush();
	}
}
