package ctrmap.stdlib.res;

import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.fs.TempFileAccessor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utility for retrieving bundled program resources using prebuilt tables.
 */
public class ResourceAccess {

	private static final List<String> loadedTables = new ArrayList<>();

	public static ResourceTable resTbl = new ResourceTable();
	
	private static final ClassLoader classLoader = ResourceAccess.class.getClassLoader();

	/**
	 * Constructs a ResourceFile object with a pathname.
	 * @param pathname Path of the resource file.
	 * @return A ResourceFile that links to the resource in the filesystem/JAR.
	 */
	public static ResourceFile getResourceFile(String pathname) {
		ResourceTable.ResourceInfo i = resTbl.getResInfo(pathname);
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
		return classLoader.getResourceAsStream("ctrmap/resources/" + name);
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

	public static void buildResourceTable(File root, String tableName) {
		ResourceTable rt = new ResourceTable(root);
		rt.write(new File(root + "/" + tableName));
	}
	
	/**
	 * Builds a resource table from a file tree.
	 * @param root The root directory of the resource tree,
	 * @param rootPrefix A path prefix to add to all files in the resource table.
	 * @param tableName Name of the table file in 'root'.
	 */
	public static void buildResourceTable(File root, String rootPrefix, String tableName) {
		ResourceTable rt = new ResourceTable(root, rootPrefix);
		rt.write(new File(root + "/" + tableName));
	}

	/**
	 * Loads a resource table from a program resource location.
	 * If the table was already loaded, nothing is done.
	 * @param tableName Path to the table in program resources.
	 */
	public static void loadResourceTable(String tableName) {
		if (!loadedTables.contains(tableName)) {
			InputStream stm = getStream(tableName);
			try {
				stm.available();
				ResourceTable tbl = new ResourceTable(stm);
				resTbl.merge(tbl);
				loadedTables.add(tableName);
			} catch (Exception ex) {

			}
		}
	}
}
