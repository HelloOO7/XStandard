package xstandard.fs;

import xstandard.io.base.iface.IOStream;
import xstandard.io.base.iface.ReadableStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import xstandard.io.base.iface.WriteableStream;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.base.impl.ext.data.DataInStream;
import xstandard.io.base.impl.ext.data.DataOutStream;
import xstandard.text.StringEx;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Objects;

public abstract class FSFile implements Comparable<FSFile> {

	/**
	 * Indicates that the file can be read from.
	 */
	public static final int FSF_ATT_READ = 1;
	/**
	 * Indicates that the file can be written to.
	 */
	public static final int FSF_ATT_WRITE = 2;
	/**
	 * Indicates that the file can be executed.
	 */
	public static final int FSF_ATT_EXECUTE = 4;

	public void tree(PrintStream out) {
		out.println(getName());
		printChildren(out, 0, listFiles());
	}

	private static void printChildren(PrintStream out, int indent, List<? extends FSFile> children) {
		StringBuilder id = new StringBuilder();
		for (int i = 0; i < indent; i++) {
			id.append(" ");
		}
		for (FSFile child : children) {
			out.println(id + "└─ " + child.getName());
			printChildren(out, indent + 4, child.listFiles());
		}
	}

	/**
	 * Gets a child file in this directory,
	 *
	 * @param forName Name of the child file.
	 * @return
	 */
	public abstract FSFile getChild(String forName);

	public FSFile getAnyChild(String... names) {
		if (names.length == 0) {
			return null;
		}
		for (String name : names) {
			FSFile ch = getChild(name);
			if (ch != null && ch.exists()) {
				return ch;
			}
		}
		return getChild(names[0]);
	}

	/**
	 * Gets the parent directory of this file.
	 *
	 * @return The parent directory, or null if this is the root folder.
	 */
	public abstract FSFile getParent();

	/**
	 * Creates a directory denoted by this file.
	 */
	public abstract void mkdir();

	/**
	 * Changes this file's path. Also known as moving/renaming,
	 *
	 * @param newPath New path of the file.
	 */
	public abstract void setPath(String newPath);

	/**
	 * Deletes this file/directory.
	 */
	public abstract void delete();

	/**
	 * Gets the number of bytes in this file.
	 *
	 * @return The file length, or 0 if it is a directory.
	 */
	public abstract int length();

	/**
	 * Checks if the file is a directory.
	 *
	 * @return True if the file is a directory, false if it is a file.
	 */
	public abstract boolean isDirectory();

	/**
	 * Checks if the file exists.
	 *
	 * @return True if the file exists, false if otherwise.
	 */
	public abstract boolean exists();

	public static boolean exists(FSFile file) {
		return file != null && file.exists();
	}

	/**
	 * Gets the file's name.
	 *
	 * @return The file name.
	 */
	public abstract String getName();

	/**
	 * Creates a ReadableStream directly from the file.
	 *
	 * @return
	 */
	public abstract ReadableStream getInputStream();

	/**
	 * Creates a WriteableStream from the file. Any bytes written into the stream must sooner or later be reflected in the file.
	 *
	 * @return
	 */
	public abstract WriteableStream getOutputStream();

	/**
	 * Creates an IOStream from the file. Any bytes written into the stream must sooner or later be reflected in the file.
	 *
	 * @return
	 */
	public abstract IOStream getIO();

	/**
	 * Lists all child files in this directory.
	 *
	 * @return A list of all child files, or an empty list if this is not a directory.
	 */
	public abstract List<? extends FSFile> listFiles();

	/**
	 * Gets the permission atributes of the file.
	 *
	 * @return A bitfield of the permissions. See FSF_ATT_READ, FSF_ATT_WRITE and FSF_ATT_EXECUTE.
	 */
	public abstract int getPermissions();

	/**
	 * Checks if the file can be read from.
	 *
	 * @return
	 */
	public boolean canRead() {
		return exists() && (getPermissions() & FSF_ATT_READ) != 0;
	}

