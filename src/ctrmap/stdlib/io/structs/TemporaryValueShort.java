package ctrmap.stdlib.io.structs;

import ctrmap.stdlib.io.iface.SeekableDataOutput;
import java.io.IOException;

public class TemporaryValueShort extends TemporaryValue {

	public TemporaryValueShort(SeekableDataOutput dos) throws IOException {
		super(dos);
	}
	
	@Override
	protected void writePointer(int value) throws IOException {
		dosref.writeShort(value);
	}
}
