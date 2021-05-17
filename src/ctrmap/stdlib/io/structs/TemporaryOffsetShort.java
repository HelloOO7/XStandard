package ctrmap.stdlib.io.structs;

import ctrmap.stdlib.io.iface.SeekableDataOutput;
import java.io.IOException;

public class TemporaryOffsetShort extends TemporaryOffset {

	public TemporaryOffsetShort(SeekableDataOutput dos) throws IOException {
		super(dos);
	}
	
	public TemporaryOffsetShort(SeekableDataOutput dos, int base) throws IOException {
		super(dos, base);
	}
	
	@Override
	protected void writePointer(int value) throws IOException {
		dosref.writeShort(value);
	}
}
