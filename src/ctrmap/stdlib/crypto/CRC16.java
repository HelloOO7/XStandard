package ctrmap.stdlib.crypto;

import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;

/**
 * CRC16 Checksum calculation.
 */
public class CRC16 {

	/**
	 * Calculate a CRC16_CCIITT checksum of a stream.Taken from Checksums.cs in
	 * kwsch/PKHex.
	 *
	 * @param in A DataIOStream to read data from.
	 * @param off Starting position in the stream.
	 * @param len Number of bytes to hash.
	 * @return Checksum of the data region in the stream.
	 * @throws java.io.IOException
	 */
	public static int CRC16_CCIITT(DataIOStream in, int off, int len) throws IOException {
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
