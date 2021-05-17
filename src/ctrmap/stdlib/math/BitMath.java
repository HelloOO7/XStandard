package ctrmap.stdlib.math;

public class BitMath {
	public static int setIntegerBit(int value, int bitNo, int state){
		return setIntegerBit(value, bitNo, state != 0);
	}
	
	public static int setIntegerBit(int value, int bitNo, boolean state){
		int pow = (1 << bitNo);
		value &= ~pow;
		if (state){
			value |= pow;
		}
		return value;
	}
	
	public static int setIntegerBits(int value, int startIdx, int count, int bits){
		int mask = ((1 << (count + 1)) - 1) << startIdx;
		bits <<= startIdx;
		bits &= mask;
		value &= ~mask;
		value |= bits;
		return value;
	}
}
