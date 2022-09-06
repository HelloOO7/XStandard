package xstandard.io.structs;

import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;

/**
 * 16-bit version TemporaryValue.
 */
public class TemporaryValueShort extends TemporaryValue {

	/**
	 * Creates a TemporaryValue at the current position in a stream.
	 *
	 * @param dos A data stream.
	 * @throws IOException
	 */
	public TemporaryValueShort(DataIOStream dos) throws IOException {
		super(dos);
	}

	@Override
	protected void writePointer(int value) throws IOException {
		dosref.writeShort(value);
	}
}
