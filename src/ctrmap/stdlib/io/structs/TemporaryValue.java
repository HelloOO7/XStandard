package ctrmap.stdlib.io.structs;

import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;

public class TemporaryValue {

	private int position;

	protected DataIOStream dosref;
	
	public TemporaryValue(DataIOStream dos) throws IOException {
		dosref = dos;
		position = dos.getPosition();
		writePointer(0);
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
