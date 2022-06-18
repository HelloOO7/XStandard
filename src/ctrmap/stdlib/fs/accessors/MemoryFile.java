package ctrmap.stdlib.fs.accessors;

import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.io.base.iface.IOStream;
import ctrmap.stdlib.io.base.iface.ReadableStream;
import ctrmap.stdlib.io.base.iface.WriteableStream;
import ctrmap.stdlib.io.base.impl.access.MemoryStream;
import ctrmap.stdlib.text.StringEx;
import ctrmap.stdlib.util.ArraysEx;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A virtual file or directory stored completely in system memory.
 */
public class MemoryFile extends FSFile {

	private boolean isDirectory = false;
	private boolean exists = false;

	private byte[] data;
	private String name;
	private FSFile parent;
	private final List<FSFile> children = new ArrayList<>();

	/**
	 * Creates a MemoryFile using a byte array with the given name.
	 *
	 * @param name An arbitrary name of the file.
	 * @param data File data.
	 */
	public MemoryFile(String name, byte[] data) {
		this.data = data;
		this.name = name;
		exists = data != null;
		isDirectory = data == null;
	}

	public MemoryFile(String name) {
		this(name, (byte[]) null);
	}

	MemoryFile(String name, FSFile parent) {
		this(name);
		this.parent = parent;
	}

	public static MemoryFile createBlankFile(String name) {
		MemoryFile f = new MemoryFile(name);
		f.touch();
		return f;
	}
	
	public static MemoryFile createDirectory(String name) {
		MemoryFile f = new MemoryFile(name);
		f.mkdir();
		return f;
	}

	/**
	 * @return
	 */
	@Override
	public boolean isDirectory() {
		return isDirectory;
	}

	private void setDirModeSafe() {
		if (exists && !isDirectory) {
			throw new UnsupportedOperationException(this + " is not a directory!");
		}
		isDirectory = true;
		exists = true;
	}

	private void setFileModeSafe() {
		if (exists && isDirectory) {
			throw new UnsupportedOperationException(this + " is a directory!");
		}
		isDirectory = false;
		exists = true;
	}

	public MemoryFile createChildDir(String name) {
		setDirModeSafe();
		MemoryFile d = new MemoryFile(name);
		d.mkdir();
		linkChild(d);
		return d;
	}

	public void linkChildren(FSFile srcDir) {
		for (FSFile f : srcDir.listFiles()) {
			linkChild(f);
		}
	}

	public void linkChild(FSFile file) {
		setDirModeSafe();
		if (file instanceof MemoryFile) {
			((MemoryFile) file).parent = this;
		}
		ArraysEx.addIfNotNullOrContains(children, file);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ReadableStream getInputStream() {
		if (!exists || isDirectory) {
			return null;
		}
		return getIO();
	}

	@Override
	public WriteableStream getOutputStream() {
		return getIO();
	}

	@Override
	public IOStream getIO() {
		setFileModeSafe();
		//
		// Create a MemoryStream that sets the byte[] of this MemoryFile to the MemoryStream contents when flushed.
		//
		return new MemoryStream(data == null ? new byte[0] : data) {
			@Override
			public void close() throws IOException {
				data = toByteArray();
			}
		};
	}

	/**
	 * Gets the byte[] backing the MemoryFile.
	 *
	 * @return The byte[] this MemoryFile was created from.
	 */
	public final byte[] getBackingArray() {
		setFileModeSafe();
		return data;
	}

	@Override
	public byte[] getBytes() {
		setFileModeSafe();
		return data;
	}

	@Override
	public void setBytes(byte[] bytes) {
		if (bytes == null) {
			throw new NullPointerException("File data can not be null");
		}
		setFileModeSafe();
		data = bytes;
	}

	@Override
	public List<FSFile> listFiles() {
		return children;
	}

	@Override
	public boolean exists() {
		return exists;
	}

	@Override
	public FSFile getParent() {
		return parent;
	}

	@Override
	public void mkdir() {
		setDirModeSafe();
	}

	@Override
	public FSFile getChild(String forName) {
		if (forName == null || forName.isEmpty()) {
			return this;
		}
		setDirModeSafe();
		FSFile retval = FSUtil.getChildByListing(this, forName);
		if (retval == null) {
			MemoryFile child;
			String[] allName = StringEx.splitOnecharFastNoBlank(forName, '/');
			if (allName.length == 1) {
				child = new MemoryFile(forName, this);
				linkChild(child);
			} else {
				int i = 0;
				MemoryFile lastProtoParent = this;
				for (; i < allName.length - 1; i++) {
					FSFile elem = lastProtoParent.getChild(allName[i]);
					if (elem == null || !(elem instanceof MemoryFile)) {
						MemoryFile nonExists = new MemoryFile(allName[i], lastProtoParent);
						nonExists.exists = false;
						nonExists.isDirectory = true;
						lastProtoParent = nonExists;
					} else {
						lastProtoParent = (MemoryFile) elem;
					}
				}
				child = new MemoryFile(allName[i], lastProtoParent);
				lastProtoParent.linkChild(child);
			}
			child.exists = false;
			retval = child;
		}
		return retval;
	}

	@Override
	public int length() {
		return data == null ? 0 : data.length;
	}

	@Override
	public void delete() {
		if (parent != null && parent instanceof MemoryFile) {
			((MemoryFile) parent).children.remove(this);
			parent = null;
		}
	}

	@Override
	public int getChildCount() {
		return isDirectory ? children.size() : 0;
	}

	@Override
	public void setPath(String newPath) {
		name = FSUtil.getFileName(newPath);
	}

	@Override
	public int getPermissions() {
		return isDirectory ? 0 : FSF_ATT_READ | FSF_ATT_WRITE;
	}
}
