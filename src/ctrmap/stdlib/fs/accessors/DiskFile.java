package ctrmap.stdlib.fs.accessors;

import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.io.base.iface.IOStream;
import ctrmap.stdlib.io.base.iface.ReadableStream;
import ctrmap.stdlib.io.base.iface.WriteableStream;
import ctrmap.stdlib.io.base.impl.InputStreamReadable;
import ctrmap.stdlib.io.base.impl.OutputStreamWriteable;
import ctrmap.stdlib.io.base.impl.access.FileStream;
import ctrmap.stdlib.io.base.impl.ext.BufferedIOStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A FSFile constructed from a real file-system File.
 */
public class DiskFile extends FSFile {

	private File file;

	/**
	 * Creates a DiskFile from a path in the file-system.
	 *
	 * @param path Relative or absolute path of the file.
	 */
	public DiskFile(String path) {
		this(new File(path));
	}

	/**
	 * Creates a DiskFile from a java.io.File object.
	 *
	 * @param f A File.
	 */
	public DiskFile(File f) {
		file = f;
	}

	@Override
	public String getName() {
		//If the file is at drive root, full path should be returned without the / at the end
		if (file.getAbsoluteFile().getParentFile() == null) {
			String pth = file.getAbsolutePath().replace('\\', '/');
			if (pth.lastIndexOf('/') != -1) {
				pth = pth.substring(0, pth.lastIndexOf('/'));
			}
			return pth;
		}
		return file.getName();
	}

	@Override
	public ReadableStream getInputStream() {
		try {
			return new InputStreamReadable(new BufferedInputStream(new FileInputStream(file), Math.max(4096, (int) file.length() / 1000)));
		} catch (FileNotFoundException ex) {
			Logger.getLogger(DiskFile.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	@Override
	public WriteableStream getOutputStream() {
		return new OutputStreamWriteable(getNativeOutputStream());
	}

	@Override
	public BufferedOutputStream getNativeOutputStream() {
		try {
			return new BufferedOutputStream(new FileOutputStream(file));
		} catch (FileNotFoundException ex) {
			Logger.getLogger(DiskFile.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	@Override
	public IOStream getIO() {
		return new BufferedIOStream(FileStream.create(file));
	}

	@Override
	public List<FSFile> listFiles() {
		File[] files = file.listFiles();
		List<FSFile> r = new ArrayList<>();
		if (files != null) {
			for (File f : files) {
				r.add(new DiskFile(f));
			}
		}
		return r;
	}

	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	@Override
	public boolean isFile() {
		return file.isFile();
	}

	public File getFile() {
		return file;
	}

	@Override
	public boolean exists() {
		return file.exists();
	}

	/*@Override
	public String getPath(){
		return file.getPath().replace('\\', '/');
	}*/
	@Override
	public DiskFile getParent() {
		File parent = file.getParentFile();
		if (parent != null) {
			return new DiskFile(file.getParentFile());
		} else {
			return null;
		}
	}

	@Override
	public void mkdir() {
		file.mkdir();
	}

	@Override
	public DiskFile getChild(String forName) {
		if (forName == null || forName.isEmpty()) {
			return this;
		}
		if (isFile()) {
			return null;
		}
		return new DiskFile(file.getPath() + "/" + forName);
	}

	@Override
	public int length() {
		return (int) file.length();
	}

	@Override
	public void delete() {
		if (isDirectory()) {
			for (FSFile child : listFiles()) {
				child.delete();
			}
		}
		file.delete();
	}

	@Override
	public void setPath(String targetPath) {
		if (exists()) {
			try {
				new File(targetPath).getParentFile().mkdirs();
				Files.move(file.toPath(), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ex) {
				Logger.getLogger(DiskFile.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public int getChildCount() {
		if (isDirectory()) {
			return file.list().length;
		}
		return 0;
	}

	@Override
	public int getPermissions() {
		int att = 0;
		att |= file.canRead() ? FSF_ATT_READ : 0;
		att |= file.canWrite() ? FSF_ATT_WRITE : 0;
		att |= file.canExecute() ? FSF_ATT_EXECUTE : 0;
		return att;
	}
}
