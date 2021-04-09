/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ctrmap.stdlib.formats.zip;

import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.fs.accessors.DiskFile;
import ctrmap.stdlib.io.base.LittleEndianIO;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 *
 */
public class ZipArchive extends FSFile {

	private FSFile source;

	protected List<ZipEntry> entries = new ArrayList<>();

	private ZipFile zf;

	private final boolean allowsDirectAccess;

	public ZipArchive(FSFile source) {
		this.source = source;
		allowsDirectAccess = source instanceof DiskFile;
		try {
			if (allowsDirectAccess) {
				zf = new ZipFile(((DiskFile) source).getFile());

				Enumeration<? extends ZipEntry> es = zf.entries();
				while (es.hasMoreElements()) {
					entries.add(es.nextElement());
				}
			} else {
				ZipInputStream in = new ZipInputStream(source.getInputStream());
				ZipEntry e;
				while ((e = in.getNextEntry()) != null) {
					entries.add(e);
				}
				in.close();
			}
		} catch (IOException ex) {
			Logger.getLogger(ZipArchive.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public FSFile getChild(String forName) {
		return new ZipEntryFile(this, forName);
	}

	@Override
	public FSFile getParent() {
		return source.getParent();
	}

	@Override
	public void mkdir() {
		//already is a directory
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
		return true;
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
		return listFilesByParentPath(null);
	}

	public List<FSFile> listFilesByParentPath(String parentPath) {
		List<FSFile> l = new ArrayList<>();
		for (ZipEntry e : entries) {
			if (Objects.equals(FSUtil.getParentFileName(e.getName()), parentPath)) {
				l.add(new ZipEntryFile(this, e.getName()));
			}
		}
		return l;
	}

	public ZipEntry getEntryForPath(String path) {
		for (ZipEntry e : entries) {
			if (e.getName().equals(path)) {
				return e;
			}
		}
		return null;
	}

	public InputStream getEntryInputStream(ZipEntry entry) {
		if (entry == null){
			return null;
		}
		try {
			if (zf != null) {
				return zf.getInputStream(entry);

			} else {
				//VERY SLOW. Do not use unless really needed
				ZipInputStream in = new ZipInputStream(source.getInputStream());
				ZipEntry e;
				while ((e = in.getNextEntry()) != null) {
					if (Objects.equals(e.getName(), entry.getName())){
						return new BufferedInputStream(in);
					}
				}
				in.close();
			}
		} catch (IOException ex) {
			Logger.getLogger(ZipArchive.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	@Override
	public int getChildCount(boolean includeInvisible) {
		List<FSFile> children = listFiles();
		return children.size() - getHiddenCount(children);
	}
}
