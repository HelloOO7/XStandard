package ctrmap.stdlib.arm.elf.rpmconv.rel;

import ctrmap.stdlib.fs.accessors.DiskFile;
import ctrmap.stdlib.io.RandomAccessByteArray;
import ctrmap.stdlib.io.base.IOWrapper;
import ctrmap.stdlib.io.util.BitUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fornwall.jelf.ElfFile;
import net.fornwall.jelf.ElfSection;
import net.fornwall.jelf.ElfSymbol;
import net.fornwall.jelf.ElfSymbolTableSection;
import ctrmap.stdlib.arm.elf.rpmconv.ExternalSymbolDB;
import ctrmap.stdlib.arm.elf.SectionType;
import ctrmap.stdlib.formats.rpm.RPM;
import ctrmap.stdlib.formats.rpm.RPMRelocation;
import ctrmap.stdlib.formats.rpm.RPMRelocationSource;
import ctrmap.stdlib.formats.rpm.RPMSymbol;
import ctrmap.stdlib.arm.elf.rpmconv.IElf2RpmConverter;
import ctrmap.stdlib.formats.rpm.RPMRelocationTarget;
import ctrmap.stdlib.formats.rpm.RPMSymbolAddress;
import ctrmap.stdlib.formats.rpm.RPMSymbolType;

/**
 *
 */
public class ETRel2RPMConverter implements IElf2RpmConverter {

	@Override
	public RPM getRPM(ElfFile elf, File f, ExternalSymbolDB esdb) throws IOException {
		RPM rpm = new RPM();
		rpm.baseAddress = 0;

		List<RelElfSection> sections = new ArrayList<>();

		IOWrapper io = new DiskFile(f).getIO();

		Map<String, ElfSection> relSections = new HashMap<>();

		for (int i = 0; i < elf.num_sh; i++) {
			ElfSection s = elf.getSection(i);

			SectionType compatibleType = SectionType.getSectionTypeFromElf(s.header.getName());

			if (compatibleType != null) {
				sections.add(new RelElfSection(elf, i, io));
			} else if (isRelocationSection(s)) {
				String relSecName = s.header.getName().substring(".rel".length());
				relSections.put(relSecName, s);
			}
		}

		/*for (ElfSymbol sym : elf.getSymbolTableSection().symbols){
			if (sym.st_shndx == 0){
				//EXTERN
				if (esdb.isFuncExternal(sym.getName())){
					RPMSymbol rpms = new RPMSymbol();
					rpms.name = sym.getName();
					rpms.type = RPMSymbolType.FUNCTION_THM;
					rpms.address = new RPMSymbolAddress(rpm, RPMSymbolAddress.RPMAddrType.GLOBAL, esdb.getOffsetOfFunc(sym.getName()));
					rpm.symbols.add(rpms);
				}
			}
		}*/
		int offs = 0;
		for (RelElfSection sec : sections) {
			sec.prepareForRPM(rpm, offs, esdb);
			offs += sec.length;
			offs = BitUtils.getPaddedInteger(offs, Integer.BYTES);
		}

		ElfSymbolTableSection symbs = elf.getSymbolTableSection();

		for (Map.Entry<String, ElfSection> re : relSections.entrySet()) {
			SectionType t = SectionType.getSectionTypeFromElf(re.getKey());
			RelElfSection sec = findSectionByType(sections, t);
			if (sec != null) {
				ElfSection relocation = re.getValue();
				int relocationCount = (int) (relocation.header.size / relocation.header.entry_size);

				for (int i = 0; i < relocationCount; i++) {
					io.seek((int) (relocation.header.section_offset + relocation.header.entry_size * i));

					int relocOffs = io.readInt();
					int rpmRelocOffs = relocOffs + sec.targetOffset;
					int elfRelocOffs = relocOffs + sec.sourceOffset;

					int armRelocType = io.read();
					int relocTypeArg = BitUtils.readUInt24LE(io);

					RPMRelocation rel = new RPMRelocation();
					rel.target = new RPMRelocationTarget(rpmRelocOffs);
					rel.sourceType = RPMRelocation.RPMRelSourceType.SYMBOL_INTERNAL;

					switch (armRelocType) {
						case ARMELFRelTypes.R_ARM_ABS32: {
							io.seek(elfRelocOffs);
							int addend = io.readInt();
							rel.targetType = RPMRelocation.RPMRelTargetType.OFFSET;
							ElfSymbol es = symbs.symbols[relocTypeArg];
							RPMSymbol s = findRPMByMatchElfAddr(sections, elf, es, addend);
							if (s == null) {
								System.out.println("notfound symbol " + es + " addend " + addend + " shndx " + Long.toHexString(es.st_shndx) + " at " + Integer.toHexString(relocOffs));

								s = new RPMSymbol();
								s.name = null;
								s.type = RPMSymbolType.VALUE;
								s.address = new RPMSymbolAddress(rpm, RPMSymbolAddress.RPMAddrType.LOCAL, findSectionById(sections, es.st_shndx).targetOffset + (int) es.st_value + addend);
								rpm.symbols.add(s);
							}
							else {
								System.out.println("found symbol " + Long.toHexString(es.st_value) + " of shndx " + es.st_shndx + " at " + Integer.toHexString(s.address.getAddr()));
							}
							rel.source = new RPMRelocationSource.RPMRelSrcInternalSymbol(rpm, s);
							break;
						}
						case ARMELFRelTypes.R_ARM_THM_CALL: {
							rel.targetType = RPMRelocation.RPMRelTargetType.THUMB_BRANCH_LINK;
							RPMSymbol s = findRPMByMatchElfAddr(sections, elf, symbs.symbols[relocTypeArg], 0, true);
							if (s == null) {
								System.out.println("Could not find function symbol " + symbs.symbols[relocTypeArg]);
							} else {
								System.out.println("got func symbol " + s.name + " type " + s.type + " add type " + s.address.getAddrType());
							}
							rel.source = new RPMRelocationSource.RPMRelSrcInternalSymbol(rpm, s);
							break;
						}
						case ARMELFRelTypes.R_ARM_CALL:
							rel.targetType = RPMRelocation.RPMRelTargetType.ARM_BRANCH_LINK;
							RPMSymbol s = findRPMByMatchElfAddr(sections, elf, symbs.symbols[relocTypeArg], 0, true);
							if (s == null) {
								System.out.println("Could not find function symbol " + symbs.symbols[relocTypeArg]);
							}
							rel.source = new RPMRelocationSource.RPMRelSrcInternalSymbol(rpm, s);
							break;
						default:
							System.out.println("UNSUPPORTED ARM RELOCATION TYPE: " + armRelocType);
							break;
					}

					rpm.relocations.add(rel);
				}
			}
		}

		io.close();

		RandomAccessByteArray code = new RandomAccessByteArray();
		for (RelElfSection s : sections) {
			code.seek(s.targetOffset);
			code.write(s.getBytes());
			rpm.symbols.addAll(s.rpmSymbols);
		}
		rpm.setCode(code);

		return rpm;
	}

