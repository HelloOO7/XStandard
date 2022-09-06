package xstandard.fs.accessors;

import xstandard.fs.FSFile;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.iface.ReadableStream;
import xstandard.io.base.iface.WriteableStream;
import xstandard.io.base.impl.ext.SubInputStream;
import java.util.ArrayList;
import java.util.List;

public class InlineFile extends FSFile {

	private FSFile cont;
	private String name;
	
	private int off;
	private int endoff;
	
	public InlineFile(FSFile cont, String name, int startOffset, int endOffset) {
		this.cont = cont;
		this.name = name;
		this.off = startOffset;
		this.endoff = endOffset;
	}
	
	@Override
	public FSFile getChild(String forName) {
		return null;
	}

	@Override
	public FSFile getParent() {
		return cont;
	}

	@Override
	public void mkdir() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setPath(String newPath) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void delete() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int length() {
		return endoff - off;
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public boolean exists() {
		return cont.exists();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ReadableStream getInputStream() {
		return new SubInputStream(cont.getInputStream(), off, endoff);
	}

	@Override
	public WriteableStream getOutputStream() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public IOStream getIO() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public List<? extends FSFile> listFiles() {
		return new ArrayList<>();
	}

	@Override
	public int getPermissions() {
		return FSF_ATT_READ;
	}

}
