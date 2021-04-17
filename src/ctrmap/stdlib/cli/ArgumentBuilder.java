package ctrmap.stdlib.cli;

import ctrmap.stdlib.util.ArraysEx;
import java.util.ArrayList;
import java.util.List;

public class ArgumentBuilder {

	private List<ArgumentPattern> patterns = new ArrayList<>();

	public ArgumentBuilder(ArgumentPattern... patterns) {
		this.patterns = ArraysEx.asList(patterns);
	}

	public int parserIndex = 0;
	public ArgumentContent defaultContent;
	public List<ArgumentContent> cnt = new ArrayList<>();

	public void print() {
		for (ArgumentPattern ptn : patterns) {
			ptn.print();
		}
	}

	public ArgumentContent getContent(String name) {
		return getContent(name, false);
	}

	public ArgumentContent getContent(String name, boolean allowsNullValue) {
		for (ArgumentContent c : cnt) {
			if (c.key.equals(name)) {
				return c;
			}
		}
		for (ArgumentPattern ptn : patterns) {
			if (ptn.key.equals(name)) {
				if (ptn.defaultValue == null) {
					if (ptn.allowsMultiple) {
						ArgumentContent dummy = new ArgumentContent();
						dummy.key = name;
						return dummy;
					}
					if (allowsNullValue) {
						return null;
					}
					throw new UnsupportedOperationException("Required argument not supplied: " + name);
				}
				ArgumentContent dummy = new ArgumentContent();
				dummy.key = name;
				dummy.contents.add(ptn.defaultValue);
				return dummy;
			}
		}
		throw new IllegalArgumentException("Unbuilt argument requested.");
	}

	public void parse(String[] args) {
		defaultContent = new ArgumentContent();
		StringBuilder comb = new StringBuilder();
		for (String a : args) {
			comb.append(a == null ? "" : a);
		}
		String str = comb.toString().trim();
		cnt = new ArrayList<>();
		boolean argStart = false;
		for (; parserIndex < str.length(); parserIndex++) {
			if (str.charAt(parserIndex) == '-') {
				if (!argStart) {
					String[] defaultArgs = getSplitAtSpacesWithQuotationMarks(str.substring(0, parserIndex));
					for (String dflt : defaultArgs) {
						String t = dflt.trim();
						if (t.length() > 0) {
							defaultContent.contents.add(t);
						}
					}
				}
				argStart = true;
				boolean matched = false;
				for (ArgumentPattern p : patterns) {
					String matchStr = p.match(str, parserIndex);
					if (matchStr != null) {
						cnt.add(p.getContent(str, matchStr, this));
						matched = true;
						parserIndex--;
						break;
					}
				}
				if (!matched) {
					int lastIndex = str.indexOf(" +", parserIndex);
					if (lastIndex == -1) {
						lastIndex = str.length();
					}
					throw new IllegalArgumentException("Invalid argument: " + str.substring(parserIndex, lastIndex));
				}
			}
		}
		if (!argStart) {
			String[] defaultArgs = getSplitAtSpacesWithQuotationMarks(str);
			for (String dflt : defaultArgs) {
				String t = dflt.trim();
				if (t.length() > 0) {
					defaultContent.contents.add(t);
				}
			}
		}
	}

	public static String[] getSplitAtSpacesWithQuotationMarks(String str) {
		String[] cnt = str.split(" +(?=([^\"]*\"[^\"]*\")*[^\"]*$)"); //based on https://stackabuse.com/regex-splitting-by-character-unless-in-quotes/
		return cnt;
	}
}
