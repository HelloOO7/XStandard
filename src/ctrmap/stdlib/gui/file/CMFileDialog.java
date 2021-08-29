package ctrmap.stdlib.gui.file;

import com.sun.javafx.application.PlatformImpl;
import ctrmap.stdlib.CMStdLibPrefs;
import ctrmap.stdlib.util.ArraysEx;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class CMFileDialog {

	private static Preferences prefs = CMStdLibPrefs.node("CMFileDialog");

	public static File openDirectoryDialog() {
		return openDirectoryDialog((String)null);
	}

	public static File openDirectoryDialog(String title) {
		return openDirectoryDialog(title, null);
	}

	public static File openDirectoryDialog(File initDirectory) {
		return openDirectoryDialog(null, initDirectory);
	}
	
	public static File openDirectoryDialog(String title, File initDirectory) {
		List<File> r = openFileDialog(title, false, true, false, initDirectory, null, new ExtensionFilter[0]);
		if (r.isEmpty()) {
			return null;
		}
		return r.get(0);
	}

	public static File openSaveFileDialog() {
		return openSaveFileDialog((String) null);
	}

	public static File openSaveFileDialog(String title) {
		return openSaveFileDialog(title, (String) null);
	}

	public static File openSaveFileDialog(String title, String defaultName) {
		return openFileDialog(title, true, null, defaultName, new ExtensionFilter[0]);
	}

	public static File openSaveFileDialog(ExtensionFilter... filters) {
		return openSaveFileDialog(null, filters);
	}

	public static File openSaveFileDialog(String title, ExtensionFilter... filters) {
		return openSaveFileDialog(title, null, filters);
	}

	public static File openSaveFileDialog(String title, String defaultName, ExtensionFilter... filters) {
		return openFileDialog(title, true, null, defaultName, filters);
	}

	public static File openFileDialog() {
		return openFileDialog((String) null);
	}

	public static List<File> openMultiFileDialog() {
		return openMultiFileDialog(new ExtensionFilter[0]);
	}
	
	public static List<File> openMultiFileDialog(ExtensionFilter... filters) {
		return openFileDialog(null, false, false, true, null, null, filters);
	}

	public static File openFileDialog(String title) {
		return openFileDialog(title, false, null, null, new ExtensionFilter[0]);
	}

	public static File openFileDialog(ExtensionFilter... filters) {
		return openFileDialog(null, false, null, null, filters);
	}

	public static File openFileDialog(String title, ExtensionFilter... filters) {
		return openFileDialog(title, false, null, null, filters);
	}

	public static File openFileDialog(String title, boolean isSave, File initDirectory, String initFileName, ExtensionFilter... extensionFilters) {
		List<File> r = openFileDialog(title, isSave, false, false, initDirectory, initFileName, extensionFilters);
		if (!r.isEmpty()) {
			return r.get(0);
		}
		return null;
	}

	private static void tryInitJFX() {
		try {
			//We have to use PlatformImpl because of JRE 1.8 shipping with JavaFX which doesn't have the method in Platform
			//Using com.sun.* classes is generally a bad idea, but since we ship FrankenJFX to support newer versions of JDK, we can be sure it doesn't get changed
			PlatformImpl.startup(new Runnable() {
				@Override
				public void run() {
				}
			});
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	private static final Object JFX_LOCK = new Object();

	public static List<File> openFileDialog(String title, boolean isSave, boolean isDirectory, boolean allowMultiple, File initDirectory, String initFileName, ExtensionFilter... extensionFilters) {
		tryInitJFX();
		synchronized (JFX_LOCK) {
			final List<File> rsl = new ArrayList<>();

			//We could use PlatformImpl from the newer JFX SDK which allows dynamic startup/runlater, but this would break compat with JDK 8
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					synchronized (JFX_LOCK) {
						if (isDirectory) {
							List<File> dir = openDirectoryDialogInternal(title, initDirectory);
							if (dir != null) {
								rsl.addAll(dir);
							}
						} else {
							String finalTitle = title;
							if (title == null) {
								finalTitle = "Select a file";
							}

							FileChooser fc = new FileChooser();
							File finalInitDir = initDirectory;
							if (initDirectory == null) {
								finalInitDir = new File(prefs.get("FC_LAST_DIR", ""));
							}
							if (finalInitDir.exists()) {
								fc.setInitialDirectory(finalInitDir);
							}
							fc.setInitialFileName(initFileName);
							fc.setTitle(finalTitle);
							int realFilterCount = 0;
							if (extensionFilters != null) {
								if (extensionFilters.length > 1 && !isSave) {
									ExtensionFilter combined = ExtensionFilter.combine(extensionFilters);
									fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(combined.formatName, combined.filters));
								}

								for (ExtensionFilter f : extensionFilters) {
									if (f != null) {
										fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(f.formatName, f.filters));
										realFilterCount++;
									}
								}
							}
							if (!isSave || extensionFilters == null || realFilterCount == 0) {
								fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(CommonExtensionFilters.ALL.formatName, CommonExtensionFilters.ALL.filters));
							}

							if (isSave) {
								File r = fc.showSaveDialog(null);
								if (r != null) {
									FileChooser.ExtensionFilter ef = fc.getSelectedExtensionFilter();
									String ext = ef.getExtensions().get(0).replaceAll("\\*", "");
									if (!r.getName().endsWith(ext)) {
										r = new File(r.getAbsolutePath() + ext);
									}
									rsl.add(r);
								}
							} else {
								if (allowMultiple) {
									List<File> files = fc.showOpenMultipleDialog(null);
									if (files != null) {
										rsl.addAll(files);
									}
								} else {
									File r = fc.showOpenDialog(null);
									if (r != null) {
										rsl.add(r);
									}
								}
							}

							if (!rsl.isEmpty()) {
								if (isDirectory) {
									prefs.put("FC_LAST_DIR", rsl.get(0).getAbsolutePath());

								} else {
									prefs.put("FC_LAST_DIR", rsl.get(0).getParentFile().getAbsolutePath());
								}
							}
						}
						JFX_LOCK.notifyAll();
					}
				}
			});

			try {
				JFX_LOCK.wait();
			} catch (InterruptedException ex) {
				Logger.getLogger(CMFileDialog.class.getName()).log(Level.SEVERE, null, ex);
			}
			return rsl;
		}
	}

	private static List<File> openDirectoryDialogInternal(String title, File initDirectory) {
		if (title == null) {
			title = "Select a directory";
		}
		DirectoryChooser dc = new DirectoryChooser();
		if (initDirectory == null) {
			initDirectory = new File(prefs.get("DC_LAST_DIR", ""));
		}
		if (initDirectory.exists()) {
			dc.setInitialDirectory(initDirectory);
		}
		dc.setTitle(title);
		File rsl = dc.showDialog(null);
		if (rsl != null) {
			prefs.put("DC_LAST_DIR", rsl.getAbsolutePath());
			return ArraysEx.asList(rsl);
		} else {
			return null;
		}
	}
}
