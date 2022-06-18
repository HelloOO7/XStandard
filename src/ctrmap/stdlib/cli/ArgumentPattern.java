package ctrmap.stdlib.cli;

import ctrmap.stdlib.util.ParsingUtils;
import java.util.ArrayList;
import java.util.List;

public class ArgumentPattern {

	public String key;
	public String brief;
	public final Object defaultValue;
	public final boolean allowsMultiple;
	private String[] tags;
	private ArgumentType type;

	public ArgumentPattern(String key, String brief, ArgumentType type, Object defaultValue, String... tags) {
		this(key, brief, type, defaultValue, false, tags);
	}

	public ArgumentPattern(String key, String brief, ArgumentType type, Object defaultValue, boolean allowsMultiple, String... tags) {
		this.key = key;
		this.brief = brief;
		this.defaultValue = defaultValue;
		this.tags = tags;
		this.type = type;
		this.allowsMultiple = allowsMultiple;
	}
	
	public void print(){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tags.length; i++){
			if (i != 0){
				sb.append(", ");
			}
			sb.append(tags[i]);
		}
		while (sb.length() < 30){
			sb.append(" ");
		}
		sb.append("-    ");
		sb.append(brief);
		if (defaultValue != null){
			sb.append(" (Default: ");
			sb.append(defaultValue);
			sb.append(")");
		}
		System.out.println(sb.toString());
	}
	
	public int getDefaultAsInt(){
		return (Integer)defaultValue;
	}
	
	public float getDefaultAsFloat(){
		return (Float)defaultValue;
	}

	public String getDefaultAsString(){
		return String.valueOf(defaultValue);
	}
	
	public boolean getDefaultAsBoolean(){
		return (Boolean)defaultValue;
	}
	
	public String match(String str, int index) {
		String src = str.substring(index);
		for (String tag : tags) {
			if (src.startsWith(tag)) {
				return tag;
			}
		}
		return null;
	}

	public ArgumentContent getContent(String str, String matchedTag, ArgumentBuilder bld) {
		String substr = str.substring(bld.parserIndex + matchedTag.length());
		int end = 0;
		boolean b = false;
		Outer:
		for (; end < substr.length(); end++) {
			char c = substr.charAt(end);
			switch (c) {
				case '"':
					b = !b;
					break;
				case '-':
					if (!b) {
						break Outer;
					}
					break;
			}
		}
		substr = substr.substring(0, end);
		bld.parserIndex += end + matchedTag.length();
		String[] cntArr = ArgumentBuilder.getSplitAtSpacesWithQuotationMarks(substr);
		List<String> cnt = new ArrayList<>();
		for (String c : cntArr){
			String t = ArgumentBuilder.trim(c);
			if (c.length() > 0){
				cnt.add(t);
			}
		}

		if (cnt.size() > 1 && !allowsMultiple) {
			bld.defaultContent.contents.addAll(cnt.subList(1, cnt.size()));
			cnt = cnt.subList(0, 1);
		}
		if (cnt.isEmpty() && type != ArgumentType.BOOLEAN) {
			throw new UnsupportedOperationException("Argument " + key + " requires parameters.");
		}
		ArgumentContent c = new ArgumentContent();
		if (type == ArgumentType.BOOLEAN) {
			if (cnt.isEmpty()) {
				c.contents.add(true);
			}
		}
		c.key = key;

		for (String p : cnt) {
			if (p.startsWith("\"") && p.endsWith("\"") && p.length() > 1) {
				p = p.substring(1, p.length() - 1);
			}
			switch (type) {
				case FLOAT:
					float flt;
					try {
						flt = Float.parseFloat(p);
					} catch (NumberFormatException ex) {
						throw new UnsupportedOperationException(p + "is not a valid floating point parameter for argument " + key + ".");
					}
					c.contents.add((Float) flt);
					break;
				case INT:
					int intv;
					try {
						intv = ParsingUtils.parseBasedInt(p);
					} catch (NumberFormatException ex) {
						throw new UnsupportedOperationException(p + " is not a valid integer parameter for argument " + key + ".");
					}
					c.contents.add((Integer) intv);
					break;
				case STRING:
					c.contents.add(p);
					break;
				case BOOLEAN:
					c.contents.add(Boolean.parseBoolean(p));
					break;
			}
		}
		return c;
	}
}
