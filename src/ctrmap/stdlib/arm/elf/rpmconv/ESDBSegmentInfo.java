package ctrmap.stdlib.arm.elf.rpmconv;

import ctrmap.stdlib.formats.yaml.YamlListElement;
import ctrmap.stdlib.formats.yaml.YamlNode;

public class ESDBSegmentInfo {
	public int segmentId;
	public String segmentName;
	public String segmentType;
	
	public ESDBSegmentInfo(YamlNode node){
		segmentId = node.getChildByName("ID").getValueInt();
		segmentName = node.getChildByName("Name").getValue();
		segmentType = node.getChildByName("Type").getValue();
	}
	
	public ESDBSegmentInfo(){
		
	}
	
	public YamlNode getNode(){
		YamlNode n = new YamlNode(new YamlListElement());
		n.addChild("ID", segmentId, true);
		n.addChild("Name", segmentName);
		n.addChild("Type", segmentType);
		return n;
	}
}
