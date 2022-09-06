package xstandard.io.structs;

import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;


/**
 * A TemporaryValue extension designed for writing temporary pointers.
 */
public class TemporaryOffset extends TemporaryValue {

	private final int base;

	/**
	 * Creates a TemporaryOffset in a data stream with default offset base of 0.
	 * @param dos A data stream.
	 * @throws IOException 
	 */
	public TemporaryOffset(DataIOStream dos) throws IOException {
		this(dos, 0);
	}
	
	/**
	 * Creates a TemporaryOffset in a data stream with a user-specified offset base.
	 * @param dos A data stream.
	 * @param offsetBase Offset base to add to values.
	 * @throws IOException 
	 */
	public TemporaryOffset(DataIOStream dos, int offsetBase) throws IOException {
		super(dos);
		this.base = offsetBase;
	}

	/**
	 * Points the temporary offset to the current stream position.
	 * @throws IOException 
	 */
	public void setHere() throws IOException {
		set(dosref.getPosition() + base);
	}
	
	public void setHereSelfRelative() throws IOException {
		set(dosref.getPositionUnbased() - position + base);
	}
	
	public void setHereSelfRelativeInv() throws IOException {
		set(-(dosref.getPositionUnbased()- position + base));
	}
}
