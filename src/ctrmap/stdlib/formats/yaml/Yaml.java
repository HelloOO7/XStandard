package ctrmap.stdlib.formats.yaml;

import ctrmap.stdlib.fs.FSFile;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 *
 */
public class Yaml {

	private FSFile document;

	public String documentName;

	public YamlNode root = new YamlNode();

	public Yaml() {

	}

	public Yaml(FSFile fsf) {
		if (fsf.exists()) {
			loadFromInputStream(fsf.getInputStream());
		}
		documentName = fsf.getName();
		document = fsf;
	}

	public Yaml(InputStream strm, String name) {
		loadFromInputStream(strm);
		this.documentName = name;
	}

	private void loadFromInputStream(InputStream strm) {
		Scanner scanner = new Scanner(strm);

		YamlNode currentNode = root;

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.trim().isEmpty()) {
				continue;
			}

			int tabs = 0;
			int spaces = 0;
			OUTER:
			for (int i = 0; i < line.length(); i++) {
				char c = line.charAt(i);
				switch (c) {
					case 0x09:
						tabs++;
						break;
					case 0x20:
						spaces++;
						break;
					default:
						break OUTER;
				}
			}
			tabs += spaces / 4;

			int cpr = currentNode.getParentLevel();

			if (tabs > cpr) {
				currentNode = currentNode.addChild();
			} else if (tabs == cpr) {
				currentNode = currentNode.addSibling();
			} else {
				currentNode = currentNode.parent.addSibling();
			}

			String trim = line.trim();
			if (trim.startsWith("-")) {
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

		scanner.close();
	}
	
	public YamlNode getEnsureRootNodeKeyNode(String key){
		return root.getOrCreateChildKeyByName(key);
	}

	public YamlNode getRootNodeKeyNode(String key) {
		return root.getChildByName(key);
	}
	
	public void removeRootNodeKeyNode(String key){
		root.removeChildByName(key);
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
			PrintStream out = new PrintStream(document.getOutputStream());

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
		for (int i = 0; i < level * 4; i++) {
			indent += " ";
		}
		boolean beganListElem = !writeAsListElem;
		for (YamlNode child : node.children) {
			if (beganListElem) {
				out.print(indent);
			}
			else {
				beganListElem = true;
			}
			boolean childBeginAsList = false;
			if (child.content != null) {
				child.content.print(out);
				childBeginAsList = child.content instanceof YamlListElement;
			}
			if (!childBeginAsList) {
				out.println();
			}

			writeNodeChildren(child, out, nextLevel, childBeginAsList);

			if (child == last && node.parent != root && !writeAsListElem) {
				out.println();
			}
		}
	}

	public void writeToFile(FSFile f) {
		document = f;
		write();
	}
}
