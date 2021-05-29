package ctrmap.stdlib.formats.rpm;

import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import ctrmap.stdlib.io.structs.StringTable;
import ctrmap.stdlib.io.util.StringIO;
import ctrmap.stdlib.util.ArraysEx;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class RPMRelocationTarget {
	public static final String MODULE_BASE = "base";
	
	public String module = MODULE_BASE;
	public int address;
	
	public RPMRelocationTarget(DataIOStream in) throws IOException {
		address = in.readInt();
		boolean hasModule = ((address >> 31) & 1) != 0;
		address &= 0x7FFFFFFF;
		
		if (hasModule){
			module = StringIO.readStringWithAddress(in);
		}
	}
	
	public RPMRelocationTarget(int address){
		this.address = address;
	}
	
	public RPMRelocationTarget(int address, String module){
		this(address);
		this.module = module;
	}
	
	public RPMRelocationTarget(RPMRelocationTarget tgt){
		address = tgt.address;
		module = tgt.module;
	}
	
	public void addStrings(List<String> l){
		ArraysEx.addIfNotNullOrContains(l, module);
	}
	
	public int getAddrHWordAligned(){
		return address - address % 2;
	}
	
	public int getSize(){
		if (isInternal()){
			return 4;
		}
		else {
			return 8;
		}
	}
	
	public boolean isInternal(){
		return Objects.equals(module, MODULE_BASE);
	}
	
	public boolean isExternal(){
		return !isInternal();
	}
	
	public void write(DataIOStream out, StringTable strtbl) throws IOException {
		if (isInternal()){
			out.writeInt(address);
		}
		else {
			out.writeInt(address | (1 << 31));
			strtbl.putStringOffset(module);
		}
	}
}
