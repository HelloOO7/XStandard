package xstandard.formats.yaml;

import xstandard.fs.FSFile;
import xstandard.gui.file.ExtensionFilter;
import xstandard.io.base.iface.ReadableStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.Stack;

/**
 *
 */
public class Yaml {
	
	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Yet Another Markup Language", "*.yml", "*.yaml");

	protected transient FSFile document;

	public transient String documentName;

	public transient YamlNode root = new YamlNode();

	public Yaml() {

	}
	
	public FSFile getFile(){
		return document;
	}

	public Yaml(FSFile fsf) {
		if (fsf.exists()) {
			loadFromInputStream(fsf.getInputStream());
		}
		documentName = fsf.getName();
		document = fsf;
	}

	public Yaml(ReadableStream strm, String name) {
		loadFromInputStream(strm);
		this.documentName = name;
	}

	private void loadFromInputStream(ReadableStream strm) {
		try (Scanner scanner = new Scanner(strm.getInputStream(), "UTF-8")) {
			YamlNode currentNode = root;
			
			Stack<Integer> parentLevels = new Stack<>();
			parentLevels.push(-1);
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.trim().isEmpty()) {
					continue;
				}
				
				int spaces = 0;
				OUTER:
				for (int i = 0; i < line.length(); i++) {
					char c = line.charAt(i);
					switch (c) {
						case 0x09:
							spaces += 4;
							break;
						case 0x20:
							spaces++;
							break;
						default:
							break OUTER;
					}
				}
				
				int peek = parentLevels.peek();
				if (spaces > peek){
					parentLevels.push(spaces);
					currentNode = currentNode.addChild();
				}
				else if (spaces == peek){
					currentNode = currentNode.addSibling();
				}
				else {
					YamlNode parent = currentNode;
					while (parentLevels.peek() > spaces){
						parentLevels.pop();
						parent = parent.parent;
					}
					
					currentNode = parent.addSibling();
				}
				
				/*if (spaces > cpr) {
				currentNode = currentNode.addChildKey();
				} else if (spaces == cpr) {
				currentNode = currentNode.addSibling();
				} else {
				currentNode = currentNode.parent.addSibling();
				}*/
				String trim = line.trim();
				if (trim.startsWith("-")) {
					parentLevels.push(spaces + 2);
					
					currentNode.content = new YamlListElement(currentNode);
					currentNode = currentNode.addChild();
					trim = trim.substring(1, trim.length()).trim();
				}
				
				KeyValuePair kvp = KeyValuePair.trySet(trim);
				if (kvp != null) {
					currentNode.content = kvp;
				} else {
					Key k = Key.trySet(trim);
					
					if (k != null) {
						currentNode.content = k;
					} else {
						currentNode.content = Value.trySet(trim);
					}
				}
			}
		}
	}

	public YamlNode getEnsureRootNodeKeyNode(String key) {
		return root.getEnsureChildByName(key);
	}

	public YamlNode getRootNodeKeyNode(String key) {
		return root.getChildByName(key);
	}

	public void removeRootNodeKeyNode(String key) {
		root.removeChildByName(key);
	}

	public int getRootNodeKeyValueInt(String key) {
		return root.getChildIntValue(key);
	}
	
	public boolean getRootNodeKeyValueBool(String key) {
		YamlNode node = getRootNodeKeyNode(key);
		if (node != null) {
			return node.getValueBool();
		}
		return false;
	}

	public String getRootNodeKeyValue(String key) {
		YamlNode node = getRootNodeKeyNode(key);
		if (node != null) {
			return node.getValue();
		}
		return null;
	}

	public void write() {
		if (document != null) {
			PrintStream out = new PrintStream(document.getNativeOutputStream());

			writeNodeChildren(root, out, 0, false);

			out.close();
		}
	}

	private void writeNodeChildren(YamlNode node, PrintStream out, int level, boolean writeAsListElem) {
		if (node.children.isEmpty()) {
			return;
		}
		YamlNode last = node.children.get(node.children.size() - 1);
		int nextLevel = level + 1;
		String indent = "";
		for (int i = 0; i < level * 2; i++) {
			indent += " ";
		}
		boolean beganListElem = !writeAsListElem;
		for (YamlNode child : node.children) {
			if (beganListElem) {
				out.print(indent);
			} else {
				beganListElem = true;
			}
			boolean childBeginAsList = false;
			if (child.content != null) {
				child.content.print(out);
				childBeginAsList = child.content instanceof YamlListElement;
			}
			if (!childBeginAsList || child.children.isEmpty()) {
				out.println();
			}

			writeNodeChildren(child, out, nextLevel, childBeginAsList);

			if (child == last && node.parent == root) {
				out.println();
			}
		}
	}

	public void writeToFile(FSFile f) {
		document = f;
		write();
	}
}
