package ctrmap.stdlib.fs.accessors.arc;

import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.io.base.LittleEndianIO;
import ctrmap.stdlib.util.ProgressMonitor;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface ArcFileAccessor {
	public List<FSFile> getArcFiles(ArcFile arc);
	public LittleEndianIO getIOForArcMember(ArcFile arc, String path);
	public InputStream getInputStreamForArcMember(ArcFile arc, String path);
	public OutputStream getOutputStreamForArcMember(ArcFile arc, String path);
	public int getDataSizeForArcMember(ArcFile arc, String path);
	public void writeToArcFile(ArcFile arc, ProgressMonitor monitor, ArcInput... inputs);
	public boolean isArcFile(FSFile f);
}
