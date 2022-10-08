package xstandard.util;

import xstandard.math.BitMath;

public class EnumBitflagsInt<E extends Enum> {

	private int bits;
	private final E[] constants;

	public EnumBitflagsInt(Class<E> cls) {
		this(cls, 0);
	}

	public EnumBitflagsInt(Class<E> cls, int bits) {
		constants = cls.getEnumConstants();
		this.bits = bits;
	}

	private int indexOf(E flag) {
		for (int i = 0; i < constants.length; i++) {
			if (constants[i] == flag) {
				return i;
			}
		}
		return -1;
	}

	public boolean isSet(E flag) {
		return BitMath.checkIntegerBit(bits, indexOf(flag));
	}
	
	public boolean isSetAll(E... flags) {
		for (E flag : flags) {
			if (!isSet(flag))  {
				return false;
			}
		}
		return true;
	}

	public boolean isSetAny(E... flags) {
		for (E flag : flags) {
			if (isSet(flag))  {
				return true;
			}
		}
		return false;
	}

	public void set(E flag) {
		bits = BitMath.setIntegerBit(bits, indexOf(flag), true);
	}

	public void clear(E flag) {
		bits = BitMath.setIntegerBit(bits, indexOf(flag), false);
	}
}
