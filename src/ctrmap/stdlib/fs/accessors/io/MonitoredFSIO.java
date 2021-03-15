package ctrmap.stdlib.fs.accessors.io;

import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.fs.VFSFile;
import ctrmap.stdlib.io.base.LittleEndianIO;
import java.io.File;
import java.io.IOException;

public class MonitoredFSIO extends LittleEndianIO {

	private LittleEndianIO io;

	private VFSFile vfsf;

	public MonitoredFSIO(VFSFile vfsf) {
		if (vfsf.getOvFile().isDirectory()) {
			io = vfsf.getBaseFile().getIO();
		} else {
			io = vfsf.getOvFile().getIO();
		}
		this.vfsf = vfsf;
		io.mirrorTo(this);

		if (!vfsf.getOvFile().exists()) {
			vfsf.getVFS().notifyOvFsNewFileInit(vfsf.getPath());
		}
	}

	@Override
	public void close() throws IOException {
		super.close();

		if (vfsf.getVFS().isFileChangeBlacklisted(vfsf.getPath())) {
			if (!FSUtil.fileCmp(vfsf.getBaseFile(), vfsf.getOvFile())) {
				vfsf.getVFS().notifyFileChange(vfsf.getPath());
			}
		}
	}

	@Override
	public int length() throws IOException {
		return io.length();
	}
}
