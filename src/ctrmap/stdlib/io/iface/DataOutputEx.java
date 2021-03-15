package ctrmap.stdlib.io.iface;

import java.io.DataOutput;
import java.io.IOException;

public interface DataOutputEx extends DataOutput{
	public void pad(int amount) throws IOException;
}
