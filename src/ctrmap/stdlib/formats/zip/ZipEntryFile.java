/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ctrmap.stdlib.formats.zip;

import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.io.base.iface.IOStream;
import ctrmap.stdlib.io.base.iface.ReadableStream;
import ctrmap.stdlib.io.base.iface.WriteableStream;
import ctrmap.stdlib.io.base.impl.InputStreamReadable;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;

/**
 *
 */
public class ZipEntryFile extends FSFile {

	private ZipArchive arc;
	private ZipEntry e;
	private String path;
	
	public ZipEntryFile(ZipArchive arc, String path){
		this.arc = arc;
		this.path = path;
		this.e = arc.getEntryForPath(path);
	}
	
	@Override
	public FSFile getChild(String forName) {
		return arc.getChild(path + "/" + forName);
	}

	@Override
	public FSFile getParent() {
		return arc.getChild(FSUtil.getParentFileName(path));
	}
	
	@Override
	public String getPath(){
		return arc.getPath() + "/" + path;
	}

	@Override
	public void mkdir() {
		//directories in ZIP should be automatic
	}

	@Override
	public void setPath(String newPath) {
		throw new UnsupportedOperationException("Zip archives are not yet editable.");
	}

	@Override
	public void delete() {
		throw new UnsupportedOperationException("Zip archives are not yet editable.");
	}

	@Override
	public int length() {
		return exists() ? (int)e.getSize() : 0;
	}

	@Override
	public boolean isDirectory() {
		return exists() && e.isDirectory();
	}

	@Override
	public boolean exists() {
		return e != null;
	}

	@Override
	public String getName() {
		return FSUtil.getFileName(path);
	}

	@Override
	public ReadableStream getInputStream() {
		return new InputStreamReadable(arc.getEntryInputStream(e));
	}

	@Override
	public WriteableStream getOutputStream() {
		throw new UnsupportedOperationException("Zip archives are not yet editable.");
	}

	@Override
	public IOStream getIO() {
		throw new UnsupportedOperationException("Zip archives are not yet editable.");
	}

	@Override
	public List<FSFile> listFiles() {
		return arc.listFilesByParentPath(path);
	}

	@Override
	public int getPermissions() {
		return FSF_ATT_READ;
	}
}
