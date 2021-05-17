package ctrmap.stdlib.arm.elf.rpmconv.rel;

import ctrmap.stdlib.io.MemoryStream;
import ctrmap.stdlib.io.base.IOStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.fornwall.jelf.ElfFile;
import net.fornwall.jelf.ElfSection;
import net.fornwall.jelf.ElfSymbol;
import ctrmap.stdlib.arm.elf.rpmconv.ExternalSymbolDB;
import ctrmap.stdlib.arm.elf.SectionType;
import ctrmap.stdlib.formats.rpm.RPM;
import ctrmap.stdlib.formats.rpm.RPMSymbol;
import ctrmap.stdlib.formats.rpm.RPMSymbolAddress;
import ctrmap.stdlib.formats.rpm.RPMSymbolType;

/**
 *
 */
public class RelElfSection {

	public final int id;

	public final SectionType type;
	private ElfFile elf;
	private ElfSection sec;

	public final List<Elf2RPMSymbolAdapter> rpmSymbols = new ArrayList<>();

	public final int sourceOffset;
	public final int length;
	public int targetOffset;

	private MemoryStream buf;

	public RelElfSection(ElfFile elf, int sectionId, IOStream io) throws IOException {
		id = sectionId;
		sec = elf.getSection(id);
		type = SectionType.getSectionTypeFromElf(sec.header.getName());
		this.elf = elf;
		sourceOffset = (int) sec.header.section_offset;
		length = (int) sec.header.size;
		byte[] b = new byte[length];
		if (type != SectionType.BSS) {
			io.seek(sourceOffset);
			io.read(b);
		}
		buf = new MemoryStream(b);
	}

	public byte[] getBytes() {
		return buf.toByteArray();
	}

	public void prepareForRPM(RPM rpm, int targetOffset, ExternalSymbolDB esdb) {
		this.targetOffset = targetOffset;
		createRPMSymbols(rpm, esdb);
	}

	private void createRPMSymbols(RPM rpm, ExternalSymbolDB esdb) {
		for (ElfSymbol smb : elf.getSymbolTableSection().symbols) {
			if (smb.st_shndx == id) {
				if ((smb.getName() == null || !smb.getName().startsWith("$")) && acceptsSymType(smb.getType())) {
					Elf2RPMSymbolAdapter s = new Elf2RPMSymbolAdapter(smb);
					s.name = smb.getName();
					s.type = getRpmSymType(smb.getType());
					s.size = (int) smb.st_size;

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
							int sval = (int) smb.st_value + targetOffset;
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

	private static boolean acceptsSymType(int symType) {
		switch (symType) {
			case ElfSymbol.STT_FUNC:
			case ElfSymbol.STT_NOTYPE:
//			case ElfSymbol.STT_SECTION:
				return true;
		}
		return false;
	}

	private static RPMSymbolType getRpmSymType(int symType) {
		switch (symType) {
			case ElfSymbol.STT_FUNC:
				return RPMSymbolType.FUNCTION_ARM;
			case ElfSymbol.STT_NOTYPE:
				return RPMSymbolType.VALUE;
			case ElfSymbol.STT_SECTION:
				return RPMSymbolType.SECTION;
		}
		return null;
	}
}
