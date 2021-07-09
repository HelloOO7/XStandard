
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
	
	public static void printLiteral(String str, PrintStream out){
		boolean printQuotes = str != null && (str.contains(":") || str.equals("null") || str.contains("|"));
		if (printQuotes){
			out.print("\"");
		}
		out.print(str);
		if (printQuotes){
			out.print("\"");
		}
	}
}
