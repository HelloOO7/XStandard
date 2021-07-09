package ctrmap.stdlib.io.serialization;

/**
 * Method of writing references to objects during serialization.
 */
public enum ReferenceType {
	/**
	 * All objects are written in-line.
	 */
	NONE,
	/**
	 * Objects are referenced with absolute addresses.
	 */
	ABSOLUTE_POINTER,
	/**
	 * Objects are referenced with addresses relative to the stream position.
	 */
	SELF_RELATIVE_POINTER
}
