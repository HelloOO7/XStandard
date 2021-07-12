package ctrmap.stdlib.io.structs;

import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;

/**
 * A simple data element that remembers a stream position and allows to write to it later.
 */
public class TemporaryValue {

	private int position;

	protected DataIOStream dosref;
	
	/**
	 * Creates a TemporaryValue at the current position in a stream.
	 * @param dos A data stream.
	 * @throws IOException 
	 */
	public TemporaryValue(DataIOStream dos) throws IOException {
		dosref = dos;
		position = dos.getPositionUnbased();
		writePointer(0);
	}

	/**
	 * Sets the stream to a value at the temporary position.
	 * @param value The value to write to the stream.
	 * @throws IOException 
	 */
	public void set(int value) throws IOException {
		int rememberpos = dosref.getPosition();
		dosref.seekUnbased(position);
		writePointer(value);
		dosref.seek(rememberpos);
	}
	
	/**
	 * Overridable implementation of the method to write the temporary value.
	 * @param value The value to write.
	 * @throws IOException 
	 */
	protected void writePointer(int value) throws IOException {
		dosref.writeInt(value);
	}
}
