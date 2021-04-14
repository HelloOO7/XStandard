package ctrmap.stdlib.io;

import ctrmap.stdlib.io.base.LittleEndianIO;
import ctrmap.stdlib.io.iface.SeekableDataOutput;
import ctrmap.stdlib.io.iface.SeekableDataInput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class LittleEndianRandomAccessFile extends LittleEndianIO {

	private RAFWrapper raf_handle;

	public LittleEndianRandomAccessFile(String path) throws FileNotFoundException {
		this(new File(path));
	}

	public LittleEndianRandomAccessFile(File path) throws FileNotFoundException {
		this(new RAFWrapper(path, "rw"));
	}
	
	private LittleEndianRandomAccessFile(RAFWrapper raf){
		super(raf, raf, raf);
		raf_handle = raf;
	}

	public File getFile() {
		return raf_handle.getOriginFile();
	}

	@Override
	public int length() throws IOException {
		return (int) raf_handle.length();
	}

	public int getFilePointer() throws IOException {
		return (int) raf_handle.getFilePointer();
	}
	
	private static class RAFWrapper extends RandomAccessFile implements SeekableDataInput, SeekableDataOutput{

		private File origin;
		
		public RAFWrapper(File file, String mode) throws FileNotFoundException {
			super(file, mode);
			this.origin = file;
		}
		
		public File getOriginFile(){
			return origin;
		}

		@Override
		public void seek(int addr) throws IOException {
			super.seek(addr);
		}

		@Override
		public int getPosition() throws IOException {
			return (int)getFilePointer();
		}
	}
}
