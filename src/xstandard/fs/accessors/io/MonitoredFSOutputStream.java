package xstandard.fs.accessors.io;

import xstandard.fs.FSUtil;
import xstandard.fs.VFSFile;
import xstandard.io.base.impl.WriteableWrapper;
import java.io.IOException;

public class MonitoredFSOutputStream extends WriteableWrapper {

	private VFSFile vfsf;

	private boolean everWritten = false;

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
		out.close();

		if (everWritten) {
			if (vfsf.getVFS().isFileChangeBlacklisted(vfsf.getPath())) {
				System.out.println("File " + vfsf + " is in the blacklist, checking for changes.");
				if (vfsf.getBaseFile() == null || !FSUtil.fileCmp(vfsf.getBaseFile(), vfsf.getOvFile())) {
					System.out.println("File " + vfsf + " has changed. Removing from blacklist.");
					vfsf.getVFS().notifyFileChange(vfsf.getPath());
				}
			}
		}
	}
}
