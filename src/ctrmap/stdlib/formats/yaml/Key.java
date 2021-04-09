package ctrmap.stdlib.formats.yaml;

import java.io.PrintStream;

public class Key extends YamlContent {

	public String key;
	
	public Key(String keyName){
		this.key = keyName;
	}

	public static Key trySet(String line) {
		if (line.contains(":")) {
			String[] values = line.trim().split(":");
			if (values.length == 1) {
				String key = values[0].trim();
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
		out.print(key);
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
