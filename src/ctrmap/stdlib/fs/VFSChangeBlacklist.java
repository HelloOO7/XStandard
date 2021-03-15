package ctrmap.stdlib.fs;

import ctrmap.stdlib.gui.DialogUtils;
import ctrmap.stdlib.io.LittleEndianDataInputStream;
import ctrmap.stdlib.io.LittleEndianDataOutputStream;
import ctrmap.stdlib.io.base.LittleEndianIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VFSChangeBlacklist {

	private List<String> blacklistedPaths = new ArrayList<>();

	private FSFile tempBlacklistLocation;

	public VFSChangeBlacklist(FSFile location) {
		tempBlacklistLocation = location;
		try {
			if (tempBlacklistLocation.exists()) {
				LittleEndianDataInputStream in = new LittleEndianDataInputStream(tempBlacklistLocation.getInputStream());
				int len = in.available();
				if (len > 0) {
					boolean restore = DialogUtils.showYesNoDialog("Backup VFS data found",
							"CTRMap's last session was not shut down properly.\n"
							+ "As a result, leftover extracted file system contents are present in the VFS directory.\n"
							+ "However, CTRMap can load fallback blacklist metadata to delete the files\n"
							+ "on next successful shutdown if they are still eligible for deletion.\n\n"
							+ "Do you want to restore the last session's blacklist?");
					if (restore) {
						while (in.getPosition() < len) {
							String path = in.readString();
							blacklistedPaths.add(path);
						}
					}
				}
				in.close();
			}
		} catch (IOException ex) {
			Logger.getLogger(VFSChangeBlacklist.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public boolean hasPath(String path) {
		return blacklistedPaths.contains(path);
	}

	public void putBlacklistPath(String path) {
		path = FSWildCard.getWildCardedPath(path);
		if (!blacklistedPaths.contains(path)) {
			blacklistedPaths.add(path);
			writeToIO();
		}
	}

	public void removePathFromBlacklist(String path) {
		blacklistedPaths.remove(path);
		writeToIO();
	}
	
	public void relocatePaths(String path, String toReplace){
		for (int i = 0; i < blacklistedPaths.size(); i++){
			String str = blacklistedPaths.get(i);
			if (str.contains(path)){
				blacklistedPaths.set(i, str.replace(path, toReplace));
			}
		}
	}

	private void writeToIO() {
		try {
			LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(tempBlacklistLocation.getOutputStream());
			for (String blp : blacklistedPaths) {
				dos.writeString(blp);
			}
			dos.close();
		} catch (IOException ex) {
			Logger.getLogger(VFSChangeBlacklist.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void terminate() {
		tempBlacklistLocation.delete();
	}

	public void doRemoveFiles(VFS vfs) {
		System.out.println("JVM shutting down, cleaning up unused OvFS files...");
		FSFile ovfsRoot = vfs.getOvFSRoot();

		for (String blPath : blacklistedPaths) {
			System.out.println("Removing file " + blPath);
			FSFile victim = ovfsRoot.getMatchingChild(blPath);
			if (victim != null) {
				victim.delete();
			} else {
				System.out.println("Failed to remove file (not found in OvFS).");
			}
		}
	}
}
