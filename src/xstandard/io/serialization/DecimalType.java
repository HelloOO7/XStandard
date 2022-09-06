package xstandard.io.serialization;

/**
 * Method of interpreting decimals during de/serialization.
 */
public enum DecimalType {
	/**
	 * IEEE-754 floating point.
	 */
    FLOATING_POINT,
	/**
	 * Nintendo DS 9-bit fractional fixed point decimal notation.
	 */
	FIXED_POINT_NNFX
}
