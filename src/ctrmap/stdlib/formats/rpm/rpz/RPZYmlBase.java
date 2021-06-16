package ctrmap.stdlib.formats.rpm.rpz;

import ctrmap.stdlib.formats.yaml.Yaml;
import ctrmap.stdlib.formats.yaml.YamlReflectUtil;
import ctrmap.stdlib.fs.FSFile;

public class RPZYmlBase extends Yaml {

	public String name;
	public RPZVersion version;

	public RPZYmlBase(FSFile fsf) {
		super(fsf);
		YamlReflectUtil.deserializeToObject(root, this);
	}
	
	public RPZYmlBase(){
		super();
	}

	@Override
	public void write() {
		root.removeAllChildren();
		YamlReflectUtil.addFieldsToNode(root, this);
		super.write();
	}


	public static class RPZYmlReference {
		public String ymlPath;
	}
}
