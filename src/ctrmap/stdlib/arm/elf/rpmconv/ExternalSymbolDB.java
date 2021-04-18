
package ctrmap.stdlib.arm.elf.rpmconv;

import ctrmap.stdlib.formats.yaml.Yaml;
import ctrmap.stdlib.formats.yaml.YamlListElement;
import ctrmap.stdlib.formats.yaml.YamlNode;
import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.fs.accessors.DiskFile;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ExternalSymbolDB {
	private List<ESDBSegmentInfo> segments = new ArrayList<>();
	private Map<String, ESDBAddress> offsetMap = new HashMap<>();
	
	public ExternalSymbolDB(){
		
	}
	
	public ExternalSymbolDB(FSFile f){
		Yaml yml = new Yaml(f);
		
		YamlNode segmentsRoot = yml.getRootNodeKeyNode("Segments");
		for (YamlNode seg : segmentsRoot.children){
			segments.add(new ESDBSegmentInfo(seg));
		}
		
		YamlNode symbolsRoot = yml.getRootNodeKeyNode("Symbols");
		for (YamlNode sym : symbolsRoot.children){
			String name = sym.getChildByName("Name").getValue();
			offsetMap.put(name, new ESDBAddress(sym));
		}
	}
	
	public void putSegment(ESDBSegmentInfo seg){
		segments.add(seg);
	}
	
	public void putOffset(String name, ESDBAddress add){
		offsetMap.put(name, add);
	}
	
	public ESDBSegmentInfo getSegByName(String name){
		if (name == null){
			return null;
		}
		for (ESDBSegmentInfo i : segments){
			if (name.equals(i.segmentName)){
				return i;
			}
		}
		return null;
	}
	
	public ESDBSegmentInfo getSegById(int id){
		for (ESDBSegmentInfo i : segments){
			if (i.segmentId == id){
				return i;
			}
		}
		return null;
	}
	
	public boolean isFuncExternal(String name){
		return offsetMap.containsKey(name);
	}
	
	public ESDBSegmentInfo getSegOfFunc(String name){
		ESDBAddress a = offsetMap.get(name);
		if (a != null){
			return getSegById(a.segment);
		}
		return null;
	}
	
	public int getOffsetOfFunc(String name){
		ESDBAddress a = offsetMap.get(name);
		if (a != null){
			return a.address;
		}
		return 0;
	}
	
	public void writeToFile(FSFile fsf){
		Yaml yml = new Yaml();
		
		this.segments.sort((ESDBSegmentInfo o1, ESDBSegmentInfo o2) -> o1.segmentId - o2.segmentId);
		
		YamlNode segments = yml.getEnsureRootNodeKeyNode("Segments");
		for (ESDBSegmentInfo s : this.segments){
			segments.addChild(s.getNode());
		}
		
		YamlNode symbs = yml.getEnsureRootNodeKeyNode("Symbols");
		for (Map.Entry<String, ESDBAddress> sym : offsetMap.entrySet()){
			YamlNode n = new YamlNode(new YamlListElement());
			n.addChild("Name", sym.getKey());
			sym.getValue().addToNode(n);
			symbs.addChild(n);
		}
		
		yml.writeToFile(fsf);
	}
}
