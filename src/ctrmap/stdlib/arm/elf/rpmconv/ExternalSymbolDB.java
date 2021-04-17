
package ctrmap.stdlib.arm.elf.rpmconv;

import ctrmap.stdlib.formats.yaml.Yaml;
import ctrmap.stdlib.formats.yaml.YamlNode;
import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.fs.accessors.DiskFile;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ExternalSymbolDB {
	private Map<String, Integer> offsetMap = new HashMap<>();
	
	public ExternalSymbolDB(FSFile f){
		Yaml yml = new Yaml(f);
		for (YamlNode ch : yml.root.children){
			offsetMap.put(ch.getKey(), ch.getValueInt());
		}
	}
	
	public boolean isFuncExternal(String name){
		return offsetMap.containsKey(name);
	}
	
	public int getOffsetOfFunc(String name){
		return offsetMap.getOrDefault(name, 0);
	}
}
