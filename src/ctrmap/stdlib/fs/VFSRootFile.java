package ctrmap.stdlib.fs;

import ctrmap.stdlib.fs.accessors.DiskFile;
import java.io.File;

public class VFSRootFile extends DiskFile {

	public VFSRootFile(String path) {
		super(path);
	}
	
	public VFSRootFile(File path) {
		super(path);
	}
}
