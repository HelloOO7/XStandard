package ctrmap.stdlib.res;

import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.fs.TempFileAccessor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ResourceAccess {

	public static ResourceTable resTbl = readResourceTable();

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

	public static void main(String[] args) {
		buildResourceTable(new File("src/ctrmap/resources"));
	}

	public static void buildResourceTable(File root) {
		ResourceTable rt = new ResourceTable(root);
		rt.write(new File(root + "/res.tbl"));
	}

	public static ResourceTable readResourceTable() {
		InputStream stm = getStream("res.tbl");
		try {
			stm.available();
			return new ResourceTable(stm);
		} catch (Exception ex) {
			return null;

		}
	}
}
