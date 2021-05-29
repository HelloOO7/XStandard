package ctrmap.stdlib.io.base.impl.access;

import ctrmap.stdlib.io.base.iface.IOStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileStream extends RandomAccessFile implements IOStream {

	private File file;

	public FileStream(File file) throws FileNotFoundException {
		super(file, "rw");

		this.file = file;
	}

	public static FileStream create(File file) {
		try {
			return new FileStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Could not find file " + file + "; FileStream can not be created!");
		}
	}

	@Override
	public int getPosition() throws IOException {
		return (int) getFilePointer();
	}

	@Override
	public void seek(int position) throws IOException {
		super.seek(position);
	}

	@Override
	public int getLength() {
		//Using file.length() is about a ms slower
		try {
			return (int) length();
		} catch (IOException ex) {
			Logger.getLogger(FileStream.class.getName()).log(Level.SEVERE, null, ex);
		}
		return 0;
	}
}
