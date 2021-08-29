package ctrmap.stdlib.formats.rpm.rpz;

import ctrmap.stdlib.formats.yaml.Yaml;
import ctrmap.stdlib.formats.yaml.YamlNodeName;
import ctrmap.stdlib.formats.yaml.YamlReflectUtil;
import ctrmap.stdlib.fs.FSFile;

class RPZYmlBase extends Yaml {

	@YamlNodeName("Name")
	public String name;
	@YamlNodeName("Version")
	public RPZVersion version;

	public RPZYmlBase(FSFile fsf) {
		super(fsf);
		YamlReflectUtil.deserializeToObject(root, this);
	}
	
	public RPZYmlBase(){
		super();
		version = new RPZVersion();
	}

	@Override
	public void write() {
		root.removeAllChildren();
		YamlReflectUtil.addFieldsToNode(root, this);
		super.write();
	}


	public static class RPZYmlReference {
		@YamlNodeName("YmlPath")
		public String ymlPath;
	}
}
