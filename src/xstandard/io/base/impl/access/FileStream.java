package xstandard.io.base.impl.access;

import xstandard.io.base.iface.IOStream;

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
			e.printStackTrace();
			throw new RuntimeException("Could not find file " + file + "; FileStream can not be created!");
		}
	}

	@Override
	public synchronized int read() throws IOException {
		return super.read();
	}

	@Override
	public synchronized int read(byte[] b, int off, int len) throws IOException {
		return super.read(b, off, len);
	}

	@Override
	public synchronized int skipBytes(int amount) throws IOException {
		if (amount < 0) {
			super.seek(super.getFilePointer() + amount);
			return amount;
		} else {
			return super.skipBytes(amount);
		}
	}

	@Override
	public synchronized int getPosition() throws IOException {
		return (int) getFilePointer();
	}

	@Override
	public synchronized void seek(long position) throws IOException {
		super.seek(position);
	}

	@Override
	public synchronized int getLength() {
		//Using file.length() is about a ms slower
		try {
			return (int) length();
		} catch (IOException ex) {
			Logger.getLogger(FileStream.class.getName()).log(Level.SEVERE, null, ex);
		}
		return 0;
	}

	@Override
	public synchronized void setLength(long length) throws IOException {
		super.setLength(length);
	}
}