	/**
	 * Checks if the file can be written to.
	 *
	 * @return
	 */
	public boolean canWrite() {
		return (getPermissions() & FSF_ATT_WRITE) != 0;
	}

	/**
	 * Checks if the file can be executed.
	 *
	 * @return
	 */
	public boolean canExecute() {
		return (getPermissions() & FSF_ATT_EXECUTE) != 0;
	}

	/**
	 * Gets the name of this file without the extension, if one is present.
	 *
	 * @return
	 */
	public String getNameWithoutExtension() {
		return FSUtil.getFileNameWithoutExtension(getName());
	}

	/**
	 * Checks for multiple file permissions.
	 *
	 * @param flags The permissions to check. Can be either FSF_ATT_READ, FSF_ATT_WRITE or FSF_ATT_EXECUTE.
	 * @return True if the file has all of the permissions.
	 */
	//privilege check sounds better but does not look good in code
	public boolean checkPrivileges(int... flags) {
		int p = getPermissions();
		for (int f : flags) {
			if ((p & f) == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the number of files in this directory.
	 *
	 * @return Number of files in the directory, or 0 if this is a file.
	 */
	public int getChildCount() {
		return listFiles().size();
	}

	/**
	 * Reads the file into a byte array.
	 *
	 * @return
	 */
	public byte[] getBytes() {
		return FSUtil.readFileToBytes(this);
	}

	/**
	 * Writes a byte array in whole into the file.
	 *
	 * @param bytes
	 */
	public void setBytes(byte[] bytes) {
		FSUtil.writeBytesToFile(this, bytes);
	}

	public void touch() {
		setBytes(new byte[0]);
	}
	
	/**
	 * Creates a DataInStream from the file.
	 *
	 * @return
	 */
	public DataInStream getDataInputStream() {
		return new DataInStream(getInputStream());
	}

	/**
	 * Creates a DataOutStream to the file.
	 *
	 * @return
	 */
	public DataOutStream getDataOutputStream() {
		return new DataOutStream(getOutputStream());
	}

	/**
	 * Creates a DataIOStream of the file.
	 *
	 * @return
	 */
	public DataIOStream getDataIOStream() {
		return new DataIOStream(getIO());
	}

	/**
	 * Creates an InputStream from the file.
	 *
	 * @return
	 */
	public InputStream getNativeInputStream() {
		return getInputStream().getInputStream();
	}

	/**
	 * Creates an OutputStream to the file.
	 *
	 * @return
	 */
	public OutputStream getNativeOutputStream() {
		return getOutputStream().getOutputStream();
	}

	/**
	 * Gets the number of visible files in this directory. A hidden file is any whose file name begins with '.'.
	 *
	 * @return
	 */
	public int getVisibleChildCount() {
		List<? extends FSFile> files = listFiles();
		return files.size() - getHiddenCount(files);
	}

	/**
	 * Gets the number of hidden files in this directory. A hidden file is any whose file name begins with '.'.
	 *
	 * @return
	 */
	public int getHiddenChildCount() {
		return getHiddenCount(listFiles());
	}

	protected static int getHiddenCount(List<? extends FSFile> children) {
		int hidden = 0;

		for (FSFile f : children) {
			if (f.getName().startsWith(".")) {
				hidden++;
			}
		}

		return hidden;
	}

	/**
	 * Checks if the file is a file - that is, it exists and isn't a directory.
	 *
	 * @return
	 */
	public boolean isFile() {
		return exists() && !isDirectory();
	}

	/**
	 * Gets the full path of the file.
	 *
	 * @return
	 */
	public String getPath() {
		if (getParent() != null) {
			return (getParent().getPath() + "/" + getName()).replace('\\', '/');
		}
		return getName();
	}

	/**
	 * Renames the file.
	 *
	 * @param name New name of the file.
	 */
	public void renameTo(String name) {
		FSFile p = getParent();
		if (p == null) {
			setPath(name);
		} else {
			setPath(p.getPath() + "/" + name);
		}
	}

	/**
	 * Compares the file to an object.
	 *
	 * @param o
	 * @return True if the object is the file and its path matches this file's.
	 */
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof FSFile) {
			return ((FSFile) o).getPath().equals(getPath());
		}
		return false;
	}
	
	public void copyTo(FSFile dest) {
		FSUtil.copy(this, dest);
	}

	@Override
	public int compareTo(FSFile fsf) {
		return getPath().compareTo(fsf.getPath());
	}

	/**
	 * Gets this file's path, stripped of a parent file's.
	 *
	 * @param relativeTo The parent file to base the path from.
	 * @return
	 */
	public String getPathRelativeTo(FSFile relativeTo) {
		return getPathRelativeTo(getPath(), relativeTo.getPath());
	}

	/**
	 * Gets a path relative to another.
	 *
	 * @param path The full path.
	 * @param relPath The path to make the full path relative to.
	 * @return 'path' relative to 'relPath'.
	 */
	public static String getPathRelativeTo(String path, String relPath) {
		path = path.replace('\\', '/');
		relPath = relPath.replace('\\', '/');
		if (relPath.isEmpty()) {
			return path;
		}
		if (path.equals(relPath)) {
			return "";
		}
		if (path.startsWith(relPath)) {
			String str = StringEx.deleteAllString(path, relPath);
			if (str.startsWith("/")) {
				str = str.substring(1);
			}
			return str;
		}
		String[] elems = StringEx.splitOnecharFastNoBlank(path, '/');
		String[] relElems = StringEx.splitOnecharFastNoBlank(relPath, '/');
		boolean isReltoDir = relPath.endsWith("/");
		int commonElemsCount = 0;
		for (; commonElemsCount < Math.min(elems.length, relElems.length); commonElemsCount++) {
			if (!elems[commonElemsCount].equals(relElems[commonElemsCount])) {
				break;
			}
		}
		if (commonElemsCount > 0) {
			StringBuilder out = new StringBuilder();
			for (int i = 0; i < relElems.length - commonElemsCount - (isReltoDir ? 0 : 1); i++) {
				out.append("../");
			}
			for (int i = commonElemsCount; i < elems.length; i++) {
				if (i != commonElemsCount) {
					out.append("/");
				}
				out.append(elems[i]);
			}
			return out.toString();
		}
		return path;
	}

	/**
	 * Creates a directory denoted by this file, and all parent directories required for this operation to succeed.
	 */
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

	/**
	 * Gets an existing child file with wild card support.
	 *
	 * @param childPath Path of the child file.
	 * @param mng A WildCard manager.
	 * @return The child file, or null if it could not be matched.
	 */
	public FSFile getMatchingChild(String childPath, FSWildCardManager mng) {
		if (childPath == null || childPath.isEmpty()) {
			return null;
		}
		int slashIdx = childPath.indexOf("/");
		String immChildName = childPath.substring(0, slashIdx != -1 ? slashIdx : childPath.length());
		String followChildPath = childPath.substring(slashIdx + 1);
		FSFile child;
		if (immChildName.startsWith(":") && immChildName.endsWith(":")) {
			child = mng.getExistingRefFile(this, immChildName);
		} else {
			child = getChild(immChildName);
		}
		if (child != null) {
			if (mng.getWildCardedPath(child.getName()).equals(mng.getWildCardedPath(childPath))) {
				return child;
			}
			return child.getMatchingChild(followChildPath, mng);
		}
		return null;
	}

	/**
	 * Lists the child files as a String array.
	 *
	 * @return
	 */
	public List<String> list() {
		List<? extends FSFile> files = listFiles();
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

	@Override
	public int hashCode() {
		return Objects.hashCode(getPath());
	}
}
