package xstandard.res;

import xstandard.fs.FSUtil;
import xstandard.fs.TempFileAccessor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Utility for retrieving bundled program resources using prebuilt tables.
 */
public class ResourceAccess {

	private static final Set<String> loadedTableNames = new HashSet<>();
	private static List<ResourceTable> mountedResTables = new ArrayList<>();
	
	private static final ClassLoader classLoader = ResourceAccess.class.getClassLoader();

	/**
	 * Constructs a ResourceFile object with a pathname.
	 * @param pathname Path of the resource file.
	 * @return A ResourceFile that links to the resource in the filesystem/JAR.
	 */
	public static ResourceFile getResourceFile(String pathname) {
		ResourceTable.ResourceInfo i = null;
		for (ResourceTable t : mountedResTables) {
			i = t.getResInfo(pathname);
			if (i != null) {
				break;
			}
		}
		if (i == null) {
			throw new NullPointerException(pathname + " is not a valid resource.");
		}
		return new ResourceFile(i);
	}

	/**
	 * Reads a resource into a byte array.
	 * @param name Path of the resource.
	 * @return An array of bytes containing the entire resource's data.
	 */
	public static byte[] getByteArray(String name) {
		return FSUtil.readStreamToBytesFastAndDangerous(getStream(name));
	}

	/**
	 * Returns an InputStream linked to a program resource.
	 * @param name Path of the resource.
	 * @return An InputStream of the resource data.
	 */
	public static InputStream getStream(String name) {
		return classLoader.getResourceAsStream(name);
	}

	/**
	 * Copies a resource to the default temporary directory.
	 * @param name Path of the resource.
	 * @return The File into which the resource was copied.
	 */
	public static File copyToTemp(String name) {
		try {
			InputStream in = getStream(name);
			byte[] b = new byte[in.available()];
			in.read(b);
			in.close();
			File out = TempFileAccessor.createTempFile(UUID.randomUUID() + ".tmp");
			OutputStream os = new FileOutputStream(out);
			os.write(b);
			os.close();
			return out;
		} catch (IOException ex) {
			return null;
		}
	}

	public static void buildResourceTable(File root, String rootClasspath, String tableName) {
		ResourceTable rt = new ResourceTable(root, rootClasspath);
		rt.write(new File(root + "/" + tableName));
	}
	
	/**
	 * Builds a resource table from a file tree.
	 * @param root The root directory of the resource tree,
	 * @param rootClasspath Classpath of the source root directory.
	 * @param rootPrefix A path prefix to add to all files in the resource table.
	 * @param tableName Name of the table file in 'root'.
	 */
	public static void buildResourceTable(File root, String rootClasspath, String rootPrefix, String tableName) {
		ResourceTable rt = new ResourceTable(root, rootClasspath, rootPrefix);
		rt.write(new File(root + "/" + tableName));
	}

	/**
	 * Loads a resource table from a program resource location.
	 * If the table was already loaded, nothing is done.
	 * @param tableName Path to the table in program resources.
	 */
	public static void loadResourceTable(String tableName) {
		if (!loadedTableNames.contains(tableName)) {
			InputStream stm = getStream(tableName);
			try {
				stm.available();
				ResourceTable tbl = new ResourceTable(stm);
				mountedResTables.add(tbl);
				loadedTableNames.add(tableName);
			} catch (Exception ex) {

			}
		}
	}
}
