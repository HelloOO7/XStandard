package ctrmap.stdlib.fs.accessors.arc;

import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.io.base.LittleEndianIO;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class ArcFile extends FSFile{
	private FSFile source;
	private ArcFileAccessor accessor;
	
	public ArcFile(FSFile source, ArcFileAccessor accessor){
		this.source = source;
		this.accessor = accessor;
	}

	@Override
	public FSFile getChild(String forName) {
		if (forName.startsWith(".")){
			return null;
		}
		System.out.println("ArcFile getChild requested " + this + "/" + forName);
		return new ArcFileMember(this, FSUtil.cleanPath(forName), accessor);
	}

	@Override
	public FSFile getParent() {
		return source.getParent();
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
	public boolean exists() {
		return source.exists();
	}

	@Override
	public String getName() {
		return source.getName();
	}

	@Override
	public InputStream getInputStream() {
		return source.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() {
		return source.getOutputStream();
	}

	@Override
	public LittleEndianIO getIO() {
		return source.getIO();
	}

	@Override
	public List<FSFile> listFiles() {
		return accessor.getArcFiles(this);
	}

	@Override
	public int length() {
		return source.length();
	}
	
	public FSFile getSource(){
		return source;
	}

	@Override
	public void delete() {
		throw new SecurityException("ArcFiles should not be deleted.");
	}

	@Override
	public int getChildCount(boolean includeHidden) {
		return accessor.getArcFiles(this).size();
	}

	@Override
	public void setPath(String newPath) {
		source.setPath(newPath);
	}
}
