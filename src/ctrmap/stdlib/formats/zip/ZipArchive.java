package ctrmap.stdlib.formats.zip;

import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.fs.accessors.DiskFile;
import ctrmap.stdlib.fs.accessors.FSFileAdapter;
import ctrmap.stdlib.io.base.iface.ReadableStream;
import ctrmap.stdlib.io.base.iface.WriteableStream;
import ctrmap.stdlib.io.base.impl.InputStreamReadable;
import ctrmap.stdlib.io.base.impl.OutputStreamWriteable;
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
import java.util.zip.ZipOutputStream;

/**
 *
 */
public class ZipArchive extends FSFileAdapter {

	public static final String MAGIC = "PK\u0003\u0004";

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

	public static ZipArchive pack(FSFile srcDir, FSFile dest) {
		try {
			ZipOutputStream out = new ZipOutputStream(dest.getNativeOutputStream());

			packDir(out, srcDir, null);

			out.close();

			return new ZipArchive(dest);
		} catch (IOException ex) {
			Logger.getLogger(ZipArchive.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	private static String appendPath(String parent, String elem) {
		if (parent == null || parent.isEmpty()) {
			return elem;
		}
		return parent + "/" + elem;
	}

	private static void packFile(ZipOutputStream out, FSFile file, String parentPath) throws IOException {
		//System.out.println("Packing file " + file);
		ZipEntry entry = new ZipEntry(appendPath(parentPath, file.getName()));
		out.putNextEntry(entry);
		ReadableStream in = file.getInputStream();
		FSUtil.transferStreams(in, new OutputStreamWriteable(out));
		in.close();
	}

	private static void packDir(ZipOutputStream out, FSFile dir, String parentPath) throws IOException {
		//System.out.println("Packing dir " + dir);
		String path = parentPath == null ? "" : appendPath(parentPath, dir.getName());

		List<? extends FSFile> children = dir.listFiles();
		if (parentPath != null) {
			ZipEntry e = new ZipEntry(path + "/");
			out.putNextEntry(e);
		}
		for (FSFile f : children) {
			if (f.isDirectory()) {
				packDir(out, f, path);
			} else {
				packFile(out, f, path);
			}
		}
	}

	public static void extractZipToFile(FSFile target, FSFile zip) {
		try {
			ZipInputStream in = new ZipInputStream(zip.getNativeInputStream());
			ReadableStream wrapper = new InputStreamReadable(in);

			ZipEntry e;
			while ((e = in.getNextEntry()) != null) {
				FSFile newFile = target.getChild(e.getName());
				if (e.isDirectory()) {
					newFile.mkdirs();
				} else {
					newFile.getParent().mkdirs();
					WriteableStream out = newFile.getOutputStream();

					FSUtil.transferStreams(wrapper, out);

					out.close();
				}
			}

			in.close();
		} catch (IOException ex) {
			Logger.getLogger(ZipArchive.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static boolean isZip(FSFile fsf) {
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
		if (parentPath == null) {
			return new ArrayList<>();
		}
		parentPath = stripLastSlash(parentPath);
		List<FSFile> l = new ArrayList<>();
		for (ZipEntry e : entries) {
			String nameWithoutEndSlash = stripLastSlash(e.getName());
			if (Objects.equals(FSUtil.getParentFilePath(nameWithoutEndSlash), parentPath)) {
				l.add(new ZipEntryFile(this, e.getName()));
			}
		}
		return l;
	}

	public static String stripLastSlash(String str) {
		String nameWithoutEndSlash = str;
		if (nameWithoutEndSlash.endsWith("/")) {
			nameWithoutEndSlash = nameWithoutEndSlash.substring(0, nameWithoutEndSlash.length() - 1);
		}
		return nameWithoutEndSlash;
	}

	public ZipEntry getEntryForPath(String path) {
		path = stripLastSlash(path);
		for (ZipEntry e : entries) {
			String cmpName = stripLastSlash(e.getName());

			if (cmpName.equals(path)) {
				return e;
			}
		}
		return null;
	}

	InputStream getEntryInputStream(ZipEntry entry) {
		if (entry == null) {
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
					if (Objects.equals(e.getName(), entry.getName())) {
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
