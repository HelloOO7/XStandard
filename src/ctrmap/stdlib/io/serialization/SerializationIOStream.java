package ctrmap.stdlib.io.serialization;

import ctrmap.stdlib.io.base.iface.IOStream;
import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;

public class SerializationIOStream extends DataIOStream {
	
	private long lastTracedSeek = -1;
	
	public SerializationIOStream(IOStream ios) {
		super(ios);
	}
	
	public SerializationIOStream(byte[] bytes){
		super(bytes);
	}
	
	public void resetSeekTrace(){
		lastTracedSeek = -1;
	}
	
	public long getMaxSeekSinceTrace() throws IOException {
		return Math.max(getPositionUnbased(), lastTracedSeek);
	}
	
	@Override
	public void seek(long pos) throws IOException {
		lastTracedSeek = Math.max(lastTracedSeek, Math.max(getPositionUnbased(), pos));
		super.seek(pos);
	}
}
