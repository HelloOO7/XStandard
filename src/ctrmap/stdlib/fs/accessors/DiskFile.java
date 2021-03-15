package ctrmap.stdlib.fs.accessors;

import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.io.LittleEndianRandomAccessFile;
import ctrmap.stdlib.io.base.LittleEndianIO;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiskFile extends FSFile {

	private File file;

	public DiskFile(String path) {
		this(new File(path));
	}

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
	public InputStream getInputStream() {
		try {
			return new BufferedInputStream(new FileInputStream(file), Math.max(4096, (int) file.length() / 1000));
		} catch (FileNotFoundException ex) {
			Logger.getLogger(DiskFile.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	@Override
	public OutputStream getOutputStream() {
		try {
			return new FileOutputStream(file);
		} catch (FileNotFoundException ex) {
			Logger.getLogger(DiskFile.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	@Override
	public LittleEndianIO getIO() {
		try {
			return new LittleEndianRandomAccessFile(file);
		} catch (FileNotFoundException ex) {
			Logger.getLogger(DiskFile.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
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
	public FSFile getParent() {
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
	public FSFile getChild(String forName) {
		if (exists() && !isDirectory()) {
			return null;
		}
		if (forName == null || forName.length() == 0){
			return this;
		}
		return new DiskFile(file.getPath() + "/" + forName);
	}

	@Override
	public int length() {
		return (int) file.length();
	}

	@Override
	public void delete() {
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
	public int getChildCount(boolean includeHidden) {
		if (isDirectory()) {
			String[] l = file.list();
			int hidden = 0;
			if (!includeHidden) {
				for (String s : l) {
					if (s.startsWith(".")) {
						hidden++;
					}
				}
			}
			return file.list().length - hidden;
		}
		return 0;
	}
}
