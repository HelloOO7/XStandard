package ctrmap.stdlib.fs;

import ctrmap.stdlib.fs.accessors.arc.ArcFileAccessor;

public interface FSManager {
	public ArcFileAccessor getArcFileAccessor();
	
	public FSFile getFsFile(String path);
}
