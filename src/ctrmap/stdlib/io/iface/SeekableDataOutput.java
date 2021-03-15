package ctrmap.stdlib.io.iface;

import java.io.IOException;

public interface SeekableDataOutput extends PositionedDataOutput{
	public void seek(int addr) throws IOException;
}
