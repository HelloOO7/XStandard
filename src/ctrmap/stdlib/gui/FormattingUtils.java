
package ctrmap.stdlib.gui;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FormattingUtils {
	
	public static String getFormattedHexString32(int instructionRaw) {
		String hexstring = Integer.toHexString(Integer.reverseBytes(instructionRaw));
		return ("00000000" + hexstring).substring(hexstring.length()).toUpperCase().replaceAll("..", "$0 ");
	}

	public static String getFormattedHexString32LE(int instructionRaw) {
		String hexstring = Integer.toHexString(instructionRaw);
		return ("00000000" + hexstring).substring(hexstring.length()).toUpperCase().replaceAll("..", "$0 ");
	}

	public static String getFormattedHexStringShort(int instructionRaw) {
		String hexstring = Integer.toHexString(instructionRaw);
		return ("0000" + hexstring).substring(hexstring.length()).toUpperCase().replaceAll("..", "$0 ");
	}

	public static String getCommonFormattedDate(){
		return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
	}
	
	public static String makeStringFromLiteral(String str){
		if (str.equals("null")){
			return null;
		}
		if (str.startsWith("\"") && str.endsWith("\"")){
			str = str.substring(1, str.length() - 1);
		}
		return str;
	}
	
	public static String getIntWithLeadingZeros(int zeroCount, int value) {
		return getStrWithLeadingZeros(zeroCount, String.valueOf(value));
	}
	
	public static String getStrWithLeadingZeros(int zeroCount, String value) {
		StringBuilder zeroSB = new StringBuilder();
		for (int i = 0; i < zeroCount; i++) {
			zeroSB.append("0");
		}
		String v = value.substring(0, Math.min(value.length(), zeroCount));
		return zeroSB.substring(v.length()) + v;
	}

	public static String getFriendlySize(int size) {
		if (size < 1000) {
			return size + " bytes";
		}
		DecimalFormat f = new DecimalFormat();
		f.setMaximumFractionDigits(2);
		if (size < 1000000) {
			return f.format(size / (double) 1000) + "kB";
		}
		return f.format(size / (double) 1000000) + "MB";
	}
	
	public static String getStrWithoutSpaces(String str){
		StringBuilder sb = new StringBuilder();
		boolean appendNextUnderscore = false;
		for (int i = 0; i < str.length(); i++){
			char c = str.charAt(i);
			if (Character.isLetterOrDigit(c)){
				if (appendNextUnderscore){
					sb.append('_');
					appendNextUnderscore = false;
				}
				sb.append(c);
			}
			else {
				appendNextUnderscore = true;
			}
		}
		return sb.toString();
	}
	
	public static String getEnumlyString(String str){
		return getStrWithoutSpaces(str.trim()).toUpperCase();
	}

	public static String getFriendlyEnum(Enum e) {
		String lc = e.toString().toLowerCase();
		StringBuilder sb = new StringBuilder();
		boolean isNextUpperCase = true;
		for (int i = 0; i < lc.length(); i++) {
			char c = lc.charAt(i);
			if (c == '_') {
				isNextUpperCase = true;
			} else {
				sb.append(isNextUpperCase ? Character.toUpperCase(c) : c);
				isNextUpperCase = false;
			}
		}
		return sb.toString();
	}

}
