package ctrmap.stdlib.fs.accessors.arc;

import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.io.base.iface.IOStream;
import ctrmap.stdlib.io.base.iface.ReadableStream;
import ctrmap.stdlib.io.base.iface.WriteableStream;
import ctrmap.stdlib.util.ProgressMonitor;
import java.util.List;

public interface ArcFileAccessor {
	public List<FSFile> getArcFiles(ArcFile arc);
	public IOStream getIOForArcMember(ArcFile arc, String path);
	public ReadableStream getInputStreamForArcMember(ArcFile arc, String path);
	public WriteableStream getOutputStreamForArcMember(ArcFile arc, String path);
	public int getDataSizeForArcMember(ArcFile arc, String path);
	public void writeToArcFile(ArcFile arc, ProgressMonitor monitor, ArcInput... inputs);
	public boolean isArcFile(FSFile f);
}
