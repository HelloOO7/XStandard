package xstandard.fs;

import xstandard.gui.DialogUtils;
import xstandard.io.base.impl.ext.data.DataInStream;
import xstandard.io.base.impl.ext.data.DataOutStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A registry of a VFS's OvFS files that haven't been altered since being copied from BaseFS.
 */
public class VFSChangeBlacklist {

	private final VFS fs;

	private List<String> blacklistedPaths = new ArrayList<>();

	private FSFile tempBlacklistLocation;

	/**
	 * Creates a change blacklist at the given location, using a VFS. If a file is already present in the location, a user decision dialog is shown.
	 *
	 * @param location FSFile location of the blacklist.
	 * @param fs The VFS to target.
	 */
	public VFSChangeBlacklist(FSFile location, VFS fs) {
		this.fs = fs;
		tempBlacklistLocation = location;
		try {
			if (tempBlacklistLocation.exists()) {
				DataInStream in = new DataInStream(tempBlacklistLocation.getInputStream());
				int len = in.getLength();
				if (len > 0) {
					boolean restore = DialogUtils.showYesNoDialog(
						"Backup VFS data found",
						"The last VFS session was not shut down properly.\n"
						+ "As a result, leftover extracted file system contents are present in the VFS directory.\n"
						+ "However, the VFS can load fallback blacklist metadata to delete the files\n"
						+ "on next successful shutdown if they are still eligible for deletion.\n\n"
						+ "Do you want to restore the last session's blacklist?"
					);
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

	boolean hasPath(String path) {
		return blacklistedPaths.contains(path);
	}

	void putBlacklistPath(String path) {
		if (path == null) {
			throw new IllegalArgumentException("Path can not be null.");
		}
		if (path.contains("..")) {
			throw new IllegalArgumentException("Relative paths are forbidden in VFS.");
		}
		path = fs.getFS().getWildCardManager().getWildCardedPath(path);
		if (!blacklistedPaths.contains(path)) {
			blacklistedPaths.add(path);
			writeToIO();
		}
	}

	void removePathFromBlacklist(String path) {
		blacklistedPaths.remove(path);
		writeToIO();
	}

	void relocatePaths(String path, String toReplace) {
		for (int i = 0; i < blacklistedPaths.size(); i++) {
			String str = blacklistedPaths.get(i);
			if (str.contains(path)) {
				blacklistedPaths.set(i, str.replace(path, toReplace));
			}
		}
	}

	private void writeToIO() {
		try {
			DataOutStream dos = new DataOutStream(tempBlacklistLocation.getOutputStream());
			for (String blp : new ArrayList<>(blacklistedPaths)) {
				dos.writeString(blp);
			}
			dos.close();
		} catch (IOException ex) {
			Logger.getLogger(VFSChangeBlacklist.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Deletes the temporary blacklist file.
	 */
	public void terminate() {
		tempBlacklistLocation.delete();
	}

	/**
	 * Removes all blacklisted files from the disk.
	 */
	public void doRemoveFiles() {
		System.out.println("JVM shutting down, cleaning up unused OvFS files...");
		FSFile ovfsRoot = fs.getOvFSRoot();

		doRemoveFiles(ovfsRoot);
	}

	public void doRemoveFiles(FSFile root) {
		for (String blPath : blacklistedPaths) {
			System.out.println("Removing file " + blPath);
			FSFile victim = root.getMatchingChild(blPath, fs.getFS().getWildCardManager());
			if (victim != null) {
				victim.delete();
			} else {
				System.out.println("Failed to remove file (not found in OvFS).");
			}
		}
	}
}
