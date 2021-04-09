package ctrmap.stdlib.fs.accessors.io;

import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.fs.VFSFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MonitoredFSOutputStream extends OutputStream {

	private OutputStream os;

	private VFSFile vfsf;

	public MonitoredFSOutputStream(VFSFile vfsf) {
		if (vfsf.getOvFile().isDirectory()) {
			os = vfsf.getBaseFile().getOutputStream();
		} else {
			os = vfsf.getOvFile().getOutputStream();
		}
		this.vfsf = vfsf;

		if (!vfsf.getOvFile().exists()) {
			vfsf.getVFS().notifyOvFsNewFileInit(vfsf.getPath());
		}
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		os.write(b, off, len);
	}

	@Override
	public void write(int b) throws IOException {
		os.write(b);
	}

	@Override
	public void close() throws IOException {
		os.close();
		if (vfsf.getVFS().isFileChangeBlacklisted(vfsf.getPath())) {
			System.out.println("File " + vfsf + " is in the blacklist, checking for changes.");
			if (!FSUtil.fileCmp(vfsf.getBaseFile(), vfsf.getOvFile())) {
				System.out.println("File " + vfsf + " has changed. Removing from blacklist.");
				int baseLen = vfsf.getBaseFile().length();
				int ovLen = vfsf.getOvFile().length();
				if (baseLen != ovLen){
					System.out.println("(length difference - BaseFS: " + baseLen + " / OvFS: " + ovLen + ")");
				}
				vfsf.getVFS().notifyFileChange(vfsf.getPath());
			}
		}
	}
}
