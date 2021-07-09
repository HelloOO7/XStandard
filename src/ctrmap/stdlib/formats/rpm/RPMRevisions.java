package ctrmap.stdlib.formats.rpm;

public class RPMRevisions {

	/**
	 * Current revision of the RPM writer.
	 */
	public static final int REV_CURRENT = 3;

	/**
	 * Initial version of the format.
	 */
	public static final int REV_FOUNDATION = 0;

	/**
	 * Added symbol length field.
	 */
	public static final int REV_SYMBOL_LENGTH = 1;

	/**
	 * Added product name and version metadata fields. THIS VERSION HAS BEEN
	 * TERMINATED!
	 */
	public static final int REV_PRODUCT_INFO = 2;

	/**
	 * Use the string table to write symbol names. Makes structs more easily
	 * deserializable and opens the door for possible string compression.
	 */
	public static final int REV_SYMBSTR_TABLE = 3;

	/**
	 * Reduce the size of the symbol table by writing string pointers and symbol
	 * sizes as shorts.
	 */
	public static final int REV_SMALL_SYMBOLS = 3;

	/**
	 * Write essential header fields into a separately referenced INFO section.
	 * This eliminates the need for reserved values in the RPM footer.
	 */
	public static final int REV_INFO_SECTION = 3;

	/**
	 * Implements the capability to add arbitrary user data to an RPM.
	 */
	public static final int REV_METADATA = 3;
}
