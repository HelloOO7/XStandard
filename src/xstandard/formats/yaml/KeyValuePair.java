package xstandard.formats.yaml;

import xstandard.text.FormattingUtils;
import java.io.PrintStream;

/**
 *
 */
public class KeyValuePair extends YamlContent {

	public String key;
	public String value;

	public KeyValuePair(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public static KeyValuePair trySet(String line) {
		int ddotIdx = getDDotIdx(line);
		if (ddotIdx != -1) {
			String[] values = splitToKeyAndValue(ddotIdx, line);

			if (values.length == 2) {
				String key = FormattingUtils.makeStringFromLiteral(values[0].trim());
				String value = FormattingUtils.makeStringFromLiteral(values[1].trim());
				if (!key.isEmpty() && (value == null || !value.isEmpty())) {
					return new KeyValuePair(key, value);
				}
			}
		}
		return null;
	}

	public static String[] splitToKeyAndValue(int idx, String str) {
		return new String[]{str.substring(0, idx), str.substring(idx + 1, str.length())};
	}

	public static int getDDotIdx(String line) {
		boolean discardNext = false;
		boolean applies = true;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '\\') {
				discardNext = true;
			} else {
				discardNext = false;
			}
			if (c == '"') {
				if (!discardNext) {
					applies = !applies;
				}
			}
			else if (c == ':' && applies){
				return i;
			}
		}
		return -1;
	}

	@Override
	public String toString() {
		return key + ": " + value;
	}

	@Override
	public void print(PrintStream out) {
		printLiteral(key, out);
		out.print(": ");
		printLiteral(value, out);
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}
};
