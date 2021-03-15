package ctrmap.stdlib.io.util;

import ctrmap.stdlib.io.base.IOWrapper;
import ctrmap.stdlib.io.iface.SeekableDataInput;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class BitUtils {

	public static int readUInt24LE(DataInput in) throws IOException {
		return (int) (in.readUnsignedByte()
				| in.readUnsignedByte() << 8
				| in.readUnsignedByte() << 16);
	}

	public static int readInt24LE(DataInput in) throws IOException {
		return signExtend(readUInt24LE(in), 24);
	}

	public static void writeInt24LE(int v, DataOutput out) throws IOException {
		out.write(v & 0xFF);
		out.write((v >> 8) & 0xFF);
		out.write((v >> 16));
	}

	public static int signExtend(int value, int bits) {
		int output = (int) value;
		boolean sign = (value & (1 << (bits - 1))) > 0;
		if (sign) {
			output -= (1 << bits);
		}
		return output;
	}

	public static byte[] integerToByteArray(int i) {
		return new byte[]{(byte) (i >>> 24), (byte) (i >>> 16), (byte) (i >>> 8), (byte) i};
	}

	public static int byteArrayToInteger(byte[] b) {
		return byteArrayToInteger(b, 0);
	}

	public static int byteArrayToInteger(byte[] b, int offs) {
		int x = b[offs];
		x = (x << 8) | (b[offs + 1] & 255);
		x = (x << 8) | (b[offs + 2] & 255);
		x = (x << 8) | (b[offs + 3] & 255);
		return x;
	}

	public static byte[] getPadding(int offsetInPack, int length) {
		int endingOffset = (int) Math.ceil((offsetInPack + length) / 128.0F) * 128;
		return new byte[endingOffset - offsetInPack - length];
	}

	public static int getPaddedInteger(int v, int padTo) {
		v += (padTo - (v % padTo)) % padTo;
		return v;
	}

	public static byte[] getTrimmedArray(byte[] in) {
		for (int i = in.length - 1; i > 0; i--) {
			if (in[i] != 0) {
				int finalIndex = i + (4 - (i % 4));
				byte[] ret = new byte[finalIndex];
				System.arraycopy(in, 0, ret, 0, finalIndex);
				return ret;
			}
		}
		return new byte[0];
	}

	public static boolean isByteArrayNotEmpty(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			if (b[i] != 0) {
				return true;
			}
		}
		return false;
	}

	public static SearchResult searchForInt32(IOWrapper in, int startPos, int endPos, int value) throws IOException {
		return searchForBytes(in, startPos, endPos, new SearchPattern(ByteBuffer.wrap(new byte[4]).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()));
	}

	public static SearchResult searchForBytes(IOWrapper in, int startPos, int endPos, SearchPattern... patterns) throws IOException {
		if (patterns.length == 0) {
			throw new IllegalArgumentException("At least one pattern has to be provided.");
		}
		int pos = in.getPosition();
		int inLength = in.length();
		if (endPos != -1) {
			inLength = endPos;
		}

		int[] ptnCaps = new int[patterns.length];
		int ptnLengthMax = 0;
		for (int i = 0; i < patterns.length; i++) {
			ptnCaps[i] = inLength - patterns[i].patternBytes.length;
			ptnLengthMax = Math.max(ptnLengthMax, patterns[i].patternBytes.length);
		}

		if (startPos != -1) {
			pos = startPos;
		}

		byte[] buffer = new byte[ptnLengthMax];
		boolean valid;
		SearchPattern ptn;

		while (pos < inLength) {
			in.seek(pos);
			in.readFully(buffer, 0, Math.min(buffer.length, inLength - pos));

			for (int i = 0; i < patterns.length; i++) {
				ptn = patterns[i];
				valid = true;
				if (pos < ptnCaps[i]) {
					for (int j = 0; j < ptn.patternBytes.length; j++) {
						if ((buffer[j] & ptn.patternMask[j]) != ptn.patternBytes[j]) {
							valid = false;
							break;
						}
					}
					if (valid) {
						in.seek(pos);
						return new SearchResult(ptn, buffer);
					}
				}
			}

			pos++;
		}

		return null;
	}

	public static class SearchPattern {

		public final byte[] patternBytes;
		public final byte[] patternMask;

		public SearchPattern(byte[] bytes) {
			this(bytes, null);
		}

		public SearchPattern(byte[] bytes, byte[] mask) {
			if (mask != null && bytes.length != mask.length) {
				throw new IllegalArgumentException("The number of bytes in the mask and pattern should match.");
			}
			patternBytes = bytes;
			if (mask != null) {
				for (int i = 0; i < patternBytes.length; i++) {
					patternBytes[i] = (byte) (patternBytes[i] & mask[i]);
				}
			} else {
				mask = new byte[bytes.length];
				for (int i = 0; i < mask.length; i++) {
					mask[i] = -1;
				}
			}
			patternMask = mask;
		}
	}

	public static class SearchResult {

		public final SearchPattern matchedPattern;
		public final byte[] matchedBytes;

		public SearchResult(SearchPattern ptn, byte[] bytes) {
			matchedPattern = ptn;
			matchedBytes = bytes;
		}
	}
}
