package ctrmap.stdlib.io.iface;

import java.io.DataInput;
import java.io.IOException;

public interface CloseableDataInput extends DataInput {
	public int getPosition() throws IOException;
	public void close() throws IOException;
}
