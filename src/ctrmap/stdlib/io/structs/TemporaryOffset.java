package ctrmap.stdlib.io.structs;

import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;

public class TemporaryOffset extends TemporaryValue {

	private final int base;

	public TemporaryOffset(DataIOStream dos) throws IOException {
		this(dos, 0);
	}
	
	public TemporaryOffset(DataIOStream dos, int offsetBase) throws IOException {
		super(dos);
		this.base = offsetBase;
	}

	public void setHere() throws IOException {
		set(dosref.getPosition() + base);
	}
}
