package xstandard.fs;

import java.util.Arrays;

/**
 * A "wild card" allowing to refer to a file by multiple names.
 *
 * The implementation takes a "reference name" as a token enclosed in :double
 * dots: and searches for files using a set of provided names.
 */
public class FSWildCard {

	public final String ddotId;
	public final String[] options;

	/**
	 * Creates a FS wild card match set.
	 * @param ddotId The reference path token.
	 * @param possibleOptions The file names this token can take.
	 */
	public FSWildCard(String ddotId, String... possibleOptions) {
		this.ddotId = ddotId;
		this.options = possibleOptions;
	}

	/**
	 * Gets the path token, enclosed in double dots.
	 * @return 
	 */
	public String getFullDDotId() {
		return ":" + ddotId + ":";
	}

	/**
	 * Gets the preferred file name of the wild card.
	 * @return 
	 */
	public String getFirstOption() {
		return options[0];
	}

	/**
	 * Gets the alternative names of the wild card.
	 * @return 
	 */
	public String[] getOtherOptions() {
		return Arrays.copyOfRange(options, 1, options.length);
	}
}
