package xstandard.fs.accessors.arc;

import xstandard.fs.FSFile;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.iface.ReadableStream;
import xstandard.io.base.iface.WriteableStream;
import xstandard.util.ProgressMonitor;
import java.util.List;

public interface ArcFileAccessor {
	public List<? extends FSFile> getArcFiles(ArcFile arc);
	public IOStream getIOForArcMember(ArcFile arc, String path);
	public ReadableStream getInputStreamForArcMember(ArcFile arc, String path);
	public WriteableStream getOutputStreamForArcMember(ArcFile arc, String path);
	public int getDataSizeForArcMember(ArcFile arc, String path);
	public void writeToArcFile(ArcFile arc, ProgressMonitor monitor, ArcInput... inputs);
	public boolean isArcFile(FSFile f);
}
