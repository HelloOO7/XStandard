package ctrmap.stdlib.fs.accessors.arc;

import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.io.base.LittleEndianIO;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ArcFileMember extends FSFile{
	private ArcFile arc;
	private String path;
	private ArcFileAccessor accessor;
	
	public ArcFileMember(ArcFile arc, String pathInArc, ArcFileAccessor accessor){
		this.arc = arc;
		this.path = pathInArc;
		this.accessor = accessor;
	}

	@Override
	public FSFile getChild(String forName) {
		return null;//no children for arc subfiles as they are retrieved with full path directly
	}

	@Override
	public FSFile getParent() {
		return arc;
	}

	@Override
	public void mkdir() {
		throw new UnsupportedOperationException("Arc files can not be directories yet.");
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public String getName() {
		return path;
	}

	@Override
	public InputStream getInputStream() {
		return accessor.getInputStreamForArcMember(arc, path);
	}

	@Override
	public OutputStream getOutputStream() {
		return accessor.getOutputStreamForArcMember(arc, path);
	}

	@Override
	public LittleEndianIO getIO() {
		return accessor.getIOForArcMember(arc, path);
	}

	@Override
	public List<FSFile> listFiles() {
		return new ArrayList<>();
	}

	@Override
	public int length() {
		return accessor.getDataSizeForArcMember(arc, path);
	}

	@Override
	public void delete() {
		throw new UnsupportedOperationException("ArcFile members can not be directly deleted.");
	}

	@Override
	public int getChildCount(boolean includeInvisible) {
		return 0;//costly
	}

	@Override
	public void setPath(String newPath) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
