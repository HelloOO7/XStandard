package ctrmap.stdlib.io.iface;

import java.io.DataOutput;
import java.io.IOException;

public interface PositionedDataOutput extends DataOutput{
	public int getPosition() throws IOException;
}
