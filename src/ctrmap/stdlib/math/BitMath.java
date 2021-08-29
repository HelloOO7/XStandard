package ctrmap.stdlib.math;

/**
 * Methods for manipulating integers as bit fields.
 */
public class BitMath {
	
	public static boolean checkIntegerBit(int value, int bitNo){
		return ((value >> bitNo) & 1) == 1;
	}
	
	/**
	 * Sets a bit of an integer to an integer value.
	 * @param value Integer to set the value into.
	 * @param bitNo Index of the bit to be set.
	 * @param state Desired zero or non-zero state of the bit at bitNo.
	 * @return The input integer with the bit set accordingly.
	 */
	public static int setIntegerBit(int value, int bitNo, int state){
		return setIntegerBit(value, bitNo, state != 0);
	}
	
	/**
	 * Sets a bit of an integer to boolean value.
	 * @param value Integer to set the value into.
	 * @param bitNo Index of the bit to be set.
	 * @param state Desired binary state of the bit at bitNo.
	 * @return The input integer with the bit set accordingly.
	 */
	public static int setIntegerBit(int value, int bitNo, boolean state){
		int pow = (1 << bitNo);
		value &= ~pow;
		if (state){
			value |= pow;
		}
		return value;
	}
	
	/**
	 * Sets a number of bits of an integers to a given value.
	 * @param value Integer to set the value to.
	 * @param startIdx First bit to copy the value to.
	 * @param count Number of bits to copy from the value.
	 * @param bits The value to set.
	 * @return The input integer with the bits set accordingly.
	 */
	public static int setIntegerBits(int value, int startIdx, int count, int bits){
		int mask = ((1 << (count + 1)) - 1) << startIdx;
		bits <<= startIdx;
		bits &= mask;
		value &= ~mask;
		value |= bits;
		return value;
	}

	public static int signExtend(int value, int bits) {
		int output = (int) value;
		boolean sign = (value & (1 << (bits - 1))) > 0;
		if (sign) {
			output -= (1 << bits);
		}
		return output;
	}

	/**
	 * Creates a bit mask.
	 * @param bitCount Number of bits to mask.
	 * @return An integer with bitCount bits set to 1.
	 */
	public static int makeMask(int bitCount) {
		return (1 << (bitCount)) - 1;
	}
}
