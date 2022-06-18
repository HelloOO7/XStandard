package ctrmap.stdlib.io.util;

import ctrmap.stdlib.io.IOCommon;
import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import static ctrmap.stdlib.io.util.StringIO.readString;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class StringIO {

	public static String readString(ByteBuffer in) {
		StringBuilder sb = new StringBuilder();
		int read;
		while ((read = (in.get() & 0xFF)) != 0) {
			sb.append((char) read);
		}
		return sb.toString();
	}

	public static String readString(DataInput in) throws IOException {
		StringBuilder sb = new StringBuilder();
		int read;
		while ((read = in.readUnsignedByte()) != 0) {
			sb.append((char) read);
		}
		return sb.toString();
	}

	public static String readByteLengthString(DataInput in) throws IOException {
		byte[] alloc = new byte[in.readByte()];
		in.readFully(alloc);
		return new String(alloc);
	}
	
	public static String readStringUTF16(DataInput in) throws IOException {
		StringBuilder sb = new StringBuilder();
		char read;
		while ((read = in.readChar()) != 0) {
			sb.append(read);
		}
		return sb.toString();
	}

	public static String readPaddedString(ByteBuffer in, int size) {
		return new String(readPaddedByteBuffer(in, size), StandardCharsets.US_ASCII);
	}

	public static String readPaddedString(DataInput in, int size) throws IOException {
		return new String(readPaddedByteBuffer(in, size), StandardCharsets.US_ASCII);
	}

	public static String readPaddedStringSJIS(DataInput in, int size) throws IOException {
		return new String(readPaddedByteBuffer(in, size), SJIS_CHARSET);
	}

	private static byte[] readPaddedByteBuffer(ByteBuffer in, int size) {
		byte[] buf = new byte[size];
		in.get(buf);
		int term = 0;
		for (; term < size; term++) {
			if (buf[term] == 0x00) {
				break;
			}
		}
		return Arrays.copyOfRange(buf, 0, term);
	}

	private static byte[] readPaddedByteBuffer(DataInput in, int size) throws IOException {
		byte[] buf = new byte[size];
		in.readFully(buf);
		int term = 0;
		for (; term < size; term++) {
			if (buf[term] == 0x00) {
				break;
			}
		}
		return Arrays.copyOfRange(buf, 0, term);
	}

	public static String readStringWithAddress(DataIOStream in) throws IOException {
		int addr = in.readInt();
		if (addr == 0) {
			return null;
		}
		int pos = in.getPosition();
		in.seek(addr);
		String ret = readString(in);
		in.seek(pos);
		return ret;
	}

	public static String readStringWithAddressUTF16(DataIOStream in) throws IOException {
		int addr = in.readInt();
		if (addr == 0) {
			return null;
		}
		int pos = in.getPosition();
		in.seek(addr);
		String ret = readStringUTF16(in);
		in.seek(pos);
		return ret;
	}

	private static final Charset SJIS_CHARSET = Charset.forName("SJIS");

	public static void writeString(ByteBuffer dos, String str) {
		writeStringUnterminated(dos, str);
		dos.put((byte) 0);
	}

	public static void writeString(DataOutput dos, String str) throws IOException {
		writeStringUnterminated(dos, str);
		dos.write(0);
	}
	
	public static void writeByteLengthString(DataOutput dos, String str) throws IOException {
		if (str == null){
			dos.write(0);
			return;
		}
		dos.write(str.length());
		writeStringUnterminated(dos, str);
	}
	
	public static void writeStringUTF16(DataOutput dos, String str) throws IOException {
		writeStringUnterminatedUTF16(dos, str);
		dos.writeShort(0);
	}

	public static void writePaddedString(ByteBuffer dos, String str, int len) {
		writePaddedString(dos, str, StandardCharsets.US_ASCII, len);
	}

	public static void writePaddedString(DataOutput dos, String str, int len) throws IOException {
		writePaddedString(dos, str, StandardCharsets.US_ASCII, len);
	}
	
	public static void writePaddedStringSJIS(DataOutput dos, String str, int len) throws IOException {
		writePaddedString(dos, str, SJIS_CHARSET, len);
	}

	private static void writePaddedString(ByteBuffer dos, String str, Charset charset, int len) {
		str = str.substring(0, Math.min(len, str.length()));
		if (str != null) {
			dos.put(str.getBytes(charset));
		}
		for (int i = str == null ? 0 : str.length(); i < len; i++) {
			dos.put((byte) 0);
		}
	}

	private static void writePaddedString(DataOutput dos, String str, Charset charset, int len) throws IOException {
		if (str != null) {
			str = str.substring(0, Math.min(len, str.length()));
			dos.write(str.getBytes(charset));
		}
		for (int i = str == null ? 0 : str.length(); i < len; i++) {
			dos.write(0);
		}
	}
	
	public static boolean checkMagic(byte[] data, String magic) {
		return checkMagic(data, 0, magic);
	}
	
	public static String getMagic(byte[] data, int off, int len) {
		return new String(data, off, len, StandardCharsets.US_ASCII);
	}
	
	public static boolean checkMagic(byte[] data, int position, String magic) {
		String compare = getMagic(data, position, magic.length());
		IOCommon.debugPrint("Compare magic " + compare + " to " + magic);
		return compare.equals(magic);
	}

	public static boolean checkMagic(DataInput in, String magic) throws IOException {
		byte[] buf = new byte[magic.length()];
		in.readFully(buf);
		return checkMagic(buf, magic);
	}

	public static void writeStringUnterminated(ByteBuffer dos, String str) {
		if (str != null) {
			dos.put(str.getBytes(StandardCharsets.US_ASCII));
		}
	}

	public static void writeStringUnterminated(DataOutput dos, String str) throws IOException {
		if (str != null) {
			dos.write(str.getBytes(StandardCharsets.US_ASCII));
		}
	}
	
	public static void writeStringUnterminatedUTF16(DataOutput dos, String str) throws IOException {
		if (str != null) {
			dos.write(str.getBytes(StandardCharsets.UTF_16LE));
		}
	}
}
