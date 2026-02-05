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

	public static void printLiteral(String str, PrintStream out) {
		if (str == null) {
			out.print("null");
		} else {
			boolean hasNewline = str.contains("\n");
			boolean printQuotes
					= (str.contains(":") || str.contains("|") || str.contains("\""))
					|| str.equals("null")
					|| hasNewline
					|| (str.length() > 0 && (Character.isWhitespace(str.charAt(0)) || Character.isWhitespace(str.charAt(str.length() - 1)))); //prevent trimming leading/trailing spaces
			if (printQuotes) {
				str = str.replace("\"", "\\\"");
				out.print("\"");
			}
			if (hasNewline) {
				str = str.replace("\n", "\\n");
			}
			out.print(str);
			if (printQuotes) {
				out.print("\"");
			}
		}
	}
}
