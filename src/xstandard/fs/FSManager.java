package xstandard.fs;

/**
 * File-system access controller.
 */
public interface FSManager {
	/**
	 * Gets a file from a path relative to the FSManager's root.
	 * @param path The relative file path.
	 * @return A FSFile (may not be null).
	 */
	public FSFile getFsFile(String path);
}
