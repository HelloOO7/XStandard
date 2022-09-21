package xstandard.fs.accessors.io;

import xstandard.fs.FSUtil;
import xstandard.fs.VFSFile;
import xstandard.io.base.impl.IOStreamWrapper;
import java.io.IOException;

public class MonitoredFSIO extends IOStreamWrapper {

	private VFSFile vfsf;

	private boolean everWritten = false;

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
	public void write(int i) throws IOException {
		everWritten = true;
		super.write(i);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		everWritten = true;
		super.write(b, off, len);
	}

	@Override
	public void close() throws IOException {
		super.close();

		if (everWritten) {
			if (vfsf.getVFS().isFileChangeBlacklisted(vfsf.getPath())) {
				if (vfsf.getBaseFile() == null || !FSUtil.fileCmp(vfsf.getBaseFile(), vfsf.getOvFile())) {
					vfsf.getVFS().notifyFileChange(vfsf.getPath());
				}
			}
		}
	}
}
