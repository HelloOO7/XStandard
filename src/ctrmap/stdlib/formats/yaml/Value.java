
package ctrmap.stdlib.formats.yaml;

import ctrmap.stdlib.gui.FormattingUtils;
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
