package ctrmap.stdlib.arm.elf.rpmconv.rel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ctrmap.stdlib.arm.elf.rpmconv.ExternalSymbolDB;
import ctrmap.stdlib.arm.elf.SectionType;
import ctrmap.stdlib.arm.elf.format.ELF;
import ctrmap.stdlib.arm.elf.format.sections.ELFSection;
import ctrmap.stdlib.arm.elf.format.sections.ELFSymbolSection;
import ctrmap.stdlib.formats.rpm.RPM;
import ctrmap.stdlib.formats.rpm.RPMSymbolAddress;
import ctrmap.stdlib.formats.rpm.RPMSymbolType;
import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;

/**
 *
 */
public class RelElfSection {

	public final int id;

	public final SectionType type;
	
	private ELFSection sec;
	private ELFSymbolSection sym;

	public final List<Elf2RPMSymbolAdapter> rpmSymbols = new ArrayList<>();

	public final int sourceOffset;
	public final int length;
	public int targetOffset;

	private DataIOStream buf;

	public RelElfSection(ELF elf, ELFSection sec, ELFSymbolSection symbols, DataIOStream io) throws IOException {
		id = elf.getSectionIndex(sec);
		sym = symbols;
		type = SectionType.getSectionTypeFromElf(sec.header);
		this.sec = sec;
		sourceOffset = sec.header.offset;
		length = sec.header.size;
		byte[] b = new byte[length];
		if (type != SectionType.BSS) {
			io.seek(sourceOffset);
			io.read(b);
		}
		buf = new DataIOStream(b);
	}

	public byte[] getBytes() {
		return buf.toByteArray();
	}

	public void prepareForRPM(RPM rpm, int targetOffset, ExternalSymbolDB esdb) {
		this.targetOffset = targetOffset;
		createRPMSymbols(rpm, esdb);
	}

	private void createRPMSymbols(RPM rpm, ExternalSymbolDB esdb) {
		for (ELFSymbolSection.ELFSymbol smb : sym.symbols) {
			if (smb.sectionIndex == id) {
				if ((smb.name == null || !smb.name.startsWith("$")) && acceptsSymType(smb.getSymType())) {
					Elf2RPMSymbolAdapter s = new Elf2RPMSymbolAdapter(smb);
					s.name = smb.name;
					s.type = getRpmSymType(smb.getSymType());
					s.size = smb.size;

					if (s.name != null && esdb.isFuncExternal(s.name)) {
						System.out.println("extern func " + s.name + " (cur symtype: " + s.type + ")");
						s.type = RPMSymbolType.FUNCTION_ARM;
						int off = esdb.getOffsetOfFunc(s.name);
						if ((off & 1) == 1){
							off--;
							s.type = RPMSymbolType.FUNCTION_THM;
						}
						s.address = new RPMSymbolAddress(rpm, RPMSymbolAddress.RPMAddrType.GLOBAL, off);
					} else {
						if (id == 0) {
						//	System.out.println("NONEXTERN FUNC IN EXTERN SEGMENT " + smb.getName());
							s.address = new RPMSymbolAddress(rpm, RPMSymbolAddress.RPMAddrType.GLOBAL, -1); //null address
						} else {
							int sval = smb.value + targetOffset;
							if ((sval & 1) != 0 && s.type == RPMSymbolType.FUNCTION_ARM) {
								s.type = RPMSymbolType.FUNCTION_THM;
								sval--;
							}
							s.address = new RPMSymbolAddress(rpm, RPMSymbolAddress.RPMAddrType.LOCAL, sval);
						}
					}
					rpmSymbols.add(s);
				}
			}
		}
	}

	private static boolean acceptsSymType(ELFSymbolSection.ELFSymbolType symType) {
		switch (symType) {
			case FUNC:
			case NOTYPE:
//			case ElfSymbol.STT_SECTION:
				return true;
		}
		return false;
	}

	private static RPMSymbolType getRpmSymType(ELFSymbolSection.ELFSymbolType symType) {
		switch (symType) {
			case FUNC:
				return RPMSymbolType.FUNCTION_ARM;
			case NOTYPE:
				return RPMSymbolType.VALUE;
			case SECTION:
				return RPMSymbolType.SECTION;
		}
		return null;
	}
}
