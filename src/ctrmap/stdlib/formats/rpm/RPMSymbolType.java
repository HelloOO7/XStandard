package ctrmap.stdlib.formats.rpm;

/**
 * Type of an RPM symbol.
 */
public enum RPMSymbolType {
	/**
	 * The symbol does not have a known type.
	 */
	NULL,
	/**
	 * The symbol is a value/field.
	 */
	VALUE,
	/**
	 * The symbol is a function compiled for the ARM instruction set.
	 */
	FUNCTION_ARM,
	/**
	 * The symbol is a function compiled for the Thumb instruction set.
	 */
	FUNCTION_THM,
	/**
	 * The symbol marks the start of a program segment.
	 */
	SECTION;

	public boolean isFunction() {
		return this == FUNCTION_ARM || this == FUNCTION_THM;
	}
}
