package ctrmap.stdlib.fs.accessors;

import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.io.base.iface.IOStream;
import ctrmap.stdlib.io.base.iface.ReadableStream;
import ctrmap.stdlib.io.base.iface.WriteableStream;
import java.util.List;

/**
 *
 */
public class FSFileAdapter extends FSFile {
	
	protected FSFile source;
	
	public FSFileAdapter(FSFile toWrap){
		source = toWrap;
	}

	public FSFile getSource(){
		return source;
	}
	
	@Override
	public FSFile getChild(String forName) {
		return source.getChild(forName);
	}

	@Override
	public FSFile getParent() {
		return source.getParent();
	}

	@Override
	public void mkdir() {
		source.mkdir();
	}

	@Override
	public void setPath(String newPath) {
		source.setPath(newPath);
	}

	@Override
	public void delete() {
		source.delete();
	}

	@Override
	public int length() {
		return source.length();
	}

	@Override
	public boolean isDirectory() {
		return source.isDirectory();
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
	public ReadableStream getInputStream() {
		return source.getInputStream();
	}

	@Override
	public WriteableStream getOutputStream() {
		return source.getOutputStream();
	}

	@Override
	public IOStream getIO() {
		return source.getIO();
	}

	@Override
	public List<FSFile> listFiles() {
		return source.listFiles();
	}

	@Override
	public int getChildCount() {
		return source.getChildCount();
	}

	@Override
	public int getPermissions() {
		return source.getPermissions();
	}

}
