package ctrmap.stdlib.fs;

import ctrmap.stdlib.fs.accessors.DiskFile;
import ctrmap.stdlib.fs.accessors.MemoryFile;
import ctrmap.stdlib.io.base.iface.ReadableStream;
import ctrmap.stdlib.io.base.iface.WriteableStream;
import ctrmap.stdlib.io.base.impl.access.MemoryStream;
import ctrmap.stdlib.io.base.impl.ext.data.DataInStream;
import ctrmap.stdlib.io.util.StringIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilities for file operations.
 */
public class FSUtil {

	/**
	 * Checks if a FSFile starts with the given String.
	 *
	 * @param fsf A readable FSFile.
	 * @param magic The magic to check for.
	 * @return True if the first magic.length() bytes are equal to 'magic' in
	 * ASCII.
	 */
	public static boolean checkFileMagic(FSFile fsf, String magic) {
		if (!fsf.isFile() || !fsf.canRead()) {
			return false;
		}
		try (DataInStream dis = new DataInStream(fsf.getInputStream())) {
			if (dis.getLength() >= magic.length()) {
				return StringIO.checkMagic(dis, magic);
			}
			return false;
		} catch (IOException ex) {
			Logger.getLogger(FSUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}

	/**
	 * Removes a leading '/' from a file path, if present.
	 *
	 * @param path A file path, possibly prefixed with '/'.
	 * @return The input file path, with leading '/' removed.
	 */
	public static String cleanPathFromRootSlash(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		return path;
	}

	/**
	 * Fully copies a FSFile directory and its contents from one location to
	 * another.
	 *
	 * @param source The directory to copy from.
	 * @param target The directory to copy to.
	 */
	public static void copyDirectory(FSFile source, FSFile target) {
		if (source.isDirectory() && (!target.exists() || target.isDirectory())) {
			target.mkdirs();
			copyChildren(source, target);
		}
	}

	private static void copyChildren(FSFile parent1, FSFile parent2) {
		List<? extends FSFile> files = parent1.listFiles();
		for (FSFile f : files) {
			FSFile f2 = parent2.getChild(f.getName());
			if (f.isDirectory()) {
				f2.mkdirs();
				copyChildren(f, f2);
			} else {
				copy(f, f2);
			}
		}
	}

	/**
	 * Copies a disk File from one location to another, replacing any existing
	 * file at the location.
	 *
	 * @param source The file to copy.
	 * @param target The path to copy to.
	 */
	public static void copy(File source, File target) {
		try {
			Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex) {
			Logger.getLogger(FSUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Moves a disk File from one location to another, replacing any existing
	 * file at the location.
	 *
	 * @param source The file to move.
	 * @param target The path to move to.
	 */
	public static void move(File source, File target) {
		try {
			Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex) {
			Logger.getLogger(FSUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Copies an FSFile to any writable FSFile. The FSFile can be either a
	 * directory or a file.
	 *
	 * The method will use a faster, native routine if both files are on the
	 * disk. The method will clone the backing array of the first file into the
	 * other if they are both MemoryFiles.
	 *
	 * @param source The file or directory to copy from.
	 * @param target The file or directory to copy to.
	 */
	public static void copy(FSFile source, FSFile target) {
		if (source.isDirectory() && (target.isDirectory() || !target.exists())) {
			copyDirectory(source, target);
			return;
		}

		if (transferFileIfClass(DiskFile.class, source, target, (DiskFile src, DiskFile tgt) -> {
			//Optimized copy for real Files
			copy(src.getFile(), tgt.getFile());
		})) {
			return;
		}

		if (transferFileIfClass(MemoryFile.class, source, target, (MemoryFile src, MemoryFile tgt) -> {
			//Optimized copy for MemoryFiles
			tgt.setBytes(src.getBytes().clone());
		})) {
			return;
		}

		writeBytesToFile(target, readFileToBytes(source));
	}

	/**
	 * Moves an FSFile to any writable FSFile. The FSFile can be either a
	 * directory or a file.
	 *
	 * The method will use a faster, native routine if both files are on the
	 * disk. The method will clone the backing array of the first file into the
	 * other if they are both MemoryFiles. Otherwise, a copy operation is
	 * performed, followed by the source file being deleted.
	 *
	 * @param source The file or directory to move.
	 * @param target The file or directory to move to.
	 */
	public static void move(FSFile source, FSFile target) {
		if (source.isDirectory() && target.isDirectory()) {
			copyDirectory(source, target);
			source.delete();
			return;
		}

		if (transferFileIfClass(DiskFile.class, source, target, (DiskFile src, DiskFile tgt) -> {
			//Optimized move for real Files
			move(src.getFile(), tgt.getFile());
		})) {
			return;
		}

		if (transferFileIfClass(MemoryFile.class, source, target, (MemoryFile src, MemoryFile tgt) -> {
			//Optimized move for MemoryFiles
			tgt.setBytes(src.getBytes());
		})) {
			return;
		}

		//Using setPath could result in undefined behavior if the files are not on the same file system
		writeBytesToFile(target, readFileToBytes(source));
		source.delete();
	}

	private static <T extends FSFile> T getFileOfClass(Class<T> cls, FSFile fsf) {
		if (cls.isAssignableFrom(fsf.getClass())) {
			return (T) fsf;
		} else if (fsf instanceof VFSFile) {
			FSFile vfsExistingFile = ((VFSFile) fsf).getExistingFile();
			if (cls.isAssignableFrom(vfsExistingFile.getClass())) {
				return (T) vfsExistingFile;
			}
		}
		return null;
	}

	private static interface FSFileTransferCallback<T extends FSFile> {

		public void transfer(T source, T target);
	}

	private static <T extends FSFile> boolean transferFileIfClass(Class<T> cls, FSFile source, FSFile target, FSFileTransferCallback<T> callback) {
		T srcT = getFileOfClass(cls, source);
		T tgtT = getFileOfClass(cls, target);

		if (srcT != null && tgtT != null) {
			callback.transfer(srcT, tgtT);
			return true;
		}
		return false;
	}

	/**
	 * Compares the contents of two FSFiles using the default buffer size of
	 * 8MB.
	 *
	 * @param f1 The LHS of the comparison.
	 * @param f2 The RHS of the comparison.
	 * @return True if the files are equal, byte-by-byte.
	 */
	public static boolean fileCmp(FSFile f1, FSFile f2) {
		//Load all files below 8 megabytes to memory for faster comparison
		return fileCmp(f1, f2, 8000000);
	}

	/**
	 * Compares the contents of two FSFiles.
	 *
	 * @param f1 The LHS of the comparison.
	 * @param f2 The RHS of the comparison.
	 * @param bufferThreshold The file size threshold below which the files
	 * should be compared in memory instead of their streams.
	 * @return True if the files are equal, byte-by-byte.
	 */
	public static boolean fileCmp(FSFile f1, FSFile f2, int bufferThreshold) {
		if (f1.exists() && f2.exists()) {
			if (!f1.isDirectory() && !f2.isDirectory()) {
				if (f1.length() == f2.length()) {
					if (f1.length() < bufferThreshold) {
						byte[] b1 = readFileToBytes(f1);
						byte[] b2 = readFileToBytes(f2);
						return Arrays.equals(b1, b2);
					} else {
						try {
							ReadableStream is1 = f1.getInputStream();
							ReadableStream is2 = f2.getInputStream();
							int b0 = 0;
							int b1 = 0;
							int size = is1.getLength();
							boolean b = true;
							for (int i = 0; i < size; i++) {
								b0 = is1.read();
								b1 = is2.read();
								if (b0 != b1) {
									b = false;
									break;
								}
							}
							is1.close();
							is2.close();
							return b;
						} catch (IOException ex) {
							Logger.getLogger(FSUtil.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Copies a FSFile file from one location to another without optimizations.
	 *
	 * @param from The FSFile to copy.
	 * @param to The FSFile to copy to.
	 */
	public static void copyFsFileFromTo(FSFile from, FSFile to) {
		writeBytesToFile(to, readFileToBytes(from));
	}

	/**
	 * Writes an array of bytes into a disk File. Note that the non-native
	 * operation usually turns out to be faster.
	 *
	 * @param f File to write into.
	 * @param bytes The data to write.
	 */
	public static void writeBytesToFile(File f, byte[] bytes) {
		try {
			Files.write(f.toPath(), bytes, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException ex) {
			Logger.getLogger(FSUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Writes an array of bytes into a FSFile.
	 *
	 * @param f FSFile to write into.
	 * @param bytes The data to write.
	 */
	public static void writeBytesToFile(FSFile f, byte[] bytes) {
		//This won't get any optimizations with NIO stuff as it's actually faster than that
		WriteableStream os = f.getOutputStream();
		writeBytesToStream(bytes, os);
	}

	/**
	 * Writes a String into a FSFile using the UTF-8 character set.
	 *
	 * @param f FSFile to write into.
	 * @param str A String to write, or null to clear the file.
	 */
	public static void writeStringToFile(FSFile f, String str) {
		writeBytesToFile(f, str == null ? new byte[0] : str.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Fully reads a disk file into a byte array.
	 *
	 * @param f The File to read.
	 * @return A byte array of the file data, or null if the operation failed.
	 */
	public static byte[] readFileToBytes(File f) {
		try {
			Path pth = f.toPath();
			return Files.readAllBytes(pth);
		} catch (IOException ex) {
			Logger.getLogger(FSUtil.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	/**
	 * Fully reads an FSFile into a byte array.
	 *
	 * Disk files will be read using a native method. Memory files will directly
	 * return their backing array.
	 *
	 * @param f The FSFile to read.
	 * @return A byte array of the file data, or null if the operation failed.
	 */
	public static byte[] readFileToBytes(FSFile f) {
		//Optimization for native files
		if (f == null) {
			return null;
		}

		DiskFile df = getFileOfClass(DiskFile.class, f);
		if (df != null) {
			return readFileToBytes(df.getFile());
		}

		//This mf right here .. !
		MemoryFile mf = getFileOfClass(MemoryFile.class, f);
		if (mf != null) {
			return mf.getBackingArray();
		}

		return readStreamToBytesFastAndDangerous(f.getInputStream());
	}

	/**
	 * Safely reads an InputStream to bytes, using a default 32kB buffer. Unlike
	 * the "fast and dangerous" method, this operation does not require knowing
	 * the allocation size beforehand, but comes at the disadvantage of being
	 * slightly slower because of that.
	 *
	 * @param strm Stream to read from.
	 * @return Array of all remaining bytes in the stream.
	 */
	public static byte[] readStreamToBytes(InputStream strm) {
		try {
			MemoryStream out = new MemoryStream();
			byte[] buf = new byte[32768];
			int read;
			while ((read = strm.read(buf)) != -1) {
				out.write(buf, 0, read);
			}
			return out.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(FSUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 * Reads an InputStream to a byte array in the fastest way possible. May
	 * result in undefined behavior if the stream's available() method does not
	 * produce accurate results (as it is permitted not to do so).
	 *
	 * @param strm An InputStream with an eligible available() method.
	 * @return A byte array containing all the remaining data in the input
	 * stream.
	 */
	public static byte[] readStreamToBytesFastAndDangerous(InputStream strm) {
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

	/**
	 * Reads a ReadableStream to a byte array in the fastest way possible. May
	 * result in undefined behavior if the stream's getLength() method does not
	 * produce accurate results (implementations reliant on
	 * InputStream.available()).
	 *
	 * @param strm An ReadableStream with an eligible getLength() method.
	 * @return A byte array containing all the remaining data in the input
	 * stream.
	 */
	public static byte[] readStreamToBytesFastAndDangerous(ReadableStream strm) {
		try {
			byte[] b = new byte[strm.getLength()];
			strm.read(b);
			strm.close();
			return b;
		} catch (IOException ex) {
			Logger.getLogger(FSUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 * Writes an array of bytes to an OutputStream with handled IOExceptions.
	 *
	 * @param bytes The bytes to write.
	 * @param strm The OutputStream to write into.
	 */
	public static void writeBytesToStream(byte[] bytes, OutputStream strm) {
		try {
			strm.write(bytes);
			strm.close();
		} catch (IOException ex) {
			Logger.getLogger(FSUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Writes an array of bytes to a WriteableStream with handled IOExceptions.
	 *
	 * @param bytes The bytes to write.
	 * @param strm The WriteableStream to write into.
	 */
	public static void writeBytesToStream(byte[] bytes, WriteableStream strm) {
		try {
			strm.write(bytes);
			strm.close();
		} catch (IOException ex) {
			Logger.getLogger(FSUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Creates a requested set of sub-directories in a directory.
	 *
	 * @param container The directory to create the sub-directories in.
	 * @param requiredContents Names of the sub-directories to be created.
	 */
	public static void mkDirsIfNotContains(File container, String... requiredContents) {
		List<String> contents = Arrays.asList(container.list());
		for (int i = 0; i < requiredContents.length; i++) {
			if (!contents.contains(requiredContents[i])) {
				new File(container.getAbsolutePath() + "/" + requiredContents[i]).mkdir();
			}
		}
	}

	/**
	 * Gets the pathname corresponding to a path's parent element.
	 *
	 * @param path A path.
	 * @return The parent of the path, or null if the path is a root path.
	 */
	public static String getParentFilePath(String path) {
		int end = path.replace('\\', '/').lastIndexOf("/");
		if (end != -1) {
			return path.substring(0, end);
		}
		return null;
	}

	/**
	 * Gets the file name in a pathname, AKA its last element.
	 *
	 * @param path A path.
	 * @return The last element of the path, possibly the entire path for root
	 * paths.
	 */
	public static String getFileName(String path) {
		int start = path.replace('\\', '/').lastIndexOf("/") + 1;
		return path.substring(start, path.length());
	}

	/**
	 * Gets the file extension of a file name, without the dot.
	 *
	 * @param fileName A file name.
	 * @return The extension of the file name, or an empty string if there is
	 * none.
	 */
	public static String getFileExtension(String fileName) {
		int lioDot = getLastDotIndexInName(fileName);
		return lioDot == -1 ? "" : fileName.substring(lioDot + 1);
	}

	/**
	 * Gets the file extension of a file name, including the dot.
	 *
	 * @param fileName A file name.
	 * @return The extension of the file name, or an empty string if there is
	 * none.
	 */
	public static String getFileExtensionWithDot(String fileName) {
		int lioDot = getLastDotIndexInName(fileName);
		return lioDot == -1 ? "" : fileName.substring(lioDot);
	}

	/**
	 * Gets the file name from a path, with the file extension removed.
	 * 
	 * For example:
	 *  - C:/Work/Stuff.txt -> Stuff
	 *  - Sonic.exe -> Sonic
	 * 
	 * @param fileName A file name or path.
	 * @return The input without the file extension.
	 */
	public static String getFileNameWithoutExtension(String fileName) {
		fileName = getFileName(fileName);
		int lioDot = getLastDotIndexInName(fileName);
		return lioDot != -1 ? fileName.substring(0, lioDot) : fileName;
	}

	private static int getLastDotIndexInName(String fileName) {
		int slash = fileName.indexOf("/");
		int lioDot = fileName.lastIndexOf(".");
		return lioDot > slash ? lioDot : -1;
	}
}
