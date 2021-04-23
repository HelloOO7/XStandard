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

public class ResourceAccess {

	private static final List<String> loadedTables = new ArrayList<>();

	public static ResourceTable resTbl = new ResourceTable();

	public static ResourceFile getResourceFile(String pathname) {
		ResourceTable.ResourceInfo i = resTbl.getResInfo(pathname);
		if (i == null) {
			throw new NullPointerException(pathname + " is not a valid resource.");
		}
		return new ResourceFile(i);
	}

	public static byte[] getByteArray(String name) {
		return FSUtil.readStreamToBytes(getStream(name));
	}

	public static InputStream getStream(String name) {
		return ResourceAccess.class.getClassLoader().getResourceAsStream("ctrmap/resources/" + name);
	}

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
