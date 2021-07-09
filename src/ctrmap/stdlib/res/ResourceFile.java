package ctrmap.stdlib.res;

import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.io.base.iface.IOStream;
import ctrmap.stdlib.io.base.iface.ReadableStream;
import ctrmap.stdlib.io.base.iface.WriteableStream;
import ctrmap.stdlib.io.base.impl.InputStreamReadable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstraction of ResourceInfo into a file system object.
 */
public class ResourceFile extends FSFile{

	private final ResourceTable.ResourceInfo info;
	
	/**
	 * Creates a ResourceFile linked with a ResourceInfo.
	 * @param i A valid ResourceInfo.
	 */
	public ResourceFile(ResourceTable.ResourceInfo i){
		if (i == null){
			throw new RuntimeException("ResourceInfo can not be null!");
		}
		info = i;
	}
	
	@Override
	public boolean isDirectory() {
		return info.isDirectory;
	}

	@Override
	public String getName() {
		return info.resourceName;
	}

	@Override
	public ReadableStream getInputStream() {
		if (isDirectory()){
			return null;
		}
		return new InputStreamReadable(ResourceAccess.getStream(info.getResourcePath()));
	}

	@Override
	public WriteableStream getOutputStream() {
		throw new UnsupportedOperationException("Can not output to a static resource file.");
	}

	@Override
	public IOStream getIO() {
		throw new UnsupportedOperationException("Can not output to a static resource file.");
	}

	@Override
	public List<FSFile> listFiles() {
		List<FSFile> l = new ArrayList<>();
		for (ResourceTable.ResourceInfo sub : info.listFiles()){
			l.add(new ResourceFile(sub));
		}
		return l;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public FSFile getChild(String forName) {
		String[] elems = forName.split("/");
		ResourceFile cur = this;
		for (int i = 0; i < elems.length; i++){
			if (cur == null){
				break;
			}
			List<ResourceTable.ResourceInfo> lf = cur.info.listFiles();
			boolean found = false;
			for (ResourceTable.ResourceInfo resInfo : lf){
				if (resInfo.resourceName.equals(elems[i])){
					cur = new ResourceFile(resInfo);
					found = true;
					break;
				}
			}
			if (!found){
				return null;
			}
		}
		return cur;
	}

	@Override
	public FSFile getParent() {
		ResourceTable.ResourceInfo parentInfo = info.getParent();
		if (parentInfo == null){
			return null;
		}
		return new ResourceFile(parentInfo);
	}

	@Override
	public void mkdir() {
		throw new UnsupportedOperationException("Can not modify static resource data.");
	}

	@Override
	public int length() {
		try {
			InputStream in = ResourceAccess.getStream(info.getResourcePath());
			int l = in.available();
			in.close();
			return l;
		} catch (IOException ex) {
			Logger.getLogger(ResourceFile.class.getName()).log(Level.SEVERE, null, ex);
		}
		return 0;
	}

	@Override
	public void delete() {
		throw new UnsupportedOperationException("Resource files are read-only.");
	}

	@Override
	public void setPath(String newPath) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public int getPermissions() {
		return FSF_ATT_READ;
	}

}
