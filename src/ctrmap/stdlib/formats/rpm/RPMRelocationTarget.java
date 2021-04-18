package ctrmap.stdlib.formats.rpm;

import ctrmap.stdlib.io.iface.SeekableDataInput;
import ctrmap.stdlib.io.iface.SeekableDataOutput;
import ctrmap.stdlib.io.structs.StringTable;
import ctrmap.stdlib.io.util.StringUtils;
import java.io.IOException;
import java.util.Objects;

public class RPMRelocationTarget {
	public static final String MODULE_BASE = "base";
	
	public String module = MODULE_BASE;
	public int address;
	
	public RPMRelocationTarget(SeekableDataInput in) throws IOException {
		address = in.readInt();
		boolean hasModule = ((address >> 31) & 1) != 0;
		address &= 0xFEFFFFFF;
		
		if (hasModule){
			module = StringUtils.readStringWithAddress(in);
		}
	}
	
	public RPMRelocationTarget(int address){
		this.address = address;
	}
	
	public RPMRelocationTarget(int address, String module){
		this(address);
		this.module = module;
	}
	
	public boolean isInternal(){
		return Objects.equals(module, MODULE_BASE);
	}
	
	public boolean isExternal(){
		return !isInternal();
	}
	
	public void write(SeekableDataOutput out, StringTable strtbl) throws IOException {
		if (isInternal()){
			out.writeInt(address);
		}
		else {
			out.writeInt(address | (1 << 31));
			strtbl.putStringOffset(module);
		}
	}
}
