package xstandard.gui.file;

import com.sun.javafx.application.PlatformImpl;
import xstandard.XStandardPrefs;
import xstandard.fs.accessors.DiskFile;
import xstandard.util.ArraysEx;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import xstandard.fs.FSUtil;
import xstandard.text.FormattingUtils;

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

	private static Boolean canUseJFX;

	private static boolean tryInitJFX() {
		if (canUseJFX != null) {
			return canUseJFX;
		}

		try {
			canUseJFX = true;
			//We have to use PlatformImpl because of JRE 1.8 shipping with JavaFX which doesn't have the method in Platform
			//Using com.sun.* classes is generally a bad idea, but since we ship FrankenJFX to support newer versions of JDK, we can be sure it doesn't get changed
			PlatformImpl.startup(new Runnable() {
				@Override
				public void run() {
				}
			});
		} catch (Throwable e) {
			e.printStackTrace();
			canUseJFX = false;
		}

		return canUseJFX;
	}

	private static final Object JFX_LOCK = new Object();

	public static List<DiskFile> openFileDialog(String title, boolean isSave, boolean isDirectory, boolean allowMultiple, DiskFile initDirectory, String initFileName, ExtensionFilter... extensionFilters) {
		if (!tryInitJFX()) {
			return openFallbackFileDialog(title, isSave, isDirectory, allowMultiple, initDirectory, initFileName, extensionFilters);
		}
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
							rsl.addAll(openFileDialogInternal(title, isSave, allowMultiple, initDirectory, initFileName, extensionFilters));
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

	private static List<DiskFile> openFileDialogInternal(String title, boolean isSave, boolean allowMultiple, DiskFile initDirectory, String initFileName, ExtensionFilter... extensionFilters) {
		FileChooser fc = new FileChooser();
		File finalInitDir = resolveFCInitDirectory(initDirectory);
		if (finalInitDir != null) {
			fc.setInitialDirectory(finalInitDir);
		}
		fc.setInitialFileName(initFileName);
		fc.setTitle(resolveFCTitle(title));

		Map<ExtensionFilter, FileChooser.ExtensionFilter> filterMap = new HashMap<>();
		Map<FileChooser.ExtensionFilter, ExtensionFilter> filterMapInv = new HashMap<>();
		List<FileChooser.ExtensionFilter> nativeFilters = new ArrayList<>();

		if (extensionFilters != null) {
			extensionFilters = filterNullExtensionFilters(extensionFilters);

			if (extensionFilters.length > 1 && !isSave) {
				nativeFilters.add(filterToJFX(ExtensionFilter.combine(extensionFilters)));
			}

			for (ExtensionFilter f : extensionFilters) {
				if (f != null) {
					FileChooser.ExtensionFilter nativeFilter = filterToJFX(f);
					filterMap.put(f, nativeFilter);
					filterMapInv.put(nativeFilter, f);
					nativeFilters.add(nativeFilter);
				}
			}
		}
		if (!isSave || extensionFilters == null || extensionFilters.length == 0) {
			nativeFilters.add(filterToJFX(CommonExtensionFilters.ALL));
		}
		fc.getExtensionFilters().addAll(nativeFilters);

		List<DiskFile> rsl = new ArrayList<>();

		if (isSave) {
			fc.setSelectedExtensionFilter(filterMap.get(getInitialSelectedFilter(extensionFilters)));
			File r = fc.showSaveDialog(null);
			if (r != null) {
				FileChooser.ExtensionFilter ef = fc.getSelectedExtensionFilter();
				ExtensionFilter selectedXstd = filterMapInv.get(ef);
				if (selectedXstd != null) {
					saveSelectedExtensionFilter(extensionFilters, selectedXstd);
				}
				rsl.addAll(applyFileFilterOnSave(Arrays.asList(new DiskFile(r)), selectedXstd));
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
			persistFCLastDirectory(rsl.get(0).getFile().getParentFile());
		}

		return rsl;
	}

	private static List<DiskFile> openDirectoryDialogInternal(String title, DiskFile initDirectory) {
		DirectoryChooser dc = new DirectoryChooser();
		File initDirFile = resolveDCInitDirectory(initDirectory);
		if (initDirFile != null) {
			dc.setInitialDirectory(initDirFile);
		}
		dc.setTitle(resolveDCTitle(title));
		File rsl = dc.showDialog(null);
		if (rsl != null) {
			persistDCLastDirectory(rsl);
			return ArraysEx.asList(new DiskFile(rsl));
		} else {
			return null;
		}
	}

	public static List<DiskFile> openFallbackFileDialog(String title, boolean isSave, boolean isDirectory, boolean allowMultiple, DiskFile initDirectory, String initFileName, ExtensionFilter... extensionFilters) {
		if (isDirectory) {
			return openFallbackDirectoryDialogInternal(title, initDirectory);
		} else {
			return openFallbackFileDialogInternal(title, isSave, allowMultiple, initDirectory, initFileName, extensionFilters);
		}
	}

	private static List<DiskFile> openFallbackFileDialogInternal(String title, boolean isSave, boolean allowMultiple, DiskFile initDirectory, String initFileName, ExtensionFilter... extensionFilters) {
		JFileChooser chooser = new JFileChooser(resolveFCInitDirectory(initDirectory));
		chooser.setDialogTitle(resolveFCTitle(title));
		chooser.setMultiSelectionEnabled(allowMultiple);
		if (initFileName != null) {
			chooser.setSelectedFile(new File(chooser.getCurrentDirectory(), initFileName));
		}
		chooser.setAcceptAllFileFilterUsed(false); //may be added later manually
		Map<ExtensionFilter, FileFilter> filterMap = new HashMap<>();
		Map<FileFilter, ExtensionFilter> filterMapInv = new HashMap<>();
		List<FileFilter> nativeFilters = new ArrayList<>();

		if (extensionFilters != null) {
			extensionFilters = filterNullExtensionFilters(extensionFilters);
			if (extensionFilters.length > 1 && !isSave) {
				nativeFilters.add(filterToSwing(ExtensionFilter.combine(extensionFilters), 100));
			}

			for (ExtensionFilter f : extensionFilters) {
				if (f != null) {
					FileNameExtensionFilter nativeFilter = filterToSwing(f);
					filterMap.put(f, nativeFilter);
					filterMapInv.put(nativeFilter, f);
					nativeFilters.add(nativeFilter);
				}
			}
		}
		for (FileFilter filter : nativeFilters) {
			chooser.addChoosableFileFilter(filter);
		}
		if (!isSave || extensionFilters == null || extensionFilters.length == 0) {
			chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
		}

		if (isSave) {
			chooser.setFileFilter(filterMap.get(getInitialSelectedFilter(extensionFilters)));
		}

		int result = isSave ? chooser.showSaveDialog(null) : chooser.showOpenDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
			List<DiskFile> out = new ArrayList<>();
			if (allowMultiple) {
				File[] files = chooser.getSelectedFiles();
				if (files != null) {
					for (File f : files) {
						out.add(new DiskFile(f));
					}
				}
			} else {
				File file = chooser.getSelectedFile();
				if (file != null) {
					out.add(new DiskFile(file));
				}
			}

			if (isSave) {
				FileFilter ef = chooser.getFileFilter();
				ExtensionFilter selectedXstd = filterMapInv.get(ef);
				if (selectedXstd != null) {
					saveSelectedExtensionFilter(extensionFilters, selectedXstd);
				}
				out = applyFileFilterOnSave(out, selectedXstd);
			}

			if (!out.isEmpty()) {
				persistFCLastDirectory(out.get(0).getFile().getParentFile());
			}

			return out;
		} else {
			return Collections.emptyList();
		}
	}
	
	private static FileNameExtensionFilter filterToSwing(ExtensionFilter filter) {
		return filterToSwing(filter, Integer.MAX_VALUE);
	}
	
	private static FileNameExtensionFilter filterToSwing(ExtensionFilter filter, int maxDescLength) {
		String text = filter.getDisplayText();
		if (text.length() > maxDescLength) {
			text = FormattingUtils.ellipsize(text, maxDescLength);
		}
		
		//remove dot from extensions, which JFC does not want
		
		return new FileNameExtensionFilter(text, filter.getExtensions().stream().map(e -> e.substring(1)).toArray(String[]::new));
	}
	
	private static FileChooser.ExtensionFilter filterToJFX(ExtensionFilter filter) {
		return new FileChooser.ExtensionFilter(filter.formatName, filter.filters);
	}

	private static List<DiskFile> applyFileFilterOnSave(List<DiskFile> files, ExtensionFilter selectedFilter) {
		List<String> saveExtensions = new ArrayList<>();
		if (selectedFilter != null) {
			for (String ext : selectedFilter.getExtensions()) {
				saveExtensions.add(ext.replace(".", ""));
			}
		}

		List<DiskFile> out = new ArrayList<>();
		for (DiskFile df : files) {
			if (!saveExtensions.isEmpty() && !saveExtensions.contains(FSUtil.getFileExtension(df.getName()))) {
				df = new DiskFile(df.getPath() + saveExtensions.get(0));
			}
			out.add(df);
		}
		return out;
	}

	private static List<DiskFile> openFallbackDirectoryDialogInternal(String title, DiskFile initDirectory) {
		JFileChooser chooser = new JFileChooser(resolveDCInitDirectory(initDirectory));
		chooser.setDialogTitle(resolveDCTitle(title));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int result = chooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			File chosenFile = chooser.getSelectedFile();
			if (chosenFile != null) {
				persistDCLastDirectory(chosenFile);
				return ArraysEx.asList(new DiskFile(chosenFile));
			}
		}
		return null;
	}

	private static String resolveFCTitle(String title) {
		return title != null ? title : "Select a file";
	}

	private static String resolveDCTitle(String title) {
		return title != null ? title : "Select a directory";
	}

	private static File resolveFCInitDirectory(DiskFile initDirectory) {
		return resolveInitDirectory(initDirectory, "FC_LAST_DIR");
	}

	private static File resolveDCInitDirectory(DiskFile initDirectory) {
		return resolveInitDirectory(initDirectory, "DC_LAST_DIR");
	}

	private static File resolveInitDirectory(DiskFile initDirectory, String lastRegKey) {
		if (initDirectory == null) {
			initDirectory = new DiskFile(prefs.get(lastRegKey, ""));
		}
		if (initDirectory.exists()) {
			return initDirectory.getFile();
		}
		return null;
	}

	private static ExtensionFilter[] filterNullExtensionFilters(ExtensionFilter[] filters) {
		return Arrays.stream(filters).filter(Objects::nonNull).toArray(ExtensionFilter[]::new);
	}

	private static void persistFCLastDirectory(File chosen) {
		prefs.put("FC_LAST_DIR", chosen.getAbsolutePath());
	}

	private static void persistDCLastDirectory(File chosen) {
		prefs.put("DC_LAST_DIR", chosen.getAbsolutePath());
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
