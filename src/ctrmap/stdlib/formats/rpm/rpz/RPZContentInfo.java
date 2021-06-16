package ctrmap.stdlib.formats.rpm.rpz;

import ctrmap.stdlib.fs.FSFile;
import java.util.ArrayList;
import java.util.List;

public class RPZContentInfo extends RPZYmlBase {
	
	public List<RPZContentReference> content;

	public RPZContentInfo(FSFile fsf) {
		super(fsf);
	}
	
	public RPZContentInfo(){
		content = new ArrayList<>();
	}
	
	public static class RPZContentReference {
		public String sourcePath;
		public String destinationPath;
	}
}
