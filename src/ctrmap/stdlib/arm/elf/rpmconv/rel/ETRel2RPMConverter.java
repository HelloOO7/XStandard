package ctrmap.stdlib.arm.elf.rpmconv.rel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ctrmap.stdlib.arm.elf.rpmconv.ExternalSymbolDB;
import ctrmap.stdlib.arm.elf.SectionType;
import ctrmap.stdlib.arm.elf.format.ELF;
import ctrmap.stdlib.arm.elf.format.sections.ELFRelocationSectionBase;
import ctrmap.stdlib.arm.elf.format.sections.ELFSection;
import ctrmap.stdlib.arm.elf.format.sections.ELFSymbolSection;
import ctrmap.stdlib.formats.rpm.RPM;
import ctrmap.stdlib.formats.rpm.RPMRelocation;
import ctrmap.stdlib.formats.rpm.RPMRelocationSource;
import ctrmap.stdlib.formats.rpm.RPMSymbol;
import ctrmap.stdlib.arm.elf.rpmconv.IElf2RpmConverter;
import ctrmap.stdlib.formats.rpm.RPMRelocationTarget;
import ctrmap.stdlib.formats.rpm.RPMSymbolAddress;
import ctrmap.stdlib.formats.rpm.RPMSymbolType;
import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import ctrmap.stdlib.math.MathEx;

/**
 *
 */
public class ETRel2RPMConverter implements IElf2RpmConverter {

