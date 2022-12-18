package xstandard.io.util;

import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IOUtils {


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
		return searchForInt32(in, startPos, endPos, 1, value);
	}
	
	public static SearchResult searchForInt32(DataIOStream in, int startPos, int endPos, int align, int value) throws IOException {
		return searchForBytes(in, startPos, endPos, align, new SearchPattern(ByteBuffer.wrap(new byte[4]).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()));
	}

	public static SearchResult searchForBytes(DataIOStream in, int startPos, int endPos, SearchPattern... patterns) throws IOException {
		return searchForBytes(in, startPos, endPos, 1, patterns);
	}
	
	public static SearchResult searchForBytes(DataIOStream in, int startPos, int endPos, int align, SearchPattern... patterns) throws IOException {
		if (patterns.length == 0) {
			throw new IllegalArgumentException("At least one pattern has to be provided.");
		}
		int pos = startPos == -1 ? in.getPosition() : startPos;
		int inLength = endPos == -1 ? in.getLength() : endPos;

		int[] ptnCaps = new int[patterns.length];
		int ptnLengthMax = 0;
		for (int i = 0; i < patterns.length; i++) {
			ptnCaps[i] = inLength - patterns[i].patternBytes.length;
			ptnLengthMax = Math.max(ptnLengthMax, patterns[i].patternBytes.length);
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

			pos += align;
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
