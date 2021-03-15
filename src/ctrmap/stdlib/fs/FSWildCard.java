package ctrmap.stdlib.fs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum FSWildCard {
	ROMFS("romfs", "RomFS", "romfs"),
	EXEFS("exefs", "ExeFS", "exefs"),
	CODE_BIN("codebin", "code.bin", ".code.bin", "mario.exe"),
	EXHEADER("exheader", "ExHeader.bin", "exheader.bin", "exheader.exh", "ExHeader.exh", "exh.bin")
	;
	public final String ddotId;
	public final String[] options;
		
	private FSWildCard(String ddotId, String... possibleOptions){
		this.ddotId = ddotId;
		this.options = possibleOptions;
	}
	
	public String getFullDDotId(){
		return ":" + ddotId + ":";
	}
	
	public String getFirstOption(){
		return options[0];
	}
	
	public String[] getOtherOptions(){
		return Arrays.copyOfRange(options, 1, options.length);
	}
	
	public static FSWildCard getWCForString(String str){
		for (FSWildCard wc : values()){
			for (String opt : wc.options){
				if (str.equals(opt)){
					return wc;
				}
			}
		}
		return null;
	}
	
	public static String getWildCardedPath(String originalPath){
		for (FSWildCard wc : values()){
			for (String opt : wc.options){
				String ddot = wc.getFullDDotId();
				if (originalPath.contains(opt) && !originalPath.contains(ddot)){
					//This could be considered flawed when the same reference is used multiple times in the same path, however, this should never be the case
					originalPath = originalPath.replaceAll(opt, ddot);
				}
			}
		}
		return originalPath;
	}
	
	public static String getNonWildCardedPathByActual(String wildCardedPath, String referencePath){
		String[] pathElems = referencePath.split("/");
		Map<FSWildCard, String> selectedOptions = new HashMap<>();
		for (String pe : pathElems){
			FSWildCard wc = getWCForString(pe);
			if (wc != null){
				selectedOptions.put(wc, pe);
			}
		}
		
		for (FSWildCard wc : values()){
			String ddot = wc.getFullDDotId();
			if (wildCardedPath.contains(ddot)){
				String repl = selectedOptions.get(wc);
				if (repl == null){
					repl = wc.getFirstOption();
				}
				wildCardedPath = wildCardedPath.replaceAll(ddot, repl);
			}
		}
		
		return wildCardedPath;
	}
}
