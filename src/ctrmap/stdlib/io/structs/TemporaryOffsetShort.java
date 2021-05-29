package ctrmap.stdlib.io.structs;

import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;

public class TemporaryOffsetShort extends TemporaryOffset {

	public TemporaryOffsetShort(DataIOStream dos) throws IOException {
		super(dos);
	}
	
	public TemporaryOffsetShort(DataIOStream dos, int base) throws IOException {
		super(dos, base);
	}
	
	@Override
	protected void writePointer(int value) throws IOException {
		dosref.writeShort(value);
	}
}
