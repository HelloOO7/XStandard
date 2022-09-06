package xstandard.io.util;

import xstandard.math.BitMath;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Output stream wrapper for writing data bit-by-bit.
 */
public class BitWriter {
	private byte bufIdx = 0;
	private byte buf;
	
	private DataOutput out;
	
	/**
	 * Creates a BitWriter with a DataOutput as target.
	 * @param out DataOutput to wrap.
	 */
	public BitWriter(DataOutput out){
		this.out = out;
	}
	
	/**
	 * Write a single boolean bit.
	 * @param value Binary value.
	 * @throws IOException 
	 */
	public void writeBit(boolean value) throws IOException {
		if (value){
			buf |= (1 << bufIdx);
		}
		bufIdx++;
		tryFlushBuffer();
	}
	
	/**
	 * Write several bits of an integer value.
	 * @param value Value to append.
	 * @param bitCount Number of bits from value to write.
	 * @throws IOException 
	 */
	public void writeBits(int value, int bitCount) throws IOException {
		switch (bitCount){
			case 0:
				return;
			case 1:
				writeBit(value == 1);
				return;
			default:
				int bitIndex = 0;
				int bufRemain = 8 - bufIdx;
				buf |= (value & BitMath.makeMask(bufRemain + 1)) << bufIdx;
				bitIndex = bufRemain;
				value >>= bufRemain;
				bufIdx += Math.min(bitCount, bufRemain);
				tryFlushBuffer();
				if (bitIndex < bitCount){
					for (int i = 0; i < bitCount / 8; i++){
						out.write(value & 0xFF);
						bitIndex += 8;
						value >>= 8;
					}
					if (bitIndex < bitCount){
						bufIdx = (byte)(bitCount - bitIndex);
						buf = (byte)(value & BitMath.makeMask(bufIdx + 1));
					}
				}
				break;
		}
	}
		
	private void tryFlushBuffer() throws IOException {
		if (bufIdx == 8){
			out.write(buf);
			buf = 0;
			bufIdx = 0;
		}
	}
	
	/**
	 * Flushes any unwritten bits into the output stream and byte-aligns it.
	 * @throws IOException 
	 */
	public void flush() throws IOException {
		if (bufIdx != 0){
			out.write(buf);
			buf = 0;
			bufIdx = 0;
		}
	}
}
