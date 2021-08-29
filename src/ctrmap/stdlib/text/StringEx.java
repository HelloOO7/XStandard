package ctrmap.stdlib.text;

import java.util.Arrays;

/**
 * Methods for String manipulation, because the String class is final.
 */
public class StringEx {

	public static String deleteAllString(String str, String delete) {
		StringBuilder sb = new StringBuilder(str);
		int index;
		int dellen = delete.length();
		while ((index = sb.indexOf(delete)) != -1) {
			sb.delete(index, index + dellen);
		}
		return sb.toString();
	}

	public static String deleteAllChars(String str, char c) {
		StringBuilder sb = new StringBuilder(str.length());
		char x;
		int len = str.length();
		for (int i = 0; i < len; i++) {
			x = str.charAt(i);
			if (x != c) {
				sb.append(x);
			}
		}
		return sb.toString();
	}

	public static String deleteAllChars(String str, char... c) {
		StringBuilder sb = new StringBuilder(str.length());
		char x;
		int len = str.length();
		Outer:
		for (int i = 0; i < len; i++) {
			x = str.charAt(i);
			for (int j = 0; j < c.length; j++) {
				if (x == c[j]) {
					continue Outer;
				}
			}
			sb.append(x);
		}
		return sb.toString();
	}

	public static String replaceFast(String str, String toReplace, String replaceWith) {
		StringBuilder sb = new StringBuilder(str);

		int index;
		int replen = toReplace.length();
		while ((index = sb.indexOf(toReplace)) != -1) {
			sb.replace(index, index + replen, replaceWith);
		}
		return sb.toString();
	}

	public static int numberOfChar(String str, char c) {
		int num = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == c) {
				num++;
			}
		}
		return num;
	}

	public static int[] indicesOfChar(String str, char c) {
		int num = numberOfChar(str, c);

		int[] result = new int[num];

		return indicesOfChar(str, c, result, 0);
	}

	public static int[] indicesOfChar(String str, char c, int[] dest, int destOffset) {

		for (int i = 0, j = destOffset; i < str.length(); i++) {
			if (str.charAt(i) == c) {
				dest[j++] = i;
			}
		}

		return dest;
	}

	public static String[] splitOnecharFast(String str, char character) {
		int[] indices = new int[numberOfChar(str, character) + 2];
		indicesOfChar(str, character, indices, 1);
		indices[0] = -1;
		indices[indices.length - 1] = str.length();
		int sizeAdjust = 1;
		if (indices[indices.length - 2] == indices[indices.length - 1]) {
			sizeAdjust++;
		}
		String[] result = new String[indices.length - sizeAdjust];
		for (int i = 0; i < indices.length - sizeAdjust; i++) {
			result[i] = str.substring(indices[i] + 1, indices[i + 1]);
		}
		return result;
	}
	
	public static String[] splitOnecharFastNoBlank(String str, char character) {
		String[] split = splitOnecharFast(str, character);
		
		int emptyCount = 0;
		
		for (int i = 0; i < split.length; i++) {
			String trim = split[i].trim();
			split[i] = trim;
			if (trim.isEmpty()) {
				emptyCount++;
			}
		}
		
		String[] result = new String[split.length - emptyCount];
		
		for (int i = 0, j = 0; i < split.length; i++) {
			if (!split[i].isEmpty()) {
				result[j++] = split[i];
			}
		}
		
		return result;
	}

	/**
	 * Returns the index of the first non-whitespace character after the first whitespace character since an index.
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
	 * Returns the index of the first non-whitespace character of a String, starting at 'idx'.
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
	 * Returns the index of the first whitespace character of a String, starting at 'idx'.
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
	 * Returns the index of the first character of 'str' that is neither a letter nor a digit.
	 *
	 * @param str A String.
	 * @param allowedChars An optional array of characters that should be ignored even if they were to return the search.
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
