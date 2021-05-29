package ctrmap.stdlib.res;

import ctrmap.stdlib.io.base.iface.DataInputEx;
import ctrmap.stdlib.io.base.iface.DataOutputEx;
import ctrmap.stdlib.io.base.impl.ext.data.DataInStream;
import ctrmap.stdlib.io.base.impl.ext.data.DataOutStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResourceTable {

	private List<ResourceInfo> data = new ArrayList<>();

	public ResourceTable(File root) {
		addFile(root, -1, false);
	}

	public ResourceTable() {

	}

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
			int newPIdx = data.indexOf(i);
			for (File sub : f.listFiles()) {
				addFile(sub, newPIdx, true);
			}
		}
	}

	public ResourceTable(InputStream in) {
		try (DataInputEx dis = new DataInStream(in)) {
			int size = dis.readInt();
			for (int i = 0; i < size; i++) {
				data.add(new ResourceInfo(dis, this));
			}
		} catch (IOException ex) {
			Logger.getLogger(ResourceTable.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void write(File target) {
		try (DataOutputEx dos = new DataOutStream(new FileOutputStream(target))) {
			dos.writeInt(data.size());

			for (ResourceInfo i : data) {
				i.write(dos);
			}
		} catch (IOException ex) {
			Logger.getLogger(ResourceTable.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public ResourceInfo getResInfo(String path) {
		for (ResourceInfo i : data) {
			if (i.getResourcePath().equals(path)) {
				return i;
			}
		}
		return null;
	}

	public static class ResourceInfo {

		public String resourceName;
		public boolean isDirectory;
		public int parentIdx;
		private ResourceTable table;

		public ResourceInfo(File f, ResourceTable table) {
			resourceName = f.getName();
			isDirectory = f.isDirectory();
			parentIdx = -1;
			this.table = table;
		}

		public ResourceInfo(DataInputEx in, ResourceTable table) throws IOException {
			this.table = table;
			resourceName = in.readString();
			isDirectory = in.readBoolean();
			parentIdx = in.readInt();
		}

		public void write(DataOutputEx dos) throws IOException {
			dos.writeString(resourceName);
			dos.writeBoolean(isDirectory);
			dos.writeInt(parentIdx);
		}

		public ResourceInfo getParent() {
			if (parentIdx == -1) {
				return null;
			}
			return table.data.get(parentIdx);
		}

		public String getResourcePath() {
			if (parentIdx != -1) {
				return getParent().getResourcePath() + "/" + resourceName;
			}
			return resourceName;
		}

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
