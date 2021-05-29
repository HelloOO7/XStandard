package ctrmap.stdlib.io.base.iface;

import java.io.IOException;

public interface Positioned {
    public int getPosition() throws IOException;
	public int getLength();
	
	public default int available() throws IOException {
		return getLength() - getPosition();
	}
}
