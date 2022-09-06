package xstandard.arm.elf;

import xstandard.arm.elf.format.ELF;
import xstandard.arm.elf.format.ELFHeader;
import xstandard.arm.elf.format.ELFStringTable;
import xstandard.arm.elf.format.sections.ELFRelocationSectionBase;
import xstandard.arm.elf.format.sections.ELFSection;
import xstandard.arm.elf.format.sections.ELFSymbolSection;
import xstandard.math.MathEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ELFMerge {

	private ELF mainELF;

	private final HashSet<ELF> mergedElfs = new HashSet<>();
	private final Map<ELF, Map<Integer, Integer>> sectionNoRemaps = new HashMap<>();

	public ELFMerge(ELF initElf) {
		mergedElfs.add(initElf);
		mainELF = initElf;
	}

	public int getMergedElfShndx(ELF elf, int shndx) {
		if (elf == mainELF){
			return shndx;
		}
		if (!sectionNoRemaps.containsKey(elf)){
			throw new NullPointerException("Shndx mapped to ELF file not present!");
		}
		int val = sectionNoRemaps.get(elf).getOrDefault(shndx, -1);
		if (val == -1) {
			System.err.println("WARN: Shndx " + shndx + " not found !");
		}
		return val;
	}

	public void resolveExternSymbols(IELFExternResolver rsl) {
		for (ELFSymbolSection sym : mainELF.sectionsByClass(ELFSymbolSection.class)) {
			for (ELFSymbolSection.ELFSymbol smb : sym.symbols) {
				if (smb.sectionIndex == 0) {
					rsl.resolveSymAddr(this, smb);
					if (smb.sectionIndex != 0){
						if ((smb.value & 1) == 1){
							//This is definitely a Thumb function
							smb.setSymType(ELFSymbolSection.ELFSymbolType.FUNC);
						}
					}
				}
			}
		}
	}

	public ELF getELF() {
		return mainELF;
	}

	public void mergeELF(ELF elf, String sectionNameSuffix) {
		if (mergedElfs.contains(elf)) {
			return;
		}
		if (elf.header.type != ELFHeader.ELFType.REL) {
			throw new UnsupportedOperationException("Only relocatable ELFs can be merged!");
		}

		int loadAddr = 0;
		for (ELFSection sect : mainELF.sections()) {
			loadAddr = Math.max(loadAddr, sect.header.loadAddr + sect.header.size);
		}

		Map<Integer, Integer> sectionNoRemap = new HashMap<>();
		sectionNoRemap.put(0, 0); //null section - EXTERN
		sectionNoRemaps.put(elf, sectionNoRemap);

		List<ELFSymbolSection> symTabs = new ArrayList<>();
		List<ELFRelocationSectionBase> rels = new ArrayList<>();

		ELFSymbolSection symTab = mainELF.sectionsByClass(ELFSymbolSection.class).get(0);
		int symbolTableIdx = symTab.symbols.size();

		Map<ELFSymbolSection.ELFSymbol, SymbolTransferInfo> symbolTransfers = new HashMap<>();

		for (ELFSection sect : elf.sections()) {
			int originalSectionIndex = elf.getSectionIndex(sect);

			sect = sect.clone();
			sect.header.name = sect.header.name + "." + sectionNameSuffix;

			switch (sect.header.type.getSectionType()) {
				case PROGBITS:
				case NOBITS:
					sect.header.loadAddr = loadAddr;
					loadAddr += sect.header.size;
					loadAddr = MathEx.padInteger(loadAddr, sect.header.alignment);
					mainELF.addSection(sect);
					break;
				case RELA:
				case REL:
					mainELF.addSection(sect);
					rels.add((ELFRelocationSectionBase)sect);
					break;
				case STRTAB:
					if (!elf.header.isShStrTab(originalSectionIndex)) {
						mainELF.addSection(sect);
					} else {
						for (String str : ((ELFStringTable) sect).strings()) {
							mainELF.strTab.putString(str);
						}
					}
					break;
				case SYMTAB:
					//CAN NOT MERGE SYMBOL TABLE
					//having more than one symbol table breaks a lot of stuff
					//instead we have to merge the entries

					ELFSymbolSection ess = (ELFSymbolSection) sect;

					for (ELFSymbolSection.ELFSymbol sym : ess.symbols) {
						symTab.addSymbol(sym);
						symbolTransfers.put(sym, new SymbolTransferInfo(ess.getSymIndex(sym), -1));
					}

					symTabs.add(ess);

					break;
			}

			sectionNoRemap.put(originalSectionIndex, mainELF.getSectionIndex(sect));
		}

		for (ELFSymbolSection sym : symTabs) {
			for (int i = 0; i < sym.symbols.size(); i++) {
				ELFSymbolSection.ELFSymbol smb = sym.symbols.get(i);
				ELFSymbolSection.ELFSpecialSectionIndex spSecIdx = smb.getSpecialSectionIndex();
				if (smb.getSpecialSectionIndex() == null || spSecIdx == ELFSymbolSection.ELFSpecialSectionIndex.UNDEF) {
					symbolTransfers.get(smb).destIdx = symbolTableIdx;

					int newSectionIndex = sectionNoRemap.getOrDefault(smb.sectionIndex, -1);
					if (newSectionIndex == -1) {
						sym.symbols.remove(i);
						i--;
					} else {
						smb.sectionIndex = newSectionIndex;
						symbolTableIdx++;
					}
				} else {
					symbolTableIdx++;
				}
			}
		}
		
		Map<Integer, Integer> symSrcDstFast = new HashMap<>();
		for (SymbolTransferInfo sti : symbolTransfers.values()){
			symSrcDstFast.put(sti.srcIdx, sti.destIdx);
		}
		
		int symTabIndex = mainELF.getSectionIndex(symTab);

		for (ELFRelocationSectionBase<? extends ELFRelocationSectionBase.RelocationEntry> rel : rels) {
			rel.setRelocatedSegmentNo(sectionNoRemap.get(rel.getRelocatedSegmentNo()));
			rel.setSymTabSegmentNo(symTabIndex);
			
			for (ELFRelocationSectionBase.RelocationEntry e : rel.entries){
				int symIdx = e.getRelSymbol();
				int dest = symSrcDstFast.getOrDefault(symIdx, -1);
				if (dest == -1){
					System.err.println("WARN: Relocation symbol not resolved");
				}
				else {
					e.setRelSymbol(dest);
				}
			}
		}
	}

	private static class SymbolTransferInfo {

		public int srcIdx;
		public int destIdx;

		public SymbolTransferInfo(int si, int di) {
			srcIdx = si;
			destIdx = di;
		}
	}
}
