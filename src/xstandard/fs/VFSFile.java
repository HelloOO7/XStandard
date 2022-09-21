package xstandard.fs;

import xstandard.fs.accessors.io.MonitoredFSIO;
import xstandard.fs.accessors.io.MonitoredFSOutputStream;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.iface.ReadableStream;
import xstandard.io.base.iface.WriteableStream;
import java.util.ArrayList;
import java.util.List;

public class VFSFile extends FSFile {

	private VFS fs;

	private FSFile rootfs;
	private FSFile ovfs;

	private String path;
	private FSFile baseFile;
	private FSFile ovFile;

	private void init(String path, VFS fs) {
		this.fs = fs;
		this.path = fs.wildCards.getWildCardedPath(path);
		this.rootfs = fs.getBaseFSRoot();
		this.ovfs = fs.getOvFSRoot();
	}
	
	public VFSFile(String path, VFS fs) {
		init(path, fs);
		baseFile = fs.seekFile(rootfs, path);
		ovFile = fs.seekFile(ovfs, path);
	}

	public VFSFile(String path, VFS fs, FSFile baseFile) {
		init(path, fs);
		this.baseFile = baseFile;
		this.ovFile = fs.seekFile(ovfs, path);
	}
	
	public VFSFile(String path, VFS fs, FSFile baseFile, FSFile ovFile) {
		if (path.contains("..")) {
			throw new RuntimeException("Relative paths are forbidden in VFS.");
		}
		init(path, fs);
		this.baseFile = baseFile;
		this.ovFile = ovFile;
	}

	/**
	 * Gets the file linked to this VFSFile in the BaseFS.
	 *
	 * @return
	 */
	public FSFile getBaseFile() {
		return baseFile;
	}

	/**
	 * Gets the file linked to this VFSFile in the OvFS.
	 *
	 * @return
	 */
	public FSFile getOvFile() {
		return ovFile;
	}

	/**
	 * Gets the VFS this file was created with.
	 *
	 * @return
	 */
	public VFS getVFS() {
		return fs;
	}

	/**
	 * Gets either the linked OvFS or BaseFS file, depending on whichever
	 * exists. The OvFS file takes priority.
	 *
	 * @return
	 */
	public FSFile getExistingFile() {
		if (ovFile.exists() && !ovFile.isDirectory()) {
			return ovFile;
		}
		if (baseFile == null){
			return ovFile;
		}
		return baseFile;
	}

	@Override
	public FSFile getChild(String forName) {
		return new VFSFile(path + "/" + forName, fs);
	}

	@Override
	public FSFile getParent() {
		return new VFSFile(FSUtil.getParentFilePath(path), fs);
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
		return ovFile.exists() || (baseFile != null && baseFile.exists());
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
		List<? extends FSFile> ovChildren = ovFile.listFiles();
		List<? extends FSFile> baseChildren = baseFile.listFiles();

		List<FSFile> vfsChildren = new ArrayList<>();

		for (FSFile base : baseChildren) {
			//doesn't HAVE to exist
			vfsChildren.add(new VFSFile(path + "/" + base.getName(), fs, base));
		}

		for (FSFile ov : ovChildren) {
			if (findByName(vfsChildren, ov.getName()) == null) {
				vfsChildren.add(new VFSFile(path + "/" + ov.getName(), fs));
				//The BaseFS child will not exist, but this is not a problem since VFSFile always prefers the topmost existing layer
			}
		}

		return vfsChildren;
	}

	private static FSFile findByName(List<? extends FSFile> l, String name) {
		for (FSFile f : l) {
			if (f.getName().equals(name)) {
				return f;
			}
		}
		return null;
	}

	private int vfsGetChildCountImpl(boolean includeHidden) {
		List<? extends FSFile> ovChildren = ovFile.listFiles();
		List<? extends FSFile> baseChildren = baseFile.listFiles();

		List<FSFile> result = new ArrayList<>(baseChildren);

		for (FSFile ov : ovChildren) {
			if (findByName(baseChildren, ov.getName()) == null) {
				result.add(ov);
			}
		}

		return result.size() - (includeHidden ? 0 : getHiddenCount(result));
	}

	@Override
	public int getChildCount() {
		return vfsGetChildCountImpl(true);
	}

	@Override
	public int getVisibleChildCount() {
		return vfsGetChildCountImpl(false);
	}

	@Override
	public void setPath(String newPath) {
		FSWildCardManager wcm = fs.wildCards;

		newPath = wcm.getWildCardedPath(newPath);

		baseFile.setPath(wcm.getNonWildCardedPathByActual(rootfs + "/" + newPath, baseFile.getPath()));
		ovFile.setPath(wcm.getNonWildCardedPathByActual(ovfs + "/" + newPath, ovFile.getPath()));

		fs.relocateBlackListFile(path, newPath);
		path = newPath;
	}

	@Override
	public int getPermissions() {
		return getExistingFile().getPermissions();
	}
}
