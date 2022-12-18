package xstandard.io.util;

public class BitConverter {

	public static byte[] fromFloatArrayLE(float[] floats, int floatsOff, byte[] bytes, int bytesOff, int count) {
		for (int fIdx = floatsOff, bIdx = bytesOff; fIdx < count && fIdx < floats.length; fIdx++, bIdx += 4) {
			if (bIdx + 3 >= bytes.length) {
				break;
			}
			fromFloatLE(floats[fIdx], bytes, bIdx);
		}
		return bytes;
	}

	public static byte[] fromInt16LE(int i, byte[] arr, int off) {
		arr[off + 1] = (byte) (i >>> 8);
		arr[off + 0] = (byte) i;
		return arr;
	}

	public static byte[] fromInt16BE(int i, byte[] arr, int off) {
		arr[off + 0] = (byte) (i >>> 8);
		arr[off + 1] = (byte) i;
		return arr;
	}

	public static short toInt16LE(byte[] b, int offs) {
		return (short) ((b[offs] & 255) | ((b[offs + 1] & 255) << 8));
	}

	public static int toIntLE(byte[] b, int offs, int count) {
		int value = 0;
		for (int i = 0, j = offs; i < count; i++, j++) {
			value |= ((b[j] & 255) << (i << 3));
		}
		return value;
	}

	public static int toInt24LE(byte[] b, int offs) {
		return (b[offs] & 255) | ((b[offs + 1] & 255) << 8) | ((b[offs + 2] & 255) << 16);
	}

	public static long toInt64LE(byte[] b, int offs) {
		return ((long) b[offs + 7] << 56) + ((long) (b[offs + 6] & 255) << 48) + ((long) (b[offs + 5] & 255) << 40) + ((long) (b[offs + 4] & 255) << 32) + ((long) (b[offs + 3] & 255) << 24) + ((b[offs + 2] & 255) << 16) + ((b[offs + 1] & 255) << 8) + ((b[offs + 0] & 255) << 0);
	}

	public static long toInt64BE(byte[] b, int offs) {
		return ((long) b[offs] << 56) + ((long) (b[offs + 1] & 255) << 48) + ((long) (b[offs + 2] & 255) << 40) + ((long) (b[offs + 3] & 255) << 32) + ((long) (b[offs + 4] & 255) << 24) + ((b[offs + 5] & 255) << 16) + ((b[offs + 6] & 255) << 8) + ((b[offs + 7] & 255) << 0);
	}

	public static int toInt32LE(byte[] b, int offs) {
		return (b[offs] & 255) | ((b[offs + 1] & 255) << 8) | ((b[offs + 2] & 255) << 16) | ((b[offs + 3] & 255) << 24);
	}

	public static byte[] fromInt32BE(int i) {
		return new byte[]{(byte) (i >>> 24), (byte) (i >>> 16), (byte) (i >>> 8), (byte) i};
	}

	public static int toInt32BE(byte[] b) {
		return toInt32BE(b, 0);
	}

	public static int toInt32BE(byte[] b, int offs) {
		int x = b[offs];
		x = (x << 8) | (b[offs + 1] & 255);
		x = (x << 8) | (b[offs + 2] & 255);
		x = (x << 8) | (b[offs + 3] & 255);
		return x;
	}

	public static byte[] fromInt32LE(int i, byte[] arr, int off) {
		arr[off + 3] = (byte) (i >>> 24);
		arr[off + 2] = (byte) (i >>> 16);
		arr[off + 1] = (byte) (i >>> 8);
		arr[off + 0] = (byte) i;
		return arr;
	}

	public static byte[] fromIntLE(int value, byte[] b, int offs, int size) {
		for (int i = 0; i < size; i++, offs++) {
			b[offs] = (byte) (value & 255);
			value >>= 8;
		}
		return b;
	}

	public static byte[] fromFloatLE(float f, byte[] arr, int off) {
		fromInt32LE(Float.floatToRawIntBits(f), arr, off);
		return arr;
	}

	public static float toFloatLE(byte[] b, int offs) {
		return Float.intBitsToFloat(toInt32LE(b, offs));
	}
}
