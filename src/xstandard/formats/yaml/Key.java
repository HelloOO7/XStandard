package xstandard.formats.yaml;

import xstandard.text.FormattingUtils;
import java.io.PrintStream;

public class Key extends YamlContent {

	public String key;
	
	public Key(String keyName){
		this.key = keyName;
	}

	public static Key trySet(String line) {
		int ddotIdx = KeyValuePair.getDDotIdx(line);
		if (ddotIdx != -1) {
			String[] values = KeyValuePair.splitToKeyAndValue(ddotIdx, line);
			if (values[1].trim().isEmpty()) {
				String key = FormattingUtils.makeStringFromLiteral(values[0].trim());
				if (!key.isEmpty()) {
					return new Key(key);
				}
			}
		}
		return null;
	}
	
	@Override
	public String toString(){
		return key;
	}

	@Override
	public void print(PrintStream out) {
		printLiteral(key, out);
		out.print(":");
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getValue() {
		return null;
	}

	@Override
	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public void setValue(String value) {
		
	}
}
