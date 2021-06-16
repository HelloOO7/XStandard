package ctrmap.stdlib.formats.rpm.rpz;

import ctrmap.stdlib.fs.FSFile;
import java.util.ArrayList;
import java.util.List;

public class RPZModuleInfo extends RPZYmlBase {
	public boolean isDependencyModule;
	public String productId;
	public String rpmName;
	public List<RPZTarget> targets;
	
	public List<RPZDependency> dependencies;
	public RPZYmlReference contentInfo;

	public RPZModuleInfo(FSFile fsf) {
		super(fsf);
		
		if (dependencies == null){
			dependencies = new ArrayList<>();
		}
		if (targets == null){
			targets = new ArrayList<>();
		}
	}
	
	public RPZModuleInfo(){
		targets = new ArrayList<>();
		dependencies = new ArrayList<>();
	}
	
	public static class RPZTarget {
		public String targetName;
	}
	
	public static class RPZDependency {
		public String productId;
		public int version;
		public VersionCmpOp versionOperator;
		public String bundledDepModule;
		
		public static enum VersionCmpOp {
			LESS,
			EQUAL,
			GEQUAL
		}
	}
}
