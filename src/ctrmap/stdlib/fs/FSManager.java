package ctrmap.stdlib.fs;

import ctrmap.stdlib.fs.accessors.arc.ArcFileAccessor;

/**
 * File-system access controller. Supports mounting of data archives using
 * ArcFileAccessor.
 */
public interface FSManager {
	
	/**
	 * Gets the ArcFileAccessor for recognizing ArcFiles.
	 * @return An ArcFileAccessor, or null if this FSManager does not use one.
	 */
	public default ArcFileAccessor getArcFileAccessor(){
		return null;
	}

	/**
	 * Gets the FSWildCardManager for recognizing wild card paths.
	 * @return A FSWildCardManager.
	 */
	public default FSWildCardManager getWildCardManager(){
		return FSWildCardManager.BLANK_WILD_CARD_MNG;
	}

	/**
	 * Gets a file from a path relative to the FSManager's root.
	 * @param path The relative file path.
	 * @return A FSFile (may not be null).
	 */
	public FSFile getFsFile(String path);

	/**
	 * Gets a file from the FSManager root using its wild card set.
	 * @param refPath A wildcard-enabled relative file path.
	 * @return 
	 */
	public default FSFile getFileFromRefPath(String refPath) {
		return getFileFromRefPath(getFsFile(""), refPath);
	}
	
	/**
	 * Gets a file from the FSManager root using its wild card set.
	 * @param parent The parent of the relative path.
	 * @param refPath A wildcard-enabled relative file path.
	 * @return 
	 */
	public default FSFile getFileFromRefPath(FSFile parent, String refPath) {
		return getWildCardManager().getFileFromRefPath(parent, refPath, getArcFileAccessor());
	}
}
