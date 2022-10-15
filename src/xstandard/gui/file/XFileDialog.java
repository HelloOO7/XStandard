package xstandard.gui.file;

import com.sun.javafx.application.PlatformImpl;
import xstandard.XStandardPrefs;
import xstandard.fs.accessors.DiskFile;
import xstandard.util.ArraysEx;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import xstandard.fs.FSUtil;

public class XFileDialog {

	private static Preferences prefs = XStandardPrefs.node("XFileDialog");

	public static DiskFile openDirectoryDialog() {
		return openDirectoryDialog((String) null);
	}

	public static DiskFile openDirectoryDialog(String title) {
		return openDirectoryDialog(title, null);
	}

	public static DiskFile openDirectoryDialog(DiskFile initDirectory) {
		return openDirectoryDialog(null, initDirectory);
	}

	public static DiskFile openDirectoryDialog(String title, DiskFile initDirectory) {
		List<DiskFile> r = openFileDialog(title, false, true, false, initDirectory, null, new ExtensionFilter[0]);
		if (r.isEmpty()) {
			return null;
		}
		return r.get(0);
	}

	public static DiskFile openSaveFileDialog() {
		return openSaveFileDialog((String) null);
	}

	public static DiskFile openSaveFileDialog(String title) {
		return openSaveFileDialog(title, (String) null);
	}

	public static DiskFile openSaveFileDialog(String title, String defaultName) {
		return openFileDialog(title, true, null, defaultName, new ExtensionFilter[0]);
	}

	public static DiskFile openSaveFileDialog(ExtensionFilter... filters) {
		return openSaveFileDialog(null, filters);
	}

	public static DiskFile openSaveFileDialog(String title, ExtensionFilter... filters) {
		return openSaveFileDialog(title, null, filters);
	}

	public static DiskFile openSaveFileDialog(String title, String defaultName, ExtensionFilter... filters) {
		return openFileDialog(title, true, null, defaultName, filters);
	}

	public static DiskFile openFileDialog() {
		return openFileDialog((String) null);
	}

	public static List<DiskFile> openMultiFileDialog() {
		return openMultiFileDialog(new ExtensionFilter[0]);
	}

	public static List<DiskFile> openMultiFileDialog(ExtensionFilter... filters) {
		return openFileDialog(null, false, false, true, null, null, filters);
	}

	public static DiskFile openFileDialog(String title) {
		return openFileDialog(title, false, null, null, new ExtensionFilter[0]);
	}

	public static DiskFile openFileDialog(ExtensionFilter... filters) {
		return openFileDialog(null, false, null, null, filters);
	}

	public static DiskFile openFileDialog(String title, ExtensionFilter... filters) {
		return openFileDialog(title, false, null, null, filters);
	}

