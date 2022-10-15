package xstandard.gui.file;

import xstandard.fs.FSFile;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ExtensionFilter {

	public final String formatName;
	public final String[] filters;

	public ExtensionFilter(String fmtName, String... filters) {
		formatName = fmtName;
		this.filters = filters;
	}

	public static ExtensionFilter findByFileName(File f, ExtensionFilter... filters) {
		return findByFileName(f == null ? null : f.getPath(), filters);
	}
	
	public static ExtensionFilter findByFileName(FSFile f, ExtensionFilter... filters) {
		return findByFileName(f == null ? null : f.getPath(), filters);
	}
	
	public static ExtensionFilter findByFileName(String path, ExtensionFilter... filters) {
		if (filters == null || path == null) {
			return null;
		}
		for (ExtensionFilter flt : filters) {
			if (flt.accepts(path)) {
				return flt;
			}
		}
		return null;
	}

	public String getDisplayText() {
		StringBuilder sb = new StringBuilder(formatName);
		sb.append(" ");
		sb.append("(");
		for (int i = 0; i < filters.length; i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(filters[i]);
		}
		sb.append(")");
		return sb.toString();
	}

	public String getPrimaryExtension() {
		return getExtensions().get(0);
	}

	public List<String> getExtensions() {
		List<String> extensions = new ArrayList<>();
		for (String f : filters) {
			int dotIndex = f.indexOf('.');
			if (dotIndex != -1) {
				extensions.add(f.substring(dotIndex));
			}
			else {
				extensions.add("");
			}
		}
		return extensions;
	}

	public boolean accepts(String filePath) {
		for (String flt : filters) {
			if (filePath.matches(createRegexFromGlob(flt))) {
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
	
	@Override
	public String toString() {
		return getDisplayText();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && obj instanceof ExtensionFilter) {
			final ExtensionFilter other = (ExtensionFilter) obj;
			return Objects.equals(other.formatName, this.formatName) && Arrays.equals(filters, other.filters);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + Objects.hashCode(this.formatName);
		hash = 37 * hash + Arrays.deepHashCode(this.filters);
		return hash;
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
