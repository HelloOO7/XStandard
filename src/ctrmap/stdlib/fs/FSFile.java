package ctrmap.stdlib.fs;

import ctrmap.stdlib.io.base.LittleEndianIO;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public abstract class FSFile {

	public abstract FSFile getChild(String forName);

	public abstract FSFile getParent();

	public abstract void mkdir();
	
	public abstract void setPath(String newPath);

	public abstract void delete();

	public abstract int length();

	public abstract boolean isDirectory();

	public abstract boolean exists();

	public abstract String getName();

	public abstract InputStream getInputStream();

	public abstract OutputStream getOutputStream();

	public abstract LittleEndianIO getIO();

	public abstract List<FSFile> listFiles();

	public abstract int getChildCount(boolean includeInvisible);

	public byte[] getBytes(){
		return FSUtil.readFileToBytes(this);
	}
	
	public void setBytes(byte[] bytes){
		FSUtil.writeBytesToFile(this, bytes);
	}
	
	public int getChildCount() {
		return getChildCount(true);
	}

	public int getVisibleChildCount() {
		return getChildCount(false);
	}

	public static int getHiddenCount(List<FSFile> children) {
		int hidden = 0;

		for (FSFile f : children) {
			if (f.getName().startsWith(".")) {
				hidden++;
			}
		}
		
		return hidden;
	}
	
	public boolean isFile(){
		return exists() && !isDirectory();
	}

	public String getPath() {
		if (getParent() != null) {
			return (getParent().getPath() + "/" + getName()).replace('\\', '/');
		}
		return getName();
	}

	@Override
	public boolean equals(Object o){
		if (o != null && o instanceof FSFile){
			return ((FSFile)o).getPath().equals(getPath());
		}
		return false;
	}
	
	public String getPathRelativeTo(FSFile relativeTo) {
		return getPathRelativeTo(getPath(), relativeTo.getPath());
	}

	public static String getPathRelativeTo(String path, String relPath) {
		relPath += "/";
		if (path.startsWith(relPath)) {
			String str = path.replace(relPath, "");
			return str;
		}
		return path;
	}

	public void mkdirs() {
		Stack<FSFile> parentStack = new Stack<>();
		FSFile currentParent = this;
		while (currentParent != null && !currentParent.exists()) {
			parentStack.push(currentParent);
			currentParent = currentParent.getParent();
		}
		if (exists() && !isDirectory()) {
			parentStack.remove(this);
		}
		while (!parentStack.empty()) {
			parentStack.pop().mkdir();
		}
	}

	public FSFile getMatchingChild(String childPath) {
		int slashIdx = childPath.indexOf("/");
		String immChildName = childPath.substring(0, slashIdx != -1 ? slashIdx : childPath.length());
		String followChildPath = childPath.substring(slashIdx + 1);
		FSFile child;
		if (immChildName.startsWith(":") && immChildName.endsWith(":")) {
			child = VFS.getExistingRefFile(this, immChildName);
		} else {
			child = getChild(immChildName);
		}
		if (child != null) {
			if (FSWildCard.getWildCardedPath(child.getName()).equals(FSWildCard.getWildCardedPath(childPath))) {
				return child;
			}
			return child.getMatchingChild(followChildPath);
		}
		return null;
	}

	public List<String> list() {
		List<FSFile> files = listFiles();
		List<String> ret = new ArrayList<>();
		for (FSFile f : files) {
			ret.add(f.getPath());
		}
		return ret;
	}

	@Override
	public String toString() {
		return getPath();
	}
}
