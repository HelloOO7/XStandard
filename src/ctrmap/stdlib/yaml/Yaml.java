package ctrmap.stdlib.yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Yaml {

	public String documentName;

	public YamlNode root = new YamlNode();

	public Yaml(File f) throws FileNotFoundException {
		this(new FileInputStream(f), f.getName());
	}

	public Yaml(InputStream strm, String name) {
		this.documentName = name;
		Scanner scanner = new Scanner(strm);

		YamlNode currentNode = root;

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.trim().isEmpty()){
				continue;
			}

			int tabs = 0;
			int spaces = 0;
			for (int i = 0; i < line.length(); i++) {
				char c = line.charAt(i);
				if (c == 0x09) {
					tabs++;
				} else if (c == 0x20){
					spaces++;
				} else {
					break;
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
					currentNode.content = SimpleValue.trySet(trim);
				}
			}
		}

		scanner.close();
	}
}
