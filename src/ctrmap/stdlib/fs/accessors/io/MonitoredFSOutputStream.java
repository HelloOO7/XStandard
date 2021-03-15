package ctrmap.stdlib.fs.accessors.io;

import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.fs.VFSFile;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

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
			System.out.println("is blacklist " + vfsf);
			System.out.println(vfsf.getBaseFile().getClass());
			/*FSUtil.writeBytesToFile(new File("D:/_REWorkspace/h3d_debug/" + vfsf.getName() + "_base"), vfsf.getBaseFile().getBytes());
			FSUtil.writeBytesToFile(new File("D:/_REWorkspace/h3d_debug/" + vfsf.getName() + "_ov"), vfsf.getOvFile().getBytes());*/
			if (!FSUtil.fileCmp(vfsf.getBaseFile(), vfsf.getOvFile())) {
				System.out.println("remove bl " + vfsf);
				System.out.println(vfsf.getBaseFile().length() + "/" + vfsf.getOvFile().length());
				vfsf.getVFS().notifyFileChange(vfsf.getPath());
			}
		}
	}
}
