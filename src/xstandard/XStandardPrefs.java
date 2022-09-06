package xstandard;

import java.util.prefs.Preferences;

public class XStandardPrefs {
	private static Preferences PREFS_ROOT = Preferences.userRoot().node("XStandard");
	
	/**
	 * Gets a child node of XStandard's Preferences root.
	 * @param name Name of the node.
	 * @return The node in the preferences root.
	 */
	public static Preferences node(String name){
		return PREFS_ROOT.node(name);
	}
}
