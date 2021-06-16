package ctrmap.stdlib.io.structs;

import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import ctrmap.stdlib.io.util.StringIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringTable {
	private Map<String, List<TemporaryOffset>> registOffsets = new HashMap<>();
	
	private Map<String, Integer> nonRegistOffsets = new HashMap<>();
	
	private DataIOStream out;
	
	public StringTable(DataIOStream out){
		this.out = out;
	}
	
	public void putString(String str) {
		nonRegistOffsets.put(str, -1);
	}
	
	public int getNonRegistStringAddr(String str){
		return nonRegistOffsets.get(str);
	}
	
	public void putStringOffset(String str) throws IOException {
		List<TemporaryOffset> l = registOffsets.get(str);
		if (l == null){
			l = new ArrayList<>();
			registOffsets.put(str, l);
		}
		l.add(new TemporaryOffset(out));
	}
	
	public void writeTable() throws IOException {
		for (Map.Entry<String, List<TemporaryOffset>> e : registOffsets.entrySet()){
			for (TemporaryOffset o : e.getValue()){
				o.setHere();
			}
			StringIO.writeString(out, e.getKey());
		}
		registOffsets.clear();
		
		for (Map.Entry<String, Integer> e : nonRegistOffsets.entrySet()){
			if (e.getValue() == -1){
				e.setValue(out.getPosition());
				out.writeString(e.getKey());
			}
		}
	}
}
