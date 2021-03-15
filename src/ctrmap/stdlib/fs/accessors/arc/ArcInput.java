
package ctrmap.stdlib.fs.accessors.arc;

import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.fs.accessors.MemoryFile;

/**
 *
 */
public class ArcInput {

	public boolean compressLZ;
	public boolean compressAuto;
	public String targetPath;
	public FSFile data;

	public ArcInput(String path, FSFile fsf, boolean compressExplicit) {
		targetPath = path;
		data = fsf;
		compressLZ = compressExplicit;
		compressAuto = false;
	}

	public ArcInput(String path, byte[] data, boolean compressExplicit) {
		this(path, new MemoryFile(path, data), compressExplicit);
	}

	public ArcInput(String path, FSFile fsf) {
		targetPath = path;
		data = fsf;
		compressLZ = false;
		compressAuto = true;
	}

	public ArcInput(String path, byte[] data) {
		this(path, new MemoryFile(path, data));
	}

	public int getTargetEntryNum() {
		return Integer.parseInt(targetPath.split("/")[0]);
	}

	public ArcLanguage getTargetLangId() {
		String[] path = targetPath.split("/");
		if (path.length == 2) {
			return ArcLanguage.getLangForFriendlyName(path[1]);
		}
		return ArcLanguage.NULL;
	}

}
