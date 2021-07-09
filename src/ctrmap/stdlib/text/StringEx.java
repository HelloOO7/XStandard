package ctrmap.stdlib.text;

/**
 * Methods for String manipulation, because the String class is final.
 */
public class StringEx {

	/**
	 * Returns the index of the first non-whitespace character after the first
	 * whitespace character since an index.
	 *
	 * @param str A String.
	 * @param idx The starting index.
	 * @return The index, or -1 if not found.
	 */
	public static int indexOfFirstNonWhitespaceAfterWhitespace(String str, int idx) {
		return indexOfFirstNonWhitespace(str, indexOfFirstWhitespace(str, idx));
	}

	public static int indexOfFirstNonWhitespace(String str) {
		return indexOfFirstNonWhitespace(str, 0);
	}

	/**
	 * Returns the index of the first non-whitespace character of a String,
	 * starting at 'idx'.
	 *
	 * @param str A String.
	 * @param idx The starting index.
	 * @return The index, or -1 if not found.
	 */
	public static int indexOfFirstNonWhitespace(String str, int idx) {
		int len = str.length();
		for (int i = idx; i < len; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns the index of the first whitespace character of a String, starting
	 * at 'idx'.
	 *
	 * @param str A String.
	 * @param startIndex The starting index.
	 * @return The index, or -1 if not found.
	 */
	public static int indexOfFirstWhitespace(String str, int startIndex) {
		int len = str.length();
		for (int i = startIndex; i < len; i++) {
			if (Character.isWhitespace(str.charAt(i))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Checks if a String contains an uppercase character.
	 *
	 * @param str A String.
	 * @return True if there is at least one uppercase character in the String.
	 */
	public static boolean containsUppercase(String str) {
		int len = str.length();
		for (int i = 0; i < len; i++) {
			if (Character.isUpperCase(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the index of the first character of 'str' that is neither a
	 * letter nor a digit.
	 *
	 * @param str A String.
	 * @param allowedChars An optional array of characters that should be
	 * ignored even if they were to return the search.
	 * @return The index, or -1 if not found.
	 */
	public static char findFirstNonLetterOrDigit(String str, char... allowedChars) {
		int len = str.length();
		for (int i = 0; i < len; i++) {
			char c = str.charAt(i);
			if (!Character.isLetterOrDigit(c) && !charArrayContains(allowedChars, c)) {
				return c;
			}
		}
		return 0;
	}

	/**
	 * Checks if a character array contains a character.
	 *
	 * @param arr An array of characters
	 * @param c The character to search for.
	 * @return True if 'arr' contains 'c'.
	 */
	private static boolean charArrayContains(char[] arr, char c) {
		if (arr != null) {
			for (int i = 0; i < arr.length; i++) {
				if (arr[i] == c) {
					return true;
				}
			}
		}
		return false;
	}
}