	@Override
	public RPM getRPM(ELF elf, ExternalSymbolDB esdb) throws IOException {
		RPM rpm = new RPM();
		rpm.baseAddress = 0;

		DataIOStream io = elf.getSourceFile().getDataIOStream();

		List<RelElfSection> sections = new ArrayList<>();

		Map<ELFSection, ELFRelocationSectionBase<? extends ELFRelocationSectionBase.RelocationEntry>> relSections = new HashMap<>();

		ELFSymbolSection symbs = elf.sectionsByClass(ELFSymbolSection.class).get(0);

		for (ELFSection sec : elf.sections()) {
			SectionType compatibleType = SectionType.getSectionTypeFromElf(sec.header);

			if (compatibleType != null) {
				sections.add(new RelElfSection(elf, sec, symbs, io));
			} else if (sec instanceof ELFRelocationSectionBase) {
				ELFRelocationSectionBase rel = (ELFRelocationSectionBase) sec;
				relSections.put(elf.getSectionByIndex(rel.getRelocatedSegmentNo()), rel);
			} else {
				System.out.println("Skipping section " + sec.header.name + " (idx " + elf.getSectionIndex(sec) + ")");
			}
		}

		int offs = 0;
		for (RelElfSection sec : sections) {
			sec.prepareForRPM(rpm, offs, esdb);
			offs += sec.length;
			offs = MathEx.padInteger(offs, Integer.BYTES);
		}

		for (Map.Entry<ELFSection, ELFRelocationSectionBase<? extends ELFRelocationSectionBase.RelocationEntry>> re : relSections.entrySet()) {
			ELFRelocationSectionBase<? extends ELFRelocationSectionBase.RelocationEntry> relocation = re.getValue();
			RelElfSection sec = findSectionById(sections, relocation.getRelocatedSegmentNo());

			if (sec != null) {
				for (ELFRelocationSectionBase.RelocationEntry e : relocation.entries) {
					int relocOffs = e.offset;
					int rpmRelocOffs = relocOffs + sec.targetOffset;
					int elfRelocOffs = relocOffs + sec.sourceOffset;

					RPMRelocation rel = new RPMRelocation();
					rel.target = new RPMRelocationTarget(rpmRelocOffs);
					rel.sourceType = RPMRelocation.RPMRelSourceType.SYMBOL_INTERNAL;

					switch (e.getRelType()) {
						case R_ARM_ABS32: {
							io.seek(elfRelocOffs);
							int addend = io.readInt() + e.getAddend();
							rel.targetType = RPMRelocation.RPMRelTargetType.OFFSET;
							ELFSymbolSection.ELFSymbol es = symbs.symbols.get(e.getRelSymbol());
							RPMSymbol s = findRPMByMatchElfAddr(sections, es, addend);
							if (s == null) {
								System.out.println("notfound symbol " + es.name + " addend " + addend + " shndx " + Long.toHexString(es.sectionIndex) + " at " + Integer.toHexString(relocOffs));

								s = new RPMSymbol();
								s.name = null;
								s.type = RPMSymbolType.VALUE;
								if (findSectionById(sections, es.sectionIndex) == null) {
									System.out.println("FATAL: NOTFOUND SECTION " + es.sectionIndex + " for sym " + es.name);

									for (ELFSection se : elf.sections()) {
										System.out.println("section " + se + ": " + se.header.name);
									}
								}

								s.address = new RPMSymbolAddress(rpm, RPMSymbolAddress.RPMAddrType.LOCAL, findSectionById(sections, es.sectionIndex).targetOffset + (int) es.value + addend);
								System.out.println("notfound fixup addr " + Integer.toHexString(s.address.getAddr()));
								rpm.symbols.add(s);
							} else {
								System.out.println("found symbol " + Long.toHexString(es.value) + " of shndx " + es.sectionIndex + " at " + Integer.toHexString(s.address.getAddr()));
							}
							rel.source = new RPMRelocationSource.RPMRelSrcInternalSymbol(rpm, s);
							break;
						}
						case R_ARM_THM_PC22: {
							rel.targetType = RPMRelocation.RPMRelTargetType.THUMB_BRANCH_LINK;
							ELFSymbolSection.ELFSymbol es = symbs.symbols.get(e.getRelSymbol());
							RPMSymbol s = findRPMByMatchElfAddr(sections, es, 0, true);
							if (s == null) {
								System.out.println("Could not find function symbol " + es.name + " of shndx " + es.sectionIndex);
							} else {
								System.out.println("got func symbol " + s.name + " type " + s.type + " add type " + s.address.getAddrType());
							}
							rel.source = new RPMRelocationSource.RPMRelSrcInternalSymbol(rpm, s);
							break;
						}
						case R_ARM_CALL:
							rel.targetType = RPMRelocation.RPMRelTargetType.ARM_BRANCH_LINK;
							ELFSymbolSection.ELFSymbol es = symbs.symbols.get(e.getRelSymbol());
							RPMSymbol s = findRPMByMatchElfAddr(sections, es, 0, true);
							if (s == null) {
								System.out.println("Could not find function symbol " + es.name);
							}
							rel.source = new RPMRelocationSource.RPMRelSrcInternalSymbol(rpm, s);
							break;
						default:
							System.out.println("UNSUPPORTED ARM RELOCATION TYPE: " + e.getRelType());
							break;
					}

					rpm.relocations.add(rel);
				}
			}
		}

		io.close();

		DataIOStream code = new DataIOStream();
		for (RelElfSection s : sections) {
			code.seek(s.targetOffset);
			code.write(s.getBytes());
			rpm.symbols.addAll(s.rpmSymbols);
		}
		rpm.setCode(code);

		return rpm;
	}

	private static RPMSymbol findRPMByMatchElfAddr(List<RelElfSection> sections, ELFSymbolSection.ELFSymbol sym, int addend) {
		return findRPMByMatchElfAddr(sections, sym, addend, false);
	}

	private static RPMSymbol findRPMByMatchElfAddr(List<RelElfSection> sections, ELFSymbolSection.ELFSymbol sym, int addend, boolean needsFunc) {
		for (RelElfSection s : sections) {
			for (Elf2RPMSymbolAdapter a : s.rpmSymbols) {
				if (a.origin == sym && a.origin.getSymType() != ELFSymbolSection.ELFSymbolType.SECTION) {
					return a;
				}
			}
		}
		int addr = (int) (sym.value + addend);
		for (RelElfSection s : sections) {
			for (Elf2RPMSymbolAdapter a : s.rpmSymbols) {
				if (a.origin.sectionIndex == sym.sectionIndex && a.origin.value == addr) {
					if (!needsFunc || a.type.isFunction()) {
						return a;
					}
				}
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
}
