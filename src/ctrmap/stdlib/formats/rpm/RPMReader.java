package ctrmap.stdlib.formats.rpm;

import ctrmap.stdlib.io.base.iface.IOStream;
import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;

class RPMReader extends DataIOStream {
	public RPMReader(IOStream strm){
		super(strm);
	}
	
	private int strTableOffs = -1;
	
	public void setStrTableOffsHere() throws IOException {
		strTableOffs = getPosition();
	}
	
	@Override
	public String readStringWithAddress() throws IOException {
		if (strTableOffs == -1){
			return super.readStringWithAddress();
		}
		int addr = readUnsignedShort();
		if (addr == 0){
			return null;
		}
		addr += strTableOffs;
		int pos = getPositionUnbased();
		seek(addr);
		String str = readString();
		seekUnbased(pos);
		return str;
	}
}
