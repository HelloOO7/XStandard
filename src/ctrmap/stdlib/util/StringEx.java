package ctrmap.stdlib.util;

public class StringEx {
	
	public static int indexOfFirstNonWhitespace(String str){
		int len = str.length();
		for (int i = 0; i < len; i++){
			if (!Character.isWhitespace(str.charAt(i))){
				return i;
			}
		}
		return -1;
	}
	
	public static boolean containsUppercase(String str){
		int len = str.length();
		for (int i = 0; i < len; i++){
			if (Character.isUpperCase(str.charAt(i))){
				return true;
			}
		}
		return false;
	}
	
	public static char findFirstNonLetterOrDigit(String str, char... allowedChars){
		int len = str.length();
		for (int i = 0; i < len; i++){
			char c = str.charAt(i);
			if (!Character.isLetterOrDigit(c) && !charArrayContains(allowedChars, c)){
				return c;
			}
		}
		return 0;
	}
	
	private static boolean charArrayContains(char[] arr, char c){
		for (int i = 0; i < arr.length; i++){
			if (arr[i] == c){
				return true;
			}
		}
		return false;
	}
}
