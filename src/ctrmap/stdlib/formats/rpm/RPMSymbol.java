package ctrmap.stdlib.formats.rpm;

import ctrmap.stdlib.io.util.StringUtils;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class RPMSymbol {
	public String name;
	public RPMSymbolType type;
	public RPMSymbolAddress address;
	
	public RPMSymbol(){
		
	}
	
	public RPMSymbol(RPM rpm, DataInput in) throws IOException {
		name = StringUtils.readString(in);
		type = RPMSymbolType.values()[in.readUnsignedByte()];
		address = new RPMSymbolAddress(rpm, in);
	}
	
	public void write(DataOutput out) throws IOException {
		StringUtils.writeString(out, name);
		out.write(type.ordinal());
		address.write(out);
	}
}
