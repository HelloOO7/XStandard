package ctrmap.stdlib.io.util;

import java.io.DataOutput;
import java.io.IOException;

public class BitWriter {
	private byte bufIdx = 0;
	private byte buf;
	
	private DataOutput out;
	
	public BitWriter(DataOutput out){
		this.out = out;
	}
	
	public void writeBit(boolean value) throws IOException {
		if (value){
			buf |= (1 << bufIdx);
		}
		bufIdx++;
		tryFlushBuffer();
	}
	
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
				buf |= (value & makeMask(bufRemain)) << bufIdx;
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
						buf = (byte)(value & makeMask(bufIdx));
					}
				}
				break;
		}
	}
	
	private int makeMask(int bitCount){
		return (1 << (bitCount + 1)) - 1;
	}
	
	private void tryFlushBuffer() throws IOException {
		if (bufIdx == 8){
			out.write(buf);
			buf = 0;
			bufIdx = 0;
		}
	}
	
	public void flush() throws IOException {
		tryFlushBuffer();
	}
}
