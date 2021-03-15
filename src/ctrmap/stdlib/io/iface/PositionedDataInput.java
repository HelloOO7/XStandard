package ctrmap.stdlib.io.iface;

import java.io.DataInput;
import java.io.IOException;

public interface PositionedDataInput extends DataInput{
	public int getPosition() throws IOException;
}
