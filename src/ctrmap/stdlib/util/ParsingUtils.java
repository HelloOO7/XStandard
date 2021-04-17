package ctrmap.stdlib.util;

public class ParsingUtils {
	public static int parseBasedIntOrDefault(String str, int defValue){
		try {
			return parseBasedInt(str);
		}
		catch (NumberFormatException ex){
			return defValue;
		}
	}
	
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
}
