package ctrmap.stdlib.io.structs;

import ctrmap.stdlib.io.iface.SeekableDataOutput;
import ctrmap.stdlib.io.util.StringUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringTable {
	private Map<String, List<TemporaryOffset>> registOffsets = new HashMap<>();
	
	private SeekableDataOutput out;
	
	public StringTable(SeekableDataOutput out){
		this.out = out;
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
			StringUtils.writeString(out, e.getKey());
		}
	}
}
