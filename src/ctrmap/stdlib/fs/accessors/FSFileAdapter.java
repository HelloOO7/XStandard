/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ctrmap.stdlib.fs.accessors;

import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.io.base.LittleEndianIO;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 *
 */
public class FSFileAdapter extends FSFile {
	
	protected FSFile source;
	
	public FSFileAdapter(FSFile toWrap){
		source = toWrap;
	}

	@Override
	public FSFile getChild(String forName) {
		return source.getChild(forName);
	}

	@Override
	public FSFile getParent() {
		return source.getParent();
	}

	@Override
	public void mkdir() {
		source.mkdir();
	}

	@Override
	public void setPath(String newPath) {
		source.setPath(newPath);
	}

	@Override
	public void delete() {
		source.delete();
	}

	@Override
	public int length() {
		return source.length();
	}

	@Override
	public boolean isDirectory() {
		return source.isDirectory();
	}

	@Override
	public boolean exists() {
		return source.exists();
	}

	@Override
	public String getName() {
		return source.getName();
	}

	@Override
	public InputStream getInputStream() {
		return source.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() {
		return source.getOutputStream();
	}

	@Override
	public LittleEndianIO getIO() {
		return source.getIO();
	}

	@Override
	public List<FSFile> listFiles() {
		return source.listFiles();
	}

	@Override
	public int getChildCount(boolean includeInvisible) {
		return source.getChildCount(includeInvisible);
	}

}
