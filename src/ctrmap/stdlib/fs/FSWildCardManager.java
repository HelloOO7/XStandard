package ctrmap.stdlib.fs;

import ctrmap.stdlib.fs.accessors.arc.ArcFile;
import ctrmap.stdlib.fs.accessors.arc.ArcFileAccessor;
import java.util.HashMap;
import java.util.Map;

/**
 * A set of wild cards for FS managers.
 */
public class FSWildCardManager {

	/**
	 * A FSWildCardManager with no wild cards registered.
	 */
	public static final FSWildCardManager BLANK_WILD_CARD_MNG = new FSWildCardManager();

	private final Map<String, FSWildCard> wildCards = new HashMap<>();

	private final Map<String, FSWildCard> options = new HashMap<>();

	/**
	 * Creates a FSWildCardManager with a wild card set.
	 *
	 * @param wildCards The wild cards.
	 */
	public FSWildCardManager(FSWildCard... wildCards) {
		for (FSWildCard wc : wildCards) {
			this.wildCards.put(wc.getFullDDotId(), wc);
			for (String opt : wc.options) {
				options.put(opt, wc);
			}
		}
	}

	/**
	 * Converts a file path to a wild-carded one.
	 *
	 * @param originalPath A file path.
	 * @return The input path, with all possible elements replaced with their
	 * wild card tokens.
	 */
	public String getWildCardedPath(String originalPath) {
		for (FSWildCard wc : wildCards.values()) {
			for (String opt : wc.options) {
				String ddot = wc.getFullDDotId();
				if (originalPath.contains(opt) && !originalPath.contains(ddot)) {
					//This could be considered flawed when the same reference is used multiple times in the same path, however, this should never be the case
					originalPath = originalPath.replaceAll(opt, ddot);
				}
			}
		}
		return originalPath;
	}

	/**
	 * Converts a wildcarded path to one without wildcards with another
	 * non-wildcarded path as a reference for which file name options to choose.
	 *
	 * @param wildCardedPath A path with wild cards.
	 * @param referencePath A reference path, without wild cards.
	 * @return The input path with wildcard references removed.
	 */
	public String getNonWildCardedPathByActual(String wildCardedPath, String referencePath) {
		String[] pathElems = referencePath.split("/");
		Map<FSWildCard, String> selectedOptions = new HashMap<>();
		for (String pe : pathElems) {
			FSWildCard wc = options.get(pe);
			if (wc != null) {
				selectedOptions.put(wc, pe);
			}
		}

		String ddot;
		for (Map.Entry<String, FSWildCard> e : wildCards.entrySet()) {
			ddot = e.getKey();
			if (wildCardedPath.contains(ddot)) {
				FSWildCard wc = e.getValue();
				String repl = selectedOptions.get(wc);
				if (repl == null) {
					repl = wc.getFirstOption();
				}
				wildCardedPath = wildCardedPath.replaceAll(ddot, repl);
			}
		}

		return wildCardedPath;
	}

	/**
	 * Gets a file from a parent by a wildcard token.
	 *
	 * @param parent The parent file.
	 * @param ref A wildcard token handled by this FSWildCardManager.
	 * @return A child file that matches the wild card, or null if none found.
	 */
	public FSFile getExistingRefFile(FSFile parent, String ref) {
		FSWildCard wc = wildCards.get(ref);
		if (wc != null) {
			return getFirstExistingFile(parent, wc.getFirstOption(), wc.getOtherOptions());
		}
		return null;
	}

	private static FSFile getFirstExistingFile(FSFile parent, String defaultOption, String... otherOptions) {
		FSFile f = parent.getChild(defaultOption);
		for (String opt : otherOptions) {
			if (f == null || !f.exists()) {
				FSFile f2 = parent.getChild(opt);
				if (f2 != null && f2.exists()) {
					f = f2;
				}
			} else {
				break;
			}
		}
		return f;
	}

	/**
	 * Gets a child file using a path that may contain wild card tokens.
	 * @param parent The file the path is relative to.
	 * @param refPath A possibly wildcarded path.
	 * @return 
	 */
	public FSFile getFileFromRefPath(FSFile parent, String refPath) {
		return getFileFromRefPath(parent, refPath, null);
	}

	/**
	 * Gets a child file using a path that may contain wild card tokens.
	 * @param parent The file the path is relative to.
	 * @param refPath A possibly wildcarded path.
	 * @param afa An optional ArcFileAccessor.
	 * @return 
	 */
	public FSFile getFileFromRefPath(FSFile parent, String refPath, ArcFileAccessor afa) {
		FSFile currentParent = parent;
		if (refPath == null){
			return parent;
		}
		for (int i = 0; i < refPath.length(); i++) {
			String thing = getTextUntilSlash(refPath, i);
			i += thing.length();
			if (thing.startsWith(":") && thing.endsWith(":")) {
				currentParent = getExistingRefFile(currentParent, thing);
			} else {
				currentParent = currentParent.getChild(thing);
			}
			if (afa != null && afa.isArcFile(currentParent)) {
				currentParent = new ArcFile(currentParent, afa);
				if (i + 1 < refPath.length()) {
					return currentParent.getChild(refPath.substring(i + 1));
				}
			}
		}
		return currentParent;
	}

	private static String getTextUntilSlash(String str, int start) {
		int idx = str.indexOf('/', start);
		if (idx == -1) {
			idx = str.length();
		}
		return str.substring(start, idx);
	}
}
