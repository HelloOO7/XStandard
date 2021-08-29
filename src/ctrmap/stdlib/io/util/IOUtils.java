package ctrmap.stdlib.io.util;

import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IOUtils {

	public static byte[] integerToByteArrayBE(int i) {
		return new byte[]{(byte) (i >>> 24), (byte) (i >>> 16), (byte) (i >>> 8), (byte) i};
	}

	public static byte[] integerToByteArrayLE(int i, byte[] arr, int off) {
		arr[off + 3] = (byte) (i >>> 24);
		arr[off + 2] = (byte) (i >>> 16);
		arr[off + 1] = (byte) (i >>> 8);
		arr[off + 0] = (byte) i;
		return arr;
	}

	public static byte[] floatArrayToByteArray(float[] floats, int floatsOff, byte[] bytes, int bytesOff, int count) {
		for (int fIdx = floatsOff, bIdx = bytesOff; fIdx < count && fIdx < floats.length; fIdx++, bIdx += 4) {
			if (bIdx + 3 >= bytes.length){
				break;
			}
			int reinterpretFloat = Float.floatToIntBits(floats[fIdx]);
			
			integerToByteArrayLE(reinterpretFloat, bytes, bIdx);
		}
		return bytes;
	}

	public static int byteArrayToIntegerBE(byte[] b) {
		return byteArrayToIntegerBE(b, 0);
	}

	public static int byteArrayToIntegerBE(byte[] b, int offs) {
		int x = b[offs];
		x = (x << 8) | (b[offs + 1] & 255);
		x = (x << 8) | (b[offs + 2] & 255);
		x = (x << 8) | (b[offs + 3] & 255);
		return x;
	}

	public static int byteArrayToIntegerLE(byte[] b, int offs) {
		return (b[offs] & 0xFF) | ((b[offs + 1] & 0xFF) << 8) | ((b[offs + 2] & 0xFF) << 16) | ((b[offs + 3] & 0xFF) << 24);
	}
	
	public static int byteArrayToInteger24LE(byte[] b, int offs) {
		return (b[offs] & 0xFF) | ((b[offs + 1] & 0xFF) << 8) | ((b[offs + 2] & 0xFF) << 16);
	}
	
	public static long byteArrayToLongLE(byte[] b, int offs) {
		return (((long)b[offs + 7] << 56) +
                ((long)(b[offs + 6] & 255) << 48) +
                ((long)(b[offs + 5] & 255) << 40) +
                ((long)(b[offs + 4] & 255) << 32) +
                ((long)(b[offs + 3] & 255) << 24) +
                ((b[offs + 2] & 255) << 16) +
                ((b[offs + 1] & 255) <<  8) +
                ((b[offs + 0] & 255) <<  0));
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

	public static SearchResult searchForInt32(DataIOStream in, int startPos, int endPos, int value) throws IOException {
		return searchForBytes(in, startPos, endPos, new SearchPattern(ByteBuffer.wrap(new byte[4]).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()));
	}

	public static SearchResult searchForBytes(DataIOStream in, int startPos, int endPos, SearchPattern... patterns) throws IOException {
		if (patterns.length == 0) {
			throw new IllegalArgumentException("At least one pattern has to be provided.");
		}
		int pos = in.getPosition();
		int inLength = in.getLength();
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
