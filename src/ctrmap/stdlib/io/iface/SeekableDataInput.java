package ctrmap.stdlib.io.iface;

import java.io.IOException;

public interface SeekableDataInput extends PositionedDataInput{
	public void seek(int addr) throws IOException;
}
