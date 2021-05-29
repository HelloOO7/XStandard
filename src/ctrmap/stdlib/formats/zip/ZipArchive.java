/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ctrmap.stdlib.formats.zip;

import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.fs.accessors.DiskFile;
import ctrmap.stdlib.fs.accessors.FSFileAdapter;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class ZipArchive extends FSFileAdapter {

	public static final String MAGIC = "PK\u0004\u0003";
	
	protected List<ZipEntry> entries = new ArrayList<>();

	private ZipFile zf;

	private final boolean allowsDirectAccess;

	public ZipArchive(FSFile source) {
		super(source);
		allowsDirectAccess = source instanceof DiskFile;
		try {
			if (allowsDirectAccess) {
				zf = new ZipFile(((DiskFile) source).getFile());

				Enumeration<? extends ZipEntry> es = zf.entries();
				while (es.hasMoreElements()) {
					entries.add(es.nextElement());
				}
			} else {
				ZipInputStream in = new ZipInputStream(source.getNativeInputStream());
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
	
	public static boolean isZip(FSFile fsf){
		return FSUtil.checkFileMagic(fsf, MAGIC);
	}

	@Override
	public FSFile getChild(String forName) {
		return new ZipEntryFile(this, forName);
	}

	@Override
	public void mkdir() {
		//already is a directory
	}

	@Override
	public boolean isDirectory() {
		return true;
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
				ZipInputStream in = new ZipInputStream(source.getNativeInputStream());
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
}
