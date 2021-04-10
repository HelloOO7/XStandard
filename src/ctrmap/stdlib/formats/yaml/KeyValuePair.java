
package ctrmap.stdlib.formats.yaml;

import ctrmap.stdlib.gui.FormattingUtils;
import java.io.PrintStream;

/**
 *
 */
public class KeyValuePair extends YamlContent {
	public String key;
	public String value;
	
	public KeyValuePair(String key, String value){
		this.key = key;
		this.value = value;
	}
	
	public static KeyValuePair trySet(String line){
		int ddotIdx = getDDotIdx(line);
		if (ddotIdx != -1){
			String[] values = splitToKeyAndValue(ddotIdx, line);
			
			if (values.length == 2){
				String key = values[0].trim();
				String value = FormattingUtils.makeStringFromLiteral(values[1].trim());
				if (!key.isEmpty() && (value == null || !value.isEmpty())){
					return new KeyValuePair(key, value);
				}
			}
		}
		return null;
	}
	
	public static String[] splitToKeyAndValue(int idx, String str){
		return new String[]{str.substring(0, idx), str.substring(idx + 1, str.length())};
	}
	
	public static int getDDotIdx(String line){
		int quoteIdx = line.indexOf('"');
		int ddotIdx = line.indexOf(':');
		return (quoteIdx > ddotIdx || quoteIdx == -1) ? ddotIdx : -1;
	}
	
	@Override
	public String toString(){
		return key + ": " + value;
	}

	@Override
	public void print(PrintStream out) {
		out.print(key);
		out.print(": ");
		boolean printQuotes = value != null && (value.contains(" ") || value.contains(":"));
		if (printQuotes){
			out.print("\"");
		}
		out.print(value);
		if (printQuotes){
			out.print("\"");
		}
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}
};
