package ctrmap.stdlib.fs;

import ctrmap.stdlib.fs.accessors.arc.ArcFileAccessor;
import ctrmap.stdlib.fs.accessors.io.MonitoredFSIO;
import ctrmap.stdlib.fs.accessors.io.MonitoredFSOutputStream;
import ctrmap.stdlib.io.base.iface.IOStream;
import ctrmap.stdlib.io.base.iface.ReadableStream;
import ctrmap.stdlib.io.base.iface.WriteableStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class VFSFile extends FSFile {

	private VFS fs;
	private ArcFileAccessor afa;
	private FSFile rootfs;
	private FSFile ovfs;

	private String path;
	private FSFile baseFile;
	private FSFile ovFile;

	public VFSFile(String path, VFS fs, ArcFileAccessor afa) {
		this.fs = fs;
		this.path = FSWildCard.getWildCardedPath(path);
		this.rootfs = fs.getBaseFSRoot();
		this.ovfs = fs.getOvFSRoot();
		this.afa = afa;
		baseFile = VFS.getFileFromRefPath(rootfs, path, afa);
		ovFile = VFS.getFileFromRefPath(ovfs, path, afa);
	}

	public VFSFile(String path, VFS fs, FSFile baseFile, ArcFileAccessor afa) {
		this.fs = fs;
		this.path = FSWildCard.getWildCardedPath(path);
		this.rootfs = fs.getBaseFSRoot();
		this.ovfs = fs.getOvFSRoot();
		this.baseFile = baseFile;
		this.afa = afa;
		ovFile = VFS.getFileFromRefPath(ovfs, path, afa);
	}

	public FSFile getBaseFile() {
		return baseFile;
	}

	public FSFile getOvFile() {
		return ovFile;
	}

	public VFS getVFS() {
		return fs;
	}

	public FSFile getExistingFile() {
		if (ovFile.exists() && !ovFile.isDirectory()) {
			return ovFile;
		}
		return baseFile;
	}

	@Override
	public FSFile getChild(String forName) {
		return new VFSFile(path + "/" + forName, fs, afa);
	}

	private static String createParentPath(String path) {
		int slashIdx = path.lastIndexOf("/");
		if (slashIdx == -1) {
			return "";
		}
		return path.substring(0, slashIdx);
	}

	@Override
	public FSFile getParent() {
		return new VFSFile(createParentPath(path), fs, afa);
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public void mkdir() {
		ovFile.mkdir();
	}

	@Override
	public void delete() {
		ovFile.delete();
	}

	@Override
	public int length() {
		return getExistingFile().length();
	}

	@Override
	public boolean isDirectory() {
		return getExistingFile().isDirectory();
	}

	@Override
	public boolean exists() {
		return ovFile.exists() || baseFile.exists();
	}

	@Override
	public String getName() {
		return getExistingFile().getName();
	}

	@Override
	public ReadableStream getInputStream() {
		return getExistingFile().getInputStream();
	}

	@Override
	public WriteableStream getOutputStream() {
		ensureOvParentExists();
		return new MonitoredFSOutputStream(this);
	}

	private void ensureOvParentExists() {
		if (!ovFile.getParent().exists()) {
			ovFile.getParent().mkdirs();
		}
	}

	@Override
	public IOStream getIO() {
		if (!ovFile.exists()) {
			ensureOvParentExists();
			FSUtil.copy(baseFile, ovFile);
			fs.notifyOvFsNewFileInit(getPath());
		}
		return new MonitoredFSIO(this);
	}

	@Override
	public List<FSFile> listFiles() {
		List<FSFile> ovChildren = ovFile.listFiles();
		List<FSFile> baseChildren = baseFile.listFiles();

		List<FSFile> vfsChildren = new ArrayList<>();

		for (FSFile base : baseChildren) {
			//doesn't HAVE to exist
			vfsChildren.add(new VFSFile(path + "/" + base.getName(), fs, base, afa));
		}

		for (FSFile ov : ovChildren) {
			if (findByName(vfsChildren, ov.getName()) == null) {
				vfsChildren.add(new VFSFile(path + "/" + ov.getName(), fs, afa));
				//The BaseFS child will not exist, but this is not a problem since VFSFile always prefers the topmost existing layer
			}
		}

		return vfsChildren;
	}

	private static FSFile findByName(List<FSFile> l, String name) {
		for (FSFile f : l) {
			if (f.getName().equals(name)) {
				return f;
			}
		}
		return null;
	}
	
	private int vfsGetChildCountImpl(boolean includeHidden){
		List<FSFile> ovChildren = ovFile.listFiles();
		List<FSFile> baseChildren = baseFile.listFiles();

		for (FSFile ov : ovChildren) {
			if (findByName(baseChildren, ov.getName()) == null) {
				baseChildren.add(ov);
			}
		}
		
		return baseChildren.size() - (includeHidden ? 0 : getHiddenCount(baseChildren));
	}

	@Override
	public int getChildCount() {
		return vfsGetChildCountImpl(true);
	}
	
	@Override
	public int getVisibleChildCount(){
		return vfsGetChildCountImpl(false);
	}

	@Override
	public void setPath(String newPath) {
		newPath = FSWildCard.getWildCardedPath(newPath);
		
		baseFile.setPath(FSWildCard.getNonWildCardedPathByActual(rootfs + "/" + newPath, baseFile.getPath()));
		ovFile.setPath(FSWildCard.getNonWildCardedPathByActual(ovfs + "/" + newPath, ovFile.getPath()));
		
		fs.relocateBlackListFile(path, newPath);
		path = newPath;
	}

	@Override
	public int getPermissions() {
		return getExistingFile().getPermissions();
	}
}
