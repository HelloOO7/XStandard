package ctrmap.stdlib.formats.yaml;

import ctrmap.stdlib.util.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public class YamlNode {

	public YamlContent content;

	public List<YamlNode> children = new ArrayList<>();
	public YamlNode parent;

	public YamlNode() {

	}

	public YamlNode(YamlContent cnt) {
		content = cnt;

		if (content instanceof YamlListElement) {
			((YamlListElement) content).assign(this);
		}
	}

	public YamlNode(String key, Object value) {
		this(key, (String)(value == null ? value : String.valueOf(value)));
	}

	public YamlNode(String key, String value) {
		this(new KeyValuePair(key, value));
	}

	public YamlNode(String value) {
		this(new Value(value));
	}

	public int getParentLevel() {
		if (parent == null) {
			return -1;
		}
		return parent.getParentLevel() + 1;
	}

	public void removeAllChildren() {
		children.clear();
	}

	public void addChild(String key, int value, boolean hex) {
		if (hex) {
			addChild(key, "0x" + Integer.toHexString(value));
		} else {
			addChild(key, value);
		}
	}

	public void addChild(String key, Object value) {
		addChild(new YamlNode(key, value));
	}

	public void addChild(YamlNode n) {
		children.add(n);
		n.parent = this;
	}

	public YamlNode addChild() {
		YamlNode ch = new YamlNode();
		addChild(ch);
		return ch;
	}

	public YamlNode addSibling() {
		return parent.addChild();
	}

	public YamlNode getOrCreateChildKeyByName(String name) {
		YamlNode ch = getChildByName(name);
		if (ch == null) {
			ch = addChild();
			ch.setKey(name);
		}
		return ch;
	}

	public YamlNode getChildByName(String name) {
		for (YamlNode ch : children) {
			if (Objects.equals(ch.getKey(), name)) {
				return ch;
			}
		}
		return null;
	}

	public int getChildIntValue(String name) {
		YamlNode ch = getChildByName(name);
		return ch != null ? ch.getValueInt() : -1;
	}
	
	public String getChildValue(String name) {
		return getChildByName(name).getValue();
	}
	
	public boolean getChildBoolValue(String name) {
		YamlNode ch = getChildByName(name);
		return ch != null && ch.getValueBool();
	}

	public void removeChildByName(String name) {
		children.remove(getChildByName(name));
	}

	public void removeChild(YamlNode ch) {
		children.remove(ch);
	}

	public void setKey(String name) {
		if (content == null) {
			content = new Key(name);
		} else if (content instanceof Value) {
			content = new KeyValuePair(name, content.getValue());
		} else {
			content.setKey(name);
		}
	}
	
	public void setValueInt(int val) {
		setValueInt(val, false);
	}
	
	public void setValueInt(int val, boolean hex) {
		setValue(hex ? "0x" + Integer.toHexString(val) : String.valueOf(val));
	}

	public void setValueBool(boolean v) {
		setValue(String.valueOf(v));
	}

	public void setValue(String value) {
		if (content == null) {
			content = new Value(value);
		} else if (content instanceof Key) {
			content = new KeyValuePair(content.getKey(), value);
		} else {
			content.setValue(value);
		}
	}

	public String getKey() {
		if (content == null) {
			return null;
		}
		return content.getKey();
	}

	public String getValue() {
		if (content == null) {
			return null;
		}
		return content.getValue();
	}

	public boolean getValueBool() {
		return Boolean.parseBoolean(getValue());
	}

	public int getKeyInt() {
		return ParsingUtils.parseBasedInt(getKey());
	}

	public int getValueInt() {
		return ParsingUtils.parseBasedInt(getValue());
	}

	public boolean isValueInt() {
		try {
			getValueInt();
			return true;
		} catch (NumberFormatException ex) {

		}
		return false;
	}
	
	public boolean isKeyInt() {
		try {
			getKeyInt();
			return true;
		} catch (NumberFormatException ex) {

		}
		return false;
	}

	public boolean hasChildren(String... names) {
		for (String n : names) {
			if (getChildByName(n) == null) {
				return false;
			}
		}
		return true;
	}

	public List<String> getChildValuesAsListStr() {
		List<String> l = new ArrayList<>();
		for (YamlNode n : children) {
			l.add(n.getValue());
		}
		return l;
	}

	public List<Integer> getChildValuesAsListInt() {
		List<Integer> l = new ArrayList<>();
		for (YamlNode n : children) {
			l.add(n.getValueInt());
		}
		return l;
	}

	@Override
	public String toString() {
		return Objects.toString(content);
	}
}