	public static DiskFile openFileDialog(String title, boolean isSave, DiskFile initDirectory, String initFileName, ExtensionFilter... extensionFilters) {
		List<DiskFile> r = openFileDialog(title, isSave, false, false, initDirectory, initFileName, extensionFilters);
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

	public static List<DiskFile> openFileDialog(String title, boolean isSave, boolean isDirectory, boolean allowMultiple, DiskFile initDirectory, String initFileName, ExtensionFilter... extensionFilters) {
		tryInitJFX();
		synchronized (JFX_LOCK) {
			final List<DiskFile> rsl = new ArrayList<>();

			//We could use PlatformImpl from the newer JFX SDK which allows dynamic startup/runlater, but this would break compat with JDK 8
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					synchronized (JFX_LOCK) {
						if (isDirectory) {
							List<DiskFile> dir = openDirectoryDialogInternal(title, initDirectory);
							if (dir != null) {
								rsl.addAll(dir);
							}
						} else {
							String finalTitle = title;
							if (title == null) {
								finalTitle = "Select a file";
							}

							FileChooser fc = new FileChooser();
							File finalInitDir;
							if (initDirectory == null) {
								finalInitDir = new File(prefs.get("FC_LAST_DIR", ""));
							} else {
								finalInitDir = initDirectory.getFile();
							}
							if (finalInitDir.exists()) {
								fc.setInitialDirectory(finalInitDir);
							}
							fc.setInitialFileName(initFileName);
							fc.setTitle(finalTitle);
							
							Map<ExtensionFilter, FileChooser.ExtensionFilter> filterMap = new HashMap<>();
							Map<FileChooser.ExtensionFilter, ExtensionFilter> filterMapInv = new HashMap<>();
							List<FileChooser.ExtensionFilter> nativeFilters = new ArrayList<>();
							
							int realFilterCount = 0;
							if (extensionFilters != null) {
								if (extensionFilters.length > 1 && !isSave) {
									ExtensionFilter combined = ExtensionFilter.combine(extensionFilters);
									nativeFilters.add(new FileChooser.ExtensionFilter(combined.formatName, combined.filters));
								}

								for (ExtensionFilter f : extensionFilters) {
									if (f != null) {
										FileChooser.ExtensionFilter nativeFilter = new FileChooser.ExtensionFilter(f.formatName, f.filters);
										filterMap.put(f, nativeFilter);
										filterMapInv.put(nativeFilter, f);
										nativeFilters.add(nativeFilter);
										realFilterCount++;
									}
								}
							}
							if (!isSave || extensionFilters == null || realFilterCount == 0) {
								nativeFilters.add(new FileChooser.ExtensionFilter(CommonExtensionFilters.ALL.formatName, CommonExtensionFilters.ALL.filters));
							}
							fc.getExtensionFilters().addAll(nativeFilters);

							if (isSave) {
								fc.setSelectedExtensionFilter(filterMap.get(getInitialSelectedFilter(extensionFilters)));
								File r = fc.showSaveDialog(null);
								if (r != null) {
									FileChooser.ExtensionFilter ef = fc.getSelectedExtensionFilter();
									ExtensionFilter selectedXstd = filterMapInv.get(ef);
									if (selectedXstd != null) {
										saveSelectedExtensionFilter(extensionFilters, selectedXstd);
									}
									List<String> saveExtensions = new ArrayList<>();
									for (String ext : ef.getExtensions()) {
										saveExtensions.add(ext.replace("*.", ""));
									}
									DiskFile df;
									if (!saveExtensions.isEmpty() && !saveExtensions.contains(FSUtil.getFileExtension(r.getName()))) {
										df = new DiskFile(r.getAbsolutePath() + saveExtensions.get(0));
									} else {
										df = new DiskFile(r);
									}
									rsl.add(df);
								}
							} else {
								if (allowMultiple) {
									List<File> files = fc.showOpenMultipleDialog(null);
									if (files != null) {
										for (File f : files) {
											rsl.add(new DiskFile(f));
										}
									}
								} else {
									File r = fc.showOpenDialog(null);
									if (r != null) {
										rsl.add(new DiskFile(r));
									}
								}
							}

							if (!rsl.isEmpty()) {
								if (isDirectory) {
									prefs.put("FC_LAST_DIR", rsl.get(0).getFile().getAbsolutePath());

								} else {
									prefs.put("FC_LAST_DIR", rsl.get(0).getFile().getParentFile().getAbsolutePath());
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
				Logger.getLogger(XFileDialog.class.getName()).log(Level.SEVERE, null, ex);
			}
			return rsl;
		}
	}

	private static List<DiskFile> openDirectoryDialogInternal(String title, DiskFile initDirectory) {
		if (title == null) {
			title = "Select a directory";
		}
		DirectoryChooser dc = new DirectoryChooser();
		if (initDirectory == null) {
			initDirectory = new DiskFile(prefs.get("DC_LAST_DIR", ""));
		}
		if (initDirectory.exists()) {
			dc.setInitialDirectory(initDirectory.getFile());
		}
		dc.setTitle(title);
		File rsl = dc.showDialog(null);
		if (rsl != null) {
			prefs.put("DC_LAST_DIR", rsl.getAbsolutePath());
			return ArraysEx.asList(new DiskFile(rsl));
		} else {
			return null;
		}
	}

	private static void saveSelectedExtensionFilter(ExtensionFilter[] filters, ExtensionFilter filter) {
		int indexOf = 0;
		for (ExtensionFilter ef : filters) {
			if (ef == filter) {
				break;
			}
			indexOf++;
		}
		prefs.putInt("filters_" + makeFilterHash(filters), indexOf);
	}

	private static ExtensionFilter getInitialSelectedFilter(ExtensionFilter[] filters) {
		if (filters.length == 0) {
			return null;
		}
		int index = prefs.getInt("filters_" + makeFilterHash(filters), 0);
		if (index >= 0 && index < filters.length) {
			return filters[index];
		}
		return null;
	}

	private static int makeFilterHash(ExtensionFilter[] filters) {
		int hash = 7;
		for (ExtensionFilter f : filters) {
			hash = 37 * hash + f.hashCode();
		}
		return hash;
	}
}
