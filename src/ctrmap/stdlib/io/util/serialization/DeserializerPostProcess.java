package ctrmap.stdlib.io.util.serialization;

import java.io.IOException;

public interface DeserializerPostProcess {
	public void deserialize(DeserializerState state) throws IOException;
}