	private static RPMSymbol findRPMByMatchElfAddr(List<RelElfSection> sections, ElfFile elf, ElfSymbol sym, int addend) {
		return findRPMByMatchElfAddr(sections, elf, sym, addend, false);
	}

	private static RPMSymbol findRPMByMatchElfAddr(List<RelElfSection> sections, ElfFile elf, ElfSymbol sym, int addend, boolean needsFunc) {
		for (RelElfSection s : sections) {
			for (Elf2RPMSymbolAdapter a : s.rpmSymbols) {
				if (a.origin == sym){
					if (!needsFunc || a.type.isFunction()) {
						return a;
					}
				}
			}
		}
		int addr = (int) (sym.st_value + addend);
		for (RelElfSection s : sections) {
			for (Elf2RPMSymbolAdapter a : s.rpmSymbols) {
				if (a.origin.st_shndx == sym.st_shndx && a.origin.st_value == addr) {
					if (!needsFunc || a.type.isFunction()) {
						return a;
					}
				}
			}
		}
		return null;
	}

	private static RelElfSection findSectionByType(List<RelElfSection> l, SectionType t) {
		for (RelElfSection s : l) {
			if (s.type == t) {
				return s;
			}
		}
		return null;
	}

	private static RelElfSection findSectionById(List<RelElfSection> l, int id) {
		for (RelElfSection s : l) {
			if (s.id == id) {
				return s;
			}
		}
		return null;
	}

	private static boolean isRelocationSection(ElfSection s) {
		return s.header.getName() != null && s.header.getName().startsWith(".rel");
	}
}