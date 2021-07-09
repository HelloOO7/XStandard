package ctrmap.stdlib.fs;

import java.io.File;

/**
 * Simple utility for creating Files in the OS's temporary directory.
 */
public class TempFileAccessor {

	public static final String JAVA_IO_TEMPDIR = "java.io.tmpdir";

	private static final String TEMPDIR = System.getProperty(JAVA_IO_TEMPDIR);

	/**
	 * Creates a file in 'java.io.tmpdir'
	 *
	 * @param name Name/path of the file in the temporary directory.
	 * @return A temporary file. The file will be deleted on VM shutdown.
	 */
	public static File createTempFile(String name) {
		File f = new File(TEMPDIR + "/" + name);
		f.deleteOnExit();
		return f;
	}
}
