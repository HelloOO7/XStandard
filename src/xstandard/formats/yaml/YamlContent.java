
package xstandard.formats.yaml;

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
	
	public static void printLiteral(String str, PrintStream out){
		if (str == null) {
			out.print("null");
		}
		else {
			boolean hasNewline = str.contains("\n");
			boolean printQuotes = (str.contains(":") || str.equals("null") || str.contains("|")) || hasNewline;
			if (printQuotes){
				out.print("\"");
			}
			if (hasNewline) {
				str = str.replace("\n", "\\n");
			}
			out.print(str);
			if (printQuotes){
				out.print("\"");
			}
		}
	}
}
