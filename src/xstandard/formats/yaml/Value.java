
package xstandard.formats.yaml;

import xstandard.text.FormattingUtils;
import java.io.PrintStream;

/**
 *
 */
public class Value extends YamlContent {
	public String value;
	
	public Value(String val){
		value = val;
	}
	
	public static Value trySet(String line) {
		if (KeyValuePair.getDDotIdx(line) == -1) {
			return new Value(FormattingUtils.makeStringFromLiteral(line.trim()));
		}
		return null;
	}
	
	@Override
	public String toString(){
		return value;
	}

	@Override
	public void print(PrintStream out) {
		printLiteral(value, out);
	}

	@Override
	public String getKey() {
		return null;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setKey(String key) {
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}
}
