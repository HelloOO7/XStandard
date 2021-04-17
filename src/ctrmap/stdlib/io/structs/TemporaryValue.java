package ctrmap.stdlib.io.structs;

import ctrmap.stdlib.io.iface.SeekableDataOutput;
import java.io.IOException;

public class TemporaryValue {

	private int position;

	protected SeekableDataOutput dosref;
	
	public TemporaryValue(SeekableDataOutput dos) throws IOException {
		dosref = dos;
		position = dos.getPosition();
		dos.writeInt(0);
	}

	public void set(int value) throws IOException {
		int rememberpos = dosref.getPosition();
		dosref.seek(position);
		writePointer(value);
		dosref.seek(rememberpos);
	}
	
	protected void writePointer(int value) throws IOException {
		dosref.writeInt(value);
	}
}
