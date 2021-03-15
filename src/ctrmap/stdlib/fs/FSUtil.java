package ctrmap.stdlib.fs;

import ctrmap.stdlib.fs.accessors.DiskFile;
import ctrmap.stdlib.fs.accessors.MemoryFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FSUtil {

	public static String cleanPath(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		return path;
	}

	public static void copy(FSFile source, FSFile target) {
		if (source instanceof DiskFile && target instanceof DiskFile) {
			//Optimized copy for real filesystem files
			DiskFile dfSrc = (DiskFile) source;
			DiskFile dfTgt = (DiskFile) target;
			try {
				Files.copy(dfSrc.getFile().toPath(), dfTgt.getFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ex) {
				Logger.getLogger(FSUtil.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		writeBytesToFile(target, readFileToBytes(source));
	}

	public static boolean fileCmp(FSFile f1, FSFile f2) {
		//Load all files below 8 megabytes to memory for faster comparison
		return fileCmp(f1, f2, 8000000);
	}

	public static boolean fileCmp(FSFile f1, FSFile f2, int bufferThreshold) {
		if (f1.exists() && f2.exists()) {
			if (!f1.isDirectory() && !f2.isDirectory()) {
				if (f1.length() == f2.length()) {
					if (f1.length() < bufferThreshold) {
						return Arrays.equals(readFileToBytes(f2), readFileToBytes(f1));
					} else {
						try {
							InputStream is1 = f1.getInputStream();
							InputStream is2 = f2.getInputStream();
							int b0 = 0;
							int b1 = 0;
							int size = is1.available();
							for (int i = 0; i < size; i++) {
								b0 = is1.read();
								b1 = is2.read();
								if (b0 != b1) {
									return false;
								}
							}
							return true;
						} catch (IOException ex) {
							Logger.getLogger(FSUtil.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
				}
			}
		}
		return false;
	}

	public static void copyFsFileFromTo(FSFile from, FSFile to) {
		writeBytesToFile(to, readFileToBytes(from));
	}

	public static void writeBytesToFile(File f, byte[] bytes) {
		try {
			Files.write(f.toPath(), bytes, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException ex) {
			Logger.getLogger(FSUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void writeBytesToFile(FSFile f, byte[] bytes) {
		//This won't get any optimizations with NIO stuff as it's actually faster than that
		writeBytesToStream(bytes, f.getOutputStream());
	}
	
	public static void writeStringToFile(FSFile f, String str){
		writeBytesToFile(f, str.getBytes(Charset.forName("UTF-8")));
	}

	public static byte[] readFileToBytes(File f) {
		try {
			return Files.readAllBytes(f.toPath());
		} catch (IOException ex) {
			Logger.getLogger(FSUtil.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	public static byte[] readFileToBytes(FSFile f) {
		//Optimization for native files
		if (f == null) {
			return null;
		}
		if (f instanceof DiskFile) {
			DiskFile df = (DiskFile) f;
			return readFileToBytes(df.getFile());
		} else if (f instanceof MemoryFile) {
			return ((MemoryFile) f).getBackingArray();
		} else if (f instanceof VFSFile) {
			FSFile ef = ((VFSFile) f).getExistingFile();
			if (ef instanceof DiskFile) {
				return readFileToBytes(((DiskFile) ef).getFile());
			} else if (ef instanceof MemoryFile) {
				return ((MemoryFile) ef).getBackingArray();
			}
		}
		return readStreamToBytes(f.getInputStream());
	}

	public static byte[] readStreamToBytes(InputStream strm) {
		try {
			byte[] b = new byte[strm.available()];
			strm.read(b);
			strm.close();
			return b;
		} catch (IOException ex) {
			Logger.getLogger(FSUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static void writeBytesToStream(byte[] bytes, OutputStream strm) {
		try {
			strm.write(bytes);
			strm.close();
		} catch (IOException ex) {
			Logger.getLogger(FSUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void mkDirsIfNotContains(File container, String... requiredContents) {
		List<String> contents = Arrays.asList(container.list());
		for (int i = 0; i < requiredContents.length; i++) {
			if (!contents.contains(requiredContents[i])) {
				new File(container.getAbsolutePath() + "/" + requiredContents[i]).mkdir();
			}
		}
	}

	public static String getFileName(String path) {
		int start = path.replace('\\', '/').lastIndexOf("/") + 1;
		return path.substring(start, path.length());
	}

	public static String getFileNameWithoutExtension(String fileName) {
		String name = fileName;
		int lioDot = name.lastIndexOf(".");
		return lioDot != -1 ? name.substring(0, lioDot) : name;
	}
}
