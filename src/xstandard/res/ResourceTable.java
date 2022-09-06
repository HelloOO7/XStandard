package xstandard.res;

import xstandard.io.base.iface.DataInputEx;
import xstandard.io.base.iface.DataOutputEx;
import xstandard.io.base.impl.ext.data.DataInStream;
import xstandard.io.base.impl.ext.data.DataOutStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A table describing the file tree of program resources.
 */
public class ResourceTable {

	private String rootClasspath;
	private List<ResourceInfo> data = new ArrayList<>();

	public ResourceTable(File root, String rootClasspath) {
		this(root, rootClasspath, null);
	}

	/**
	 * Builds a resource table from disk.
	 *
	 * @param root Root directory of the file tree.
	 * @param rootClasspath Classpath of the source folder for access.
	 * @param rootVirtualPathName An optional virtual root file name.
	 */
	public ResourceTable(File root, String rootClasspath, String rootVirtualPathName) {
		this.rootClasspath = rootClasspath;
		if (rootVirtualPathName != null) {
			ResourceInfo parentRootInfo = new ResourceInfo(rootVirtualPathName, this);
			data.add(parentRootInfo);
			addFile(root, 0, false);
		} else {
			addFile(root, -1, false);
		}
	}

	/**
	 * Reads a resource table from a stream.
	 * @param in InputStream of the table data.
	 */
	public ResourceTable(InputStream in) {
		try (DataInputEx dis = new DataInStream(in)) {
			rootClasspath = dis.readString();
			int size = dis.readInt();
			for (int i = 0; i < size; i++) {
				data.add(new ResourceInfo(dis, this));
			}
		} catch (IOException ex) {
			Logger.getLogger(ResourceTable.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Serializes the table into a File.
	 * @param target An existing File object.
	 */
	public void write(File target) {
		try (DataOutputEx dos = new DataOutStream(new FileOutputStream(target))) {
			dos.writeString(rootClasspath);
			dos.writeInt(data.size());

			for (ResourceInfo i : data) {
				i.write(dos);
			}
		} catch (IOException ex) {
			Logger.getLogger(ResourceTable.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Merges the entries of 'tbl' with this table.
	 * @param tbl Table to merge.
	 */
	public void merge(ResourceTable tbl) {
		data.addAll(tbl.data);
	}

	private void addFile(File f, int parentIdx, boolean addThis) {
		ResourceInfo i = new ResourceInfo(f, this);
		i.parentIdx = parentIdx;

		if (addThis) {
			data.add(i);
		}
		if (f.isDirectory()) {
			int newPIdx = addThis ? data.indexOf(i) : parentIdx;
			for (File sub : f.listFiles()) {
				addFile(sub, newPIdx, true);
			}
		}
	}

	/**
	 * Returns the resource descriptor of a pathname.
	 * @param path Path of the requested resource.
	 * @return The ResourceInfo for 'path', or null if not found.
	 */
	public ResourceInfo getResInfo(String path) {
		for (ResourceInfo i : data) {
			if (i.getResourcePath().equals(path)) {
				return i;
			}
		}
		return null;
	}

	/**
	 * A simple descriptor of a program resource.
	 */
	public static class ResourceInfo {

		public String resourceName;
		public boolean isDirectory;
		public int parentIdx;
		private ResourceTable table;

		/**
		 * Constructs a ResourceInfo with attributes inherited from a disk file.
		 * @param f The File to inherit attributes from.
		 * @param table Parent table.
		 */
		public ResourceInfo(File f, ResourceTable table) {
			resourceName = f.getName();
			isDirectory = f.isDirectory();
			parentIdx = -1;
			this.table = table;
		}

		/**
		 * Creates a virtual directory ResourceInfo with a specific name.
		 * @param directoryName Name of the virtual directory.
		 * @param table Parent table.
		 */
		public ResourceInfo(String directoryName, ResourceTable table) {
			resourceName = directoryName;
			isDirectory = true;
			parentIdx = -1;
			this.table = table;
		}

		private ResourceInfo(DataInputEx in, ResourceTable table) throws IOException {
			this.table = table;
			resourceName = in.readString();
			isDirectory = in.readBoolean();
			parentIdx = in.readInt();
		}

		private void write(DataOutputEx dos) throws IOException {
			dos.writeString(resourceName);
			dos.writeBoolean(isDirectory);
			dos.writeInt(parentIdx);
		}

		/**
		 * Gets the ResourceInfo corresponding to the parent directory of the described file.
		 * @return ResourceInfo of the parent, or null if this is the root directory.
		 */
		public ResourceInfo getParent() {
			if (parentIdx == -1) {
				return null;
			}
			return table.data.get(parentIdx);
		}

		/**
		 * Gets the full path of the underlying resource.
		 * @return Path of the resource, separated by '/'.
		 */
		public String getResourcePath() {
			if (parentIdx != -1) {
				return getParent().getResourcePath() + "/" + resourceName;
			}
			return resourceName;
		}
		
		public String getClasspath() {
			return table.rootClasspath + "/" + getResourcePath();
		}

		/**
		 * Lists the ResourceInfo descriptors of all child resources.
		 * @return A List containing the descriptors of all children, or an empty List if this is not a directory.
		 */
		public List<ResourceInfo> listFiles() {
			int idx = table.data.indexOf(this);
			List<ResourceInfo> ret = new ArrayList<>();
			for (ResourceInfo i : table.data) {
				if (i.parentIdx == idx) {
					ret.add(i);
				}
			}
			return ret;
		}
	}
}
