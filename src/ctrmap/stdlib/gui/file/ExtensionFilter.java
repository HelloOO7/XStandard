package ctrmap.stdlib.gui.file;

import ctrmap.stdlib.fs.FSUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExtensionFilter {

	public final String formatName;
	public final String[] filters;

	public ExtensionFilter(String fmtName, String... filter) {
		formatName = fmtName;
		this.filters = filter;
	}
	
	public static ExtensionFilter findByFileName(File f, ExtensionFilter... filters){
		if (filters == null){
			return null;
		}
		String ext = FSUtil.getFileExtensionWithDot(f.getName());
		for (ExtensionFilter flt : filters){
			if (flt.getExtensions().contains(ext)){
				return flt;
			}
		}
		return null;
	}

	public String getPrimaryExtension() {
		return getExtensions().get(0);
	}

	public List<String> getExtensions() {
		List<String> extensions = new ArrayList<>();
		for (String f : filters) {
			extensions.add(f.substring(f.indexOf(".")));
		}
		return extensions;
	}
	
	public boolean accepts(String filePath){
		for (String flt : filters){
			if (filePath.matches(createRegexFromGlob(flt))){
				return true;
			}
		}
		return false;
	}

	public static ExtensionFilter combine(ExtensionFilter... filters) {
		List<String> extensions = new ArrayList<>();
		for (ExtensionFilter f : filters) {
			for (String ext : f.filters) {
				extensions.add(ext);
			}
		}
		ExtensionFilter f = new ExtensionFilter("Supported files", extensions.toArray(new String[extensions.size()]));
		return f;
	}

	/*
	https://stackoverflow.com/questions/45321050/java-string-matching-with-wildcards
	*/
	private static String createRegexFromGlob(String glob) {
		StringBuilder out = new StringBuilder("^");
		for (int i = 0; i < glob.length(); ++i) {
			final char c = glob.charAt(i);
			switch (c) {
				case '*':
					out.append(".*");
					break;
				case '?':
					out.append('.');
					break;
				case '.':
					out.append("\\.");
					break;
				case '\\':
					out.append("\\\\");
					break;
				default:
					out.append(c);
			}
		}
		out.append('$');
		return out.toString();
	}
}
