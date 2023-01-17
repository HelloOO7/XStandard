package xstandard.formats.yaml;

import xstandard.util.ParsingUtils;
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
		this(key, (String) (value == null ? value : String.valueOf(value)));
	}

	public YamlNode(String key, String value) {
		this(new KeyValuePair(key, value));
	}

	public YamlNode(Object value) {
		this(new Value((String) (value == null ? value : String.valueOf(value))));
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

	public YamlNode addChild(String key, int value, boolean hex) {
		if (hex) {
			return addChild(key, "0x" + Integer.toHexString(value));
		} else {
			return addChild(key, value);
		}
	}

	public YamlNode addChild(String key, Object value) {
		return addChild(new YamlNode(key, value));
	}

	public YamlNode addChildKey(String key) {
		return addChild(new YamlNode(new Key(key)));
	}

	public YamlNode addChildValue(String value) {
		return addChild(new YamlNode(new Value(value)));
	}

	public YamlNode addChildListElem() {
		return addChild(new YamlNode(new YamlListElement()));
	}

	public YamlNode addChild(YamlNode n) {
		children.add(n);
		n.parent = this;
		return n;
	}

	public YamlNode addChild() {
		YamlNode ch = new YamlNode();
		addChild(ch);
		return ch;
	}

	public YamlNode addSibling() {
		return parent.addChild();
	}

	public YamlNode getEnsureChildByName(String name) {
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

	public YamlNode getChildByValue(String value) {
		for (YamlNode ch : children) {
			if (Objects.equals(ch.getValue(), value)) {
				return ch;
			}
		}
		return null;
	}

	public YamlNode getChildByNameIgnoreCase(String name) {
		for (YamlNode ch : children) {
			if (ch.getKey() == null && name == null || ch.getKey().equalsIgnoreCase(name)) {
				return ch;
			}
		}
		return null;
	}
	
	public short getChildShortValue(String name) {
		YamlNode ch = getChildByName(name);
		return ch != null ? ch.getValueShort() : -1;
	}

	public int getChildIntValue(String name) {
		YamlNode ch = getChildByName(name);
		return ch != null ? ch.getValueInt() : -1;
	}

	public long getChildLongValue(String name) {
		YamlNode ch = getChildByName(name);
		return ch != null ? ch.getValueLong() : -1;
	}

	public String getChildValue(String name) {
		YamlNode ch = getChildByName(name);
		return ch != null ? ch.getValue() : null;
	}

	public boolean getChildBoolValue(String name) {
		YamlNode ch = getChildByName(name);
		return ch != null && ch.getValueBool();
	}

	public void removeChildByName(String name) {
		children.remove(getChildByName(name));
	}

	public void removeChildByValue(String value) {
		children.remove(getChildByValue(value));
	}

	public void removeChild(YamlNode ch) {
		children.remove(ch);
	}

	public void setKey(String name) {
		if (name == null) {
			content = new Value(content.getValue());
		} else {
			if (content == null) {
				content = new Key(name);
			} else if (content instanceof Value) {
				content = new KeyValuePair(name, content.getValue());
			} else {
				content.setKey(name);
			}
		}
	}

	public void setValueInt(int val) {
		setValueInt(val, false);
	}

	public void setValueLong(long val) {
		setValue(String.valueOf(val));
	}

	public void setValueInt(int val, boolean hex) {
		setValue(hex ? "0x" + Integer.toHexString(val) : String.valueOf(val));
	}

	public void setValueBool(boolean v) {
		setValue(String.valueOf(v));
	}

	public void setValue(String value) {
		if (value == null) {
			content = new Key(content.getKey());
		} else {
			if (content == null) {
				content = new Value(value);
			} else if (content instanceof Key) {
				content = new KeyValuePair(content.getKey(), value);
			} else {
				content.setValue(value);
			}
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
	
	public short getValueShort() {
		return ParsingUtils.parseBasedShort(getValue());
	}

	public int getValueInt() {
		return ParsingUtils.parseBasedInt(getValue());
	}

	public float getValueFloat() {
		return Float.parseFloat(getValue());
	}

	public long getValueLong() {
		return ParsingUtils.parseBasedLong(getValue());
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
		List<String> l = new ArrayList<>(children.size());
		for (YamlNode n : children) {
			l.add(n.getValue());
		}
		return l;
	}

	public List<Integer> getChildValuesAsListInt() {
		List<Integer> l = new ArrayList<>(children.size());
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
