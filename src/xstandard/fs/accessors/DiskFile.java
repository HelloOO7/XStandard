package xstandard.fs.accessors;

import xstandard.fs.FSFile;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.iface.ReadableStream;
import xstandard.io.base.iface.WriteableStream;
import xstandard.io.base.impl.InputStreamReadable;
import xstandard.io.base.impl.OutputStreamWriteable;
import xstandard.io.base.impl.access.FileStream;
import xstandard.io.base.impl.ext.BufferedIOStream;
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
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A FSFile constructed from a real file-system File.
 */
public class DiskFile extends FSFile {

	private File file;
	private File canonicalFile;

	private final CachedBool exists = new CachedBool();
	private final CachedBool isDirectory = new CachedBool();
	private final CachedBool isFile = new CachedBool();

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
		file = f.getAbsoluteFile();
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
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
			exists.set(true);
			isDirectory.set(false);
			isFile.set(true);
			return out;
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
		if (isDirectory.exists) {
			return isDirectory.value; //0 native calls
		}
		if (exists.exists && isFile.exists) {
			isDirectory.set(exists() && !isFile()); //0 native calls
		}
		if (!isDirectory.exists) {
			isDirectory.set(file.isDirectory()); //1 native call
		}
		if (isDirectory.value) {
			exists.set(true);
		}
		return isDirectory.value;
	}

	@Override
	public boolean isFile() {
		if (isFile.exists) {
			return isFile.value;
		}
		if (exists.exists && isDirectory.exists) {
			isFile.set(exists() && !isDirectory());
		}
		if (!isFile.exists) {
			isFile.set(file.isFile());
		}
		if (isFile.value) {
			exists.set(true);
		}
		return isFile.value;
	}

	public File getFile() {
		return file;
	}

	@Override
	public boolean exists() {
		if (!exists.exists) {
			exists.set(file.exists());
		}
		return exists.value;
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
		isDirectory.set(true);
		isFile.set(false);
		exists.set(true);
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
		exists.set(false);
		isDirectory.set(false);
		isFile.set(false);
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

	private File getEnsureCanonicalFile() {
		if (canonicalFile == null) {
			try {
				canonicalFile = file.getCanonicalFile();
			} catch (IOException ex) {
			}
		}
		return canonicalFile;
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof DiskFile) {
			return Objects.equals(getEnsureCanonicalFile(), ((DiskFile) o).getEnsureCanonicalFile());
		}
		return false;
	}

	@Override
	public int hashCode() {
		File canonFile = getEnsureCanonicalFile();
		if (canonFile != null) {
			return canonFile.hashCode();
		}
		return file.hashCode();
	}

	@Override
	public int getPermissions() {
		int att = 0;
		att |= file.canRead() ? FSF_ATT_READ : 0;
		att |= file.canWrite() ? FSF_ATT_WRITE : 0;
		if (!exists()) {
			att |= FSF_ATT_WRITE; //nonexistent files can be written to as well
		}
		att |= file.canExecute() ? FSF_ATT_EXECUTE : 0;
		return att;
	}

	private static class CachedBool {

		public boolean exists = false;
		public boolean value = false;

		public void set(boolean val) {
			exists = true;
			value = val;
		}
	}
}
