package xstandard.io.structs;

import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;

/**
 * A TemporaryOffset that uses 16-bit pointers.
 */
public class TemporaryOffsetShort extends TemporaryOffset {

	/**
	 * Creates a 16-bit TemporaryOffset in a data stream with default offset base of 0.
	 * @param dos A data stream.
	 * @throws IOException 
	 */
	public TemporaryOffsetShort(DataIOStream dos) throws IOException {
		super(dos);
	}
	
	/**
	 * Creates a 16-bit TemporaryOffset in a data stream with a user-specified offset base.
	 * @param dos A data stream.
	 * @param base Offset base to add to values.
	 * @throws IOException 
	 */
	public TemporaryOffsetShort(DataIOStream dos, int base) throws IOException {
		super(dos, base);
	}
	
	@Override
	protected void writePointer(int value) throws IOException {
		dosref.writeShort(value);
	}
}
