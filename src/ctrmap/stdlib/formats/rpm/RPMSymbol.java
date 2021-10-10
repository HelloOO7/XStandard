package ctrmap.stdlib.formats.rpm;

import ctrmap.stdlib.io.base.iface.DataOutputEx;
import ctrmap.stdlib.io.structs.StringTable;
import ctrmap.stdlib.math.BitMath;
import ctrmap.stdlib.util.ArraysEx;
import java.io.IOException;
import java.util.List;

/**
 * A code symbol, RPM flavour.
 */
public class RPMSymbol {

	public static final int RPM_SYMATTR_EXPORT = 1;

	public String name;
	public RPMSymbolType type;
	public int attributes = 0;
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

	RPMSymbol(RPM rpm, RPMReader in, int version) throws IOException {
		if (version >= RPMRevisions.REV_SYMBSTR_TABLE) {
			name = in.readStringWithAddress();
		} else {
			name = in.readString();
		}
		if (name == null || name.isEmpty()) {
			name = null;
		}
		int typeCfg = in.readUnsignedByte();
		type = RPMSymbolType.values()[typeCfg & 0b111];
		attributes = typeCfg >> 3;
		address = new RPMSymbolAddress(rpm, in);
		if (version >= RPMRevisions.REV_SYMBOL_LENGTH) {
			if (version >= RPMRevisions.REV_SMALL_SYMBOLS) {
				size = in.readUnsignedShort();
			} else {
				size = in.readInt();
			}
		}
	}

	public boolean isExportSymbol() {
		return (attributes & RPM_SYMATTR_EXPORT) != 0;
	}

	public void setIsExportSymbol(boolean value) {
		attributes = BitMath.setIntegerBit(attributes, 0, value);
	}

	public int getByteSize() {
		return 2 + 1 + 4 + 2;
		//nameptr + type + addr + size
	}

	public void addStrings(List<String> l) {
		ArraysEx.addIfNotNullOrContains(l, name);
	}

	public void write(DataOutputEx out, StringTable strtab) throws IOException {
		strtab.putStringOffset(name);
		out.write(type.ordinal() | (attributes << 3));
		address.write(out);
		out.writeShort(size);
	}

	@Override
	public String toString() {
		return name + "(" + type + ") @ 0x" + Integer.toHexString(address.getAddrAbs()) + "(" + (address.getAddrType()) + ")" + " [" + Integer.toHexString(size) + "]";
	}
}
