
package ctrmap.stdlib.formats.yaml;

import java.io.PrintStream;

/**
 *
 */
public abstract class YamlContent {
	public abstract String getKey();
	public abstract String getValue();
	public abstract void setKey(String key);
	public abstract void setValue(String value);
	
	public abstract void print(PrintStream out);
}
