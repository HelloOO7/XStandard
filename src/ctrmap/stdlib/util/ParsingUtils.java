package ctrmap.stdlib.util;

/**
 * Various methods for converting Strings to common types.
 */
public class ParsingUtils {
	
	/**
	 * Parses a String representation of an integer, respecting Java-style radix prefixes.
	 * Returns a user-specified value if the parsing fails.
	 * @param str An input string, as specified in parseBasedInt(String).
	 * @param defValue The value to return if the parsing is unsuccessful.
	 * @return The parse result, or defValue.
	 */
	public static int parseBasedIntOrDefault(String str, int defValue){
		try {
			return parseBasedInt(str);
		}
		catch (NumberFormatException ex){
			return defValue;
		}
	}
	
	/**
	 * Parses a String representation of an integer, respecting Java-style radix prefixes.
	 * @param str A string with an unprefixed decimal number, a hexadecimal number prefixed with '0x' or a binary number prefixed with '0b'.
	 * @return The integer value parsed from the string.
	 * @throws NumberFormatException See Integer.parseInt
	 */
	public static int parseBasedInt(String str){
		if (str.startsWith("0x")){
			return Integer.parseUnsignedInt(str.substring(2), 16);
		}
		if (str.startsWith("-0x")){
			return Integer.parseInt(str.substring(2), 16);
		}
		if (str.startsWith("0b")){
			return Integer.parseInt(str.substring(2), 2);
		}
		return Integer.parseInt(str);
	}
	
	public static long parseBasedLong(String str){
		if (str.startsWith("0x")){
			return Long.parseUnsignedLong(str.substring(2), 16);
		}
		if (str.startsWith("-0x")){
			return Long.parseLong(str.substring(2), 16);
		}
		if (str.startsWith("0b")){
			return Long.parseLong(str.substring(2), 2);
		}
		return Long.parseLong(str);
	}
}
