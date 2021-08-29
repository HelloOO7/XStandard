package ctrmap.stdlib.fs.accessors.io;

import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.fs.VFSFile;
import ctrmap.stdlib.io.base.impl.WriteableWrapper;
import java.io.IOException;

public class MonitoredFSOutputStream extends WriteableWrapper {

	private VFSFile vfsf;

	public MonitoredFSOutputStream(VFSFile vfsf) {
		super(null);
		if (vfsf.getOvFile().isDirectory()) {
			out = vfsf.getBaseFile().getOutputStream();
		} else {
			out = vfsf.getOvFile().getOutputStream();
		}
		this.vfsf = vfsf;

		if (!vfsf.getOvFile().exists()) {
			vfsf.getVFS().notifyOvFsNewFileInit(vfsf.getPath());
		}
	}

	@Override
	public void close() throws IOException {
		out.close();
		if (vfsf.getVFS().isFileChangeBlacklisted(vfsf.getPath())) {
			System.out.println("File " + vfsf + " is in the blacklist, checking for changes.");
			if (!FSUtil.fileCmp(vfsf.getBaseFile(), vfsf.getOvFile())) {
				System.out.println("File " + vfsf + " has changed. Removing from blacklist.");
				int baseLen = vfsf.getBaseFile().length();
				int ovLen = vfsf.getOvFile().length();
				if (baseLen != ovLen){
					System.out.println("(Length difference - BaseFS: " + baseLen + " / OvFS: " + ovLen + ")");
				}
				vfsf.getVFS().notifyFileChange(vfsf.getPath());
			}
		}
	}
}
