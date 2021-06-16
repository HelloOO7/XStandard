package ctrmap.stdlib.formats.rpm.rpz;

import ctrmap.stdlib.fs.FSFile;
import java.util.ArrayList;
import java.util.List;

public class RPZIndex extends RPZYmlBase {

	public String productId;
	
	public String description;
	
	public List<RPZYmlReference> modules;

	public RPZIndex(FSFile fsf) {
		super(fsf);
	}
	
	public RPZIndex(){
		modules = new ArrayList<>();
	}
}
