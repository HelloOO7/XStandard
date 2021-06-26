package ctrmap.stdlib.fs.accessors.io;

import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.fs.VFSFile;
import ctrmap.stdlib.fs.accessors.DiskFile;
import ctrmap.stdlib.io.base.impl.IOStreamWrapper;
import java.io.IOException;

public class MonitoredFSIO extends IOStreamWrapper {

	private VFSFile vfsf;

	public MonitoredFSIO(VFSFile vfsf) {
		super(null);
		if (vfsf.getOvFile().isDirectory()) {
			io = vfsf.getBaseFile().getIO();
		} else {
			io = vfsf.getOvFile().getIO();
		}
		this.vfsf = vfsf;

		if (!vfsf.getOvFile().exists()) {
			vfsf.getVFS().notifyOvFsNewFileInit(vfsf.getPath());
		}
	}

	@Override
	public void close() throws IOException {
		super.close();

		if (vfsf.getVFS().isFileChangeBlacklisted(vfsf.getPath())) {
			if (!FSUtil.fileCmp(vfsf.getBaseFile(), vfsf.getOvFile())) {
				System.out.println("file change " + vfsf.getPath());
				FSUtil.writeBytesToFile(new DiskFile("D:\\_REWorkspace\\CTRMapProjects\\White2\\vfs\\data\\a\\0\\6\\base.bin"), vfsf.getBaseFile().getBytes());
				FSUtil.writeBytesToFile(new DiskFile("D:\\_REWorkspace\\CTRMapProjects\\White2\\vfs\\data\\a\\0\\6\\ov.bin"), vfsf.getOvFile().getBytes());
				vfsf.getVFS().notifyFileChange(vfsf.getPath());
			}
		}
	}
}
