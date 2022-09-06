package xstandard.io.base.iface;

import java.io.IOException;

public interface Seekable extends Positioned {
    public void seek(long position) throws IOException;
	public void setLength(long length) throws IOException;
}
