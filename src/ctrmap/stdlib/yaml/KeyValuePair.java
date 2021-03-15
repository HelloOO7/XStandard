
package ctrmap.stdlib.yaml;

/**
 *
 */
public class KeyValuePair extends YamlContent {
	public String key;
	public String value;
	
	public static KeyValuePair trySet(String line){
		if (line.contains(":")){
			String[] values = line.trim().split(":");
			
			if (values.length == 2){
				String key = values[0].trim();
				String value = values[1].trim();
				if (!key.isEmpty() && !value.isEmpty()){
					KeyValuePair pair = new KeyValuePair();
					pair.key = key;
					pair.value = value;
					return pair;
				}
			}
		}
		return null;
	}
	
	@Override
	public String toString(){
		return key + ": " + value;
	}
}
