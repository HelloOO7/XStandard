package ctrmap.stdlib.fs.accessors;

import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.io.MemoryStream;
import ctrmap.stdlib.io.base.LittleEndianIO;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MemoryFile extends FSFile{

	private byte[] data;
	private String name;
	
	public MemoryFile(String name, byte[] data){
		this.data = data;
		this.name = name;
	}
	
	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(data);
	}

	@Override
	public OutputStream getOutputStream() {
		return new MemoryStream.RandomAccessBAOS(null, new MemoryStream.RandomAccessBAOS.CloseListener() {
			@Override
			public void onClose(byte[] buf) {
				data = buf;
			}
		});
	}

	@Override
	public LittleEndianIO getIO() {
		return new MemoryStream(data);
	}
	
	public byte[] getBackingArray(){
		return data;
	}

	@Override
	public List<FSFile> listFiles() {
		return new ArrayList<>();
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public FSFile getParent() {
		return null;
	}

	@Override
	public void mkdir() {
		throw new UnsupportedOperationException("Can't create a directory from a MemoryFile.");
	}

	@Override
	public FSFile getChild(String forName) {
		throw new UnsupportedOperationException("Memory files can not have children.");
	}

	@Override
	public int length() {
		return data.length;
	}

	@Override
	public void delete() {
		throw new UnsupportedOperationException("Can not delete an in-memory file.");
	}

	@Override
	public int getChildCount() {
		return 0;
	}

	@Override
	public void setPath(String newPath) {
		name = FSUtil.getFileName(newPath);
	}

	@Override
	public int getPermissions() {
		return FSF_ATT_READ;
	}
}
