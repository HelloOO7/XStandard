package ctrmap.stdlib.formats.rpm;

import ctrmap.stdlib.io.util.StringIO;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class RPMSymbol {

	public String name;
	public RPMSymbolType type;
	public RPMSymbolAddress address;
	public int size;

	public RPMSymbol() {

	}

	public RPMSymbol(RPM rpm, RPMSymbol source) {
		name = source.name;
		type = source.type;
		address = new RPMSymbolAddress(rpm, source.address);
		size = source.size;
	}
	
	public RPMSymbol(RPM rpm, String name, RPMSymbolType type, RPMSymbolAddress addr) {
		this.name = name;
		this.type = type;
		this.address = addr;
	}

	public RPMSymbol(RPM rpm, DataInput in, int version) throws IOException {
		name = StringIO.readString(in);
		if (name.isEmpty()){
			name = null;
		}
		type = RPMSymbolType.values()[in.readUnsignedByte()];
		address = new RPMSymbolAddress(rpm, in);
		if (version >= RPMRevisions.REV_SYMBOL_LENGTH) {
			size = in.readInt();
		}
	}
	
	public int getByteSize(){
		return (name == null ? 0 : name.length()) + 1 + 1 + 4 + 4;
		//name + term + type + addr + size
	}

	public void write(DataOutput out) throws IOException {
		StringIO.writeString(out, name);
		out.write(type.ordinal());
		address.write(out);
		out.writeInt(size);
	}
}
