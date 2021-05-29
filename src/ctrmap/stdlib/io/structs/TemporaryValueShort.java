package ctrmap.stdlib.io.structs;

import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;

public class TemporaryValueShort extends TemporaryValue {

	public TemporaryValueShort(DataIOStream dos) throws IOException {
		super(dos);
	}
	
	@Override
	protected void writePointer(int value) throws IOException {
		dosref.writeShort(value);
	}
}
