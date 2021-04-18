package ctrmap.stdlib.arm.elf.rpmconv;

import ctrmap.stdlib.formats.yaml.YamlNode;

public class ESDBAddress {
	public int segment;
	public int address;
	
	public ESDBAddress(YamlNode node){
		segment = node.getChildByName("Segment").getValueInt();
		address = node.getChildByName("Address").getValueInt();
	}
	
	public ESDBAddress(int seg, int add){
		segment = seg;
		address = add;
	}
	
	public void addToNode(YamlNode n){
		n.addChild("Segment", segment, true);
		n.addChild("Address", address, true);
	}
}
