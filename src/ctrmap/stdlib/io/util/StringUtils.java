package ctrmap.stdlib.io.util;

import ctrmap.stdlib.io.LittleEndianDataInputStream;
import ctrmap.stdlib.io.iface.SeekableDataInput;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StringUtils {

	public static String readString(DataInput in) throws IOException {
		StringBuilder sb = new StringBuilder();
		int read;
		while ((read = in.readUnsignedByte()) != 0) {
			sb.append((char) read);
		}
		return sb.toString();
	}
	
	public static String readStringUTF16(DataInput in) throws IOException {
		StringBuilder sb = new StringBuilder();
		char read;
		while ((read = in.readChar()) != 0) {
			sb.append(read);
		}
		return sb.toString();
	}

	public static String readString(int address, byte[] b) throws IOException {
		LittleEndianDataInputStream in = new LittleEndianDataInputStream(new ByteArrayInputStream(b));
		in.skip(address);
		String str = readString(in);
		in.close();
		return str;
	}

	public static String readGFString(DataInput in) throws IOException {
		return readStringWithSize(in, 0x20);
	}

	public static String readStringWithSize(DataInput in, int size) throws IOException {
		byte[] buf = new byte[size];
		in.readFully(buf);
		int term = 0;
		for (; term < size; term++) {
			if (buf[term] == 0x00) {
				break;
			}
		}
		return new String(Arrays.copyOfRange(buf, 0, term), "ASCII");
	}

	public static String readStringWithAddress(SeekableDataInput in) throws IOException {
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

	public static boolean checkMagic(DataInput in, String magic) throws IOException {
		byte[] buf = new byte[magic.length()];
		in.readFully(buf);
		try {
			return new String(buf, "ASCII").equals(magic);
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(StringUtils.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}
	}

	public static void writeString(DataOutput dos, String str) throws IOException {
		writeStringUnterminated(dos, str);
		dos.write(0);
	}
	
	public static void writeStringUTF16(DataOutput dos, String str) throws IOException {
		writeStringUnterminatedUTF16(dos, str);
		dos.writeShort(0);
	}

	public static void writeStringUnterminated(DataOutput dos, String str) throws IOException {
		dos.write(str.getBytes("ASCII"));
	}
	
	public static void writeStringUnterminatedUTF16(DataOutput dos, String str) throws IOException {
		dos.write(str.getBytes("UTF-16LE"));
	}

	public static boolean isUTF8Capital(byte check) {
		return (check & 255) >= 65 && (check & 255) <= 90;
	}

	public static boolean checkMagic(byte[] data, String magic) {
		if (data == null || magic.length() > data.length) {
			return false;
		}
		byte[] test = Arrays.copyOfRange(data, 0, magic.length());
		return new String(test).equals(magic);
	}
}
