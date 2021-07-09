package ctrmap.stdlib;

import java.util.prefs.Preferences;

public class CMStdLibPrefs {
	private static Preferences PREFS_ROOT = Preferences.userRoot().node("CTRMapStandardLibrary");
	
	/**
	 * Gets a child node of CTRMap-StdLib's Preferences root.
	 * @param name Name of the node.
	 * @return The node in the preferences root.
	 */
	public static Preferences node(String name){
		return PREFS_ROOT.node(name);
	}
}
