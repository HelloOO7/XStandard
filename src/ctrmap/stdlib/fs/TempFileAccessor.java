package ctrmap.stdlib.fs;

import java.io.File;

public class TempFileAccessor {
	public static final String JAVA_IO_TEMPDIR = "java.io.tmpdir";
	
	public static File createTempFile(String name){
		File f = new File(System.getProperty(JAVA_IO_TEMPDIR) + "/" + name);
		f.deleteOnExit();
		return f;
	}
}
