/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package xstandard.formats.yaml;

import java.io.PrintStream;

/**
 *
 */
public class YamlListElement extends YamlContent {
	
	private YamlNode node;
	
	public YamlListElement(YamlNode node){
		this.node = node;
	}
	
	public YamlListElement(){
		
	}
	
	void assign(YamlNode n){
		node = n;
	}

	private YamlNode getFirstChild(){
		return node.children.get(0);
	}
	
	@Override
	public String getKey() {
		return getFirstChild().getKey();
	}

	@Override
	public String getValue() {
		return getFirstChild().getValue();
	}

	@Override
	public void print(PrintStream out) {
		out.print("- ");
	}

	@Override
	public void setKey(String key) {
		getFirstChild().setKey(key);
	}

	@Override
	public void setValue(String value) {
		getFirstChild().setValue(value);
	}

}
