package ctrmap.stdlib.formats.msgtxt;

import ctrmap.stdlib.fs.FSFile;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MsgTxt {

	protected Map<String, String> entries = new HashMap<>();

	public MsgTxt(){
		
	}
	
	public MsgTxt(File f){
		try {
			loadFromInputStream(new FileInputStream(f));
		} catch (FileNotFoundException ex) {
			Logger.getLogger(MsgTxt.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public MsgTxt(FSFile fsf) {
		loadFromInputStream(fsf.getNativeInputStream());
	}
	
	public MsgTxt(InputStream strm) {
		loadFromInputStream(strm);
	}
	
	private void loadFromInputStream(InputStream strm){
		Scanner s = new Scanner(strm);
		while (s.hasNextLine()) {
			String line = s.nextLine();
			if (line.contains(":")) {
				int doubledot = line.indexOf(":");
				entries.put(line.substring(0, doubledot), line.substring(doubledot + 1).replace("\\n", "\n"));
			}
		}
		s.close();
	}
	
	public Map<String, String> getMap(){
		return entries;
	}

	public boolean hasLines(String... keys){
		for (String key : keys){
			if (!entries.containsKey(key)){
				return false;
			}
		}
		return true;
	}
	
	public String getLineForName(String name) {
		return entries.get(name);
	}
	
	public void putLine(String name, String text){
		entries.put(name, text);
	}
	
	public void writeToFile(File f){
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(f));
			
			for (Map.Entry<String, String> e : entries.entrySet()){
				w.write(e.getKey() + ":" + e.getValue().replaceAll("\n", "\\n"));
				w.newLine();
			}
			
			w.close();
		} catch (IOException ex) {
			Logger.getLogger(MsgTxt.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
