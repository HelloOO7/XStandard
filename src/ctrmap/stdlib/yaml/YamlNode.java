package ctrmap.stdlib.yaml;

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

	public int getParentLevel() {
		if (parent == null) {
			return -1;
		}
		return parent.getParentLevel() + 1;
	}

	public YamlNode addChild() {
		YamlNode ch = new YamlNode();
		children.add(ch);
		ch.parent = this;
		return ch;
	}

	public YamlNode addSibling() {
		return parent.addChild();
	}

	public YamlNode getChildByName(String name) {
		for (YamlNode ch : children) {
			if (ch.content instanceof KeyValuePair) {
				if (((KeyValuePair) ch.content).key.equals(name)) {
					return ch;
				}
			} else if (ch.content instanceof Key) {
				if (((Key) ch.content).key.equals(name)) {
					return ch;
				}
			}
		}
		return null;
	}

	public String getValue() {
		if (content instanceof KeyValuePair) {
			return ((KeyValuePair)content).value;
		} else if (content instanceof SimpleValue) {
			return ((SimpleValue)content).value;
		}
		return null;
	}
	
	public int getValueInt() {
		return Integer.parseInt(getValue());
	}
	
	@Override
	public String toString(){
		return Objects.toString(content);
	}
}
