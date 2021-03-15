package ctrmap.stdlib.gui.file;

import java.util.ArrayList;
import java.util.List;

public class ExtensionFilter {
	public final String formatName;
	public final String[] filters;
	
	public ExtensionFilter(String fmtName, String... filter){
		formatName = fmtName;
		this.filters = filter;
	}
	
	public String getPrimaryExtension(){
		return getExtensions().get(0);
	}
	
	public List<String> getExtensions(){
		List<String> extensions = new ArrayList<>();
		for (String f : filters){
			extensions.add(f.substring(f.indexOf(".")));
		}
		return extensions;
	}
	
	public static ExtensionFilter combine(ExtensionFilter... filters){
		List<String> extensions = new ArrayList<>();
		for (ExtensionFilter f : filters){
			for (String ext : f.filters){
				extensions.add(ext);
			}
		}
		ExtensionFilter f = new ExtensionFilter("Supported files", extensions.toArray(new String[extensions.size()]));
		return f;
	}
}
