package xstandard.formats.zip;

import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.iface.ReadableStream;
import xstandard.io.base.iface.WriteableStream;
import xstandard.io.base.impl.InputStreamReadable;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.InputStream;
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
		this.path = ZipArchive.stripLastSlash(path);
		this.e = arc.getEntryForPath(path);
	}
	
	@Override
	public FSFile getChild(String forName) {
		return arc.getChild(path + "/" + forName);
	}

	@Override
	public FSFile getParent() {
		return arc.getChild(FSUtil.getParentFilePath(path));
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
		if (e == null){
			return null;
		}
		return new ZipReadableFixup(arc.getEntryInputStream(e), e);
	}

	@Override
	public WriteableStream getOutputStream() {
		throw new UnsupportedOperationException("Zip archives are not yet editable.");
	}

	@Override
	public IOStream getIO() {
		if (e == null){
			return null;
		}
		return new DataIOStream(getBytes());
	}

	@Override
	public List<FSFile> listFiles() {
		return arc.listFilesByParentPath(path);
	}

	@Override
	public int getPermissions() {
		return FSF_ATT_READ;
	}
	
	public static class ZipReadableFixup extends InputStreamReadable {
		
		private int size;
		
		public ZipReadableFixup(InputStream in, ZipEntry e) {
			super(in);
			size = (int)e.getSize();
		}
		
		@Override
		public int getLength(){
			return size;
		}
		
	}
}
