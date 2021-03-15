package ctrmap.stdlib.crypto;

import ctrmap.stdlib.io.iface.SeekableDataInput;
import java.io.IOException;

/**
 *
 */
public class CRC16 {

	/*
	Checksums.cs in PKHex.
	 */
	public static int CRC16_CCIITT(SeekableDataInput in, int off, int len) throws IOException {
		int top = 0xFF;
		int bot = 0xFF;
		in.seek(off);
		for (int i = 0; i < len; i++) {
			int x = in.readUnsignedByte() ^ top;
			x ^= (x >> 4);
			top = (bot ^ (x >> 3) ^ (x << 4)) & 0xFF;
			bot = (x ^ (x << 5)) & 0xFF;
		}
		return (((top << 8) & 0xFF00) | (bot & 0xFF));
	}
}
