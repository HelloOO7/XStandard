package ctrmap.stdlib.io.iface;

import java.io.DataInput;
import java.io.IOException;

public interface DataInputEx extends DataInput{
	public void align(int amount) throws IOException;
}
