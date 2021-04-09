
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
		if (line.contains(":")){
			String[] values = line.trim().split(":");
			
			if (values.length == 2){
				String key = values[0].trim();
				String value = FormattingUtils.stripStringOfQuotations(values[1].trim());
				if (!key.isEmpty() && !value.isEmpty()){
					return new KeyValuePair(key, value);
				}
			}
		}
		return null;
	}
	
	@Override
	public String toString(){
		return key + ": " + value;
	}

	@Override
	public void print(PrintStream out) {
		out.print(key);
		out.print(": ");
		out.print(value);
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
