package xstandard.io.base.iface;

import java.io.Closeable;
import java.io.IOException;

/*
WORKAROUND CLASS

The DataOutput interface has a default method for write(byte[] b) which clashes with the one in WriteableStream in DataOutputEx.
This class exists to separate the default method from the base, but does not have any effect since the affected classes inherit the default from another interface.
*/
public interface WriteableBase extends Positioned, Closeable {
    public void write(int i) throws IOException;
    public void write(byte[] b, int off, int len) throws IOException;

	public default void writePadding(int amount, int value) throws IOException {
		for (int i = 0; i < amount; i++) {
			write(value);
		}
	}
	
    public default void pad(int align) throws IOException {
		pad(align, 0);
    }
	
	public default void pad(int align, int fillByte) throws IOException {
        int mod = getPosition() % align;
		if (mod != 0) {
			writePadding(align - mod, fillByte);
		}
    }
}
