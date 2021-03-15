package ctrmap.stdlib.yaml;

public class Key extends YamlContent {

	public String key;

	public static Key trySet(String line) {
		if (line.contains(":")) {
			String[] values = line.trim().split(":");
			if (values.length == 1) {
				String key = values[0].trim();
				if (!key.isEmpty()) {
					Key k = new Key();
					k.key = key;
					return k;
				}
			}
		}
		return null;
	}
	
	@Override
	public String toString(){
		return key;
	}
}
