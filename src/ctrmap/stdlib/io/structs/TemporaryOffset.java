package ctrmap.stdlib.io.structs;

import ctrmap.stdlib.io.iface.SeekableDataOutput;
import java.io.IOException;

public class TemporaryOffset extends TemporaryValue {

	private final int base;

	public TemporaryOffset(SeekableDataOutput dos) throws IOException {
		this(dos, 0);
	}
	
	public TemporaryOffset(SeekableDataOutput dos, int offsetBase) throws IOException {
		super(dos);
		this.base = offsetBase;
	}

	public void setHere() throws IOException {
		set(dosref.getPosition() + base);
	}
}
