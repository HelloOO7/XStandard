package ctrmap.stdlib;

import java.util.prefs.Preferences;

public class CMStdLibPrefs {
	private static Preferences PREFS_ROOT = Preferences.userRoot().node("CTRMapStandardLibrary");
	
	public static Preferences node(String name){
		return PREFS_ROOT.node(name);
	}
}
