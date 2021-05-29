package ctrmap.stdlib.io.base.iface;

import java.io.IOException;

public interface Seekable extends Positioned {
    public void seek(int position) throws IOException;
}
