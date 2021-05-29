package ctrmap.stdlib.fs.accessors.arc;

import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.fs.accessors.FSFileAdapter;
import java.util.List;

public class ArcFile extends FSFileAdapter {
	private ArcFileAccessor accessor;
	
	public ArcFile(FSFile source, ArcFileAccessor accessor){
		super(source);
		this.source = source;
		this.accessor = accessor;
	}

	@Override
	public FSFile getChild(String forName) {
		if (forName.startsWith(".")){
			return null;
		}
		//System.out.println("ArcFile getChild requested " + this + "/" + forName);
		return new ArcFileMember(this, FSUtil.cleanPath(forName), accessor);
	}

	@Override
	public void mkdir() {
		throw new UnsupportedOperationException("ArcFiles can not be directories.");
	}

	@Override
	public boolean isDirectory() {
		return true;
	}

	@Override
	public List<FSFile> listFiles() {
		return accessor.getArcFiles(this);
	}

	@Override
	public void delete() {
		throw new SecurityException("ArcFiles should not be deleted.");
	}

	@Override
	public int getChildCount() {
		return accessor.getArcFiles(this).size();
	}

	@Override
	public int getPermissions() {
		return FSF_ATT_READ | FSF_ATT_WRITE;
	}
}
