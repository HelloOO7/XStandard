package xstandard.fs.accessors.arc;

import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.fs.accessors.FSFileAdapter;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.util.List;

/**
 * A FSFile adapter linked to an ArcFileAccessor for obtaining files from
 * mounted archives.
 */
public class ArcFile extends FSFileAdapter {

	private ArcFileAccessor accessor;

	public ArcFile(FSFile source, ArcFileAccessor accessor) {
		super(source);
		this.source = source;
		this.accessor = accessor;
	}

	@Override
	public FSFile getChild(String forName) {
		if (forName.startsWith(".")) {
			return null;
		}
		//System.out.println("ArcFile getChild requested " + this + "/" + forName);
		return new ArcFileMember(this, FSUtil.cleanPathFromRootSlash(forName), accessor);
	}

	@Override
	public void mkdir() {
		throw new UnsupportedOperationException("ArcFiles can not be directories.");
	}

	@Override
	public DataIOStream getDataIOStream(){
		return super.getDataIOStream();
	}
	
	/*@Override
	public boolean isDirectory() {
		return true;
	}*/
	@Override
	public List<FSFile> listFiles() {
		return accessor.getArcFiles(this);
	}

	@Override
	public void delete() {
		System.err.println("ArcFiles should not be deleted.");
		super.delete();
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
