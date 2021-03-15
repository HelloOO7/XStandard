
package ctrmap.stdlib.yaml;

/**
 *
 */
public class SimpleValue extends YamlContent {
	public String value;
	
	public static SimpleValue trySet(String line) {
		if (!line.contains(":")) {
			SimpleValue value = new SimpleValue();
			value.value = line.trim();
			return value;
		}
		return null;
	}
	
	@Override
	public String toString(){
		return value;
	}
}
