package ctrmap.stdlib.arm.elf.rpmconv.exec;

import ctrmap.stdlib.io.MemoryStream;
import ctrmap.stdlib.io.base.IOStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.fornwall.jelf.ElfFile;
import net.fornwall.jelf.ElfSection;
import net.fornwall.jelf.ElfSymbol;
import net.fornwall.jelf.ElfSymbolTableSection;
import ctrmap.stdlib.arm.ThumbAssembler;
import ctrmap.stdlib.arm.elf.rpmconv.ExternalSymbolDB;
import ctrmap.stdlib.arm.elf.SectionType;
import ctrmap.stdlib.arm.elf.SymbInfo;
import ctrmap.stdlib.formats.rpm.RPM;
import ctrmap.stdlib.formats.rpm.RPMRelocation;
import ctrmap.stdlib.formats.rpm.RPMRelocationSource;
import ctrmap.stdlib.formats.rpm.RPMRelocationTarget;
import ctrmap.stdlib.formats.rpm.RPMSymbol;
import ctrmap.stdlib.formats.rpm.RPMSymbolType;

/**
 *
 */
public class ExecElfSection {

	public int id;

	private ElfSymbolTableSection symbols;
	private ElfFile elf;

	public final SectionType type;
	private int offset;
	private int length;
	private MemoryStream buf;

	private List<SubSection> subSections = new ArrayList<>();

	public final List<RPMRelocation> relocs = new ArrayList<>();
	
	private final List<SymbInfo> functions = new ArrayList<>();
	
	private RPM rpm;
	
	public ExecElfSection(int sectionId, ElfSection sec, SectionType type, ElfFile elf, IOStream io, RPM rpm) throws IOException {
		id = sectionId;
		this.rpm = rpm;
		this.elf = elf;
		offset = (int) sec.header.address;
		length = (int) sec.header.size;
		byte[] b = new byte[length];
		if (type != SectionType.BSS) {
			io.seek((int) sec.header.section_offset);
			io.read(b);
		}
		buf = new MemoryStream(b);
		this.type = type;
		this.symbols = elf.getSymbolTableSection();
		buf.setBase(offset);
		loadSubSections(symbols);
		loadFunctionInfo(symbols);
	}
	
	public int getOriginalSectionOffset() {
		return offset;
	}

	public int getSectionSize() {
		return length;
	}

	private void loadSubSections(ElfSymbolTableSection symbTable) {
		int eo = offset + length;
		for (ElfSymbol s : symbTable.symbols) {
			if (s.st_shndx == id) {
				int v = getSymbolValue(s);
				if (v >= offset && v < eo) {
					if (s.getName() != null) {
						switch (s.getName()) {
							case "$d":
								addSubSection(SubSection.SubSectionType.DATA, s);
								break;
							case "$t":
								addSubSection(SubSection.SubSectionType.TEXT, s);
								break;
						}
					}
				}
			}
		}
		if (!subSections.isEmpty()) {
			subSections.get(subSections.size() - 1).endOffset = offset + length;
		}
	}

	private void loadFunctionInfo(ElfSymbolTableSection symbTable) {
		for (ElfSymbol s : symbTable.symbols) {
			if (s.st_shndx == id) {
				if (s.getType() == ElfSymbol.STT_FUNC) {
					SymbInfo f = new SymbInfo(s.getName(), getSymbolValue(s));
					f.absoluteAddress -= f.absoluteAddress % 2; //hword align
					functions.add(f);
				}
			}
		}
	}

	private int getSymbolValue(ElfSymbol smb){
		return (int)smb.st_value;
	}
	
	private void addSubSection(SubSection.SubSectionType type, ElfSymbol smb) {
		int offs = getSymbolValue(smb);
		if (!subSections.isEmpty()) {
			subSections.get(subSections.size() - 1).endOffset = offs;
		}
		subSections.add(new SubSection(type, offs));
	}

	private String findNameOfFunction(int funcOffsetAbsolute) {
		funcOffsetAbsolute -= funcOffsetAbsolute % 4; //word-align
		for (SymbInfo f : functions) {
			if (f.absoluteAddress == funcOffsetAbsolute) {
				return f.name;
			}
		}
		return null;
	}

	private ElfSymbol findSymbol(int symbolOffsetAbsolute) {
		symbolOffsetAbsolute -= symbolOffsetAbsolute % 4; //word-align
		for (ElfSymbol s : symbols.symbols) {
			if (s.getType() != ElfSymbol.STT_SECTION && getSymbolValue(s) == symbolOffsetAbsolute) {
				return s;
			}
		}
		return null;
	}

	private void relocateDataSymbolReferences(ETExecRelocationState state) {
		try {
			ExternalSymbolDB esdb = state.getESDB();
			if (type != SectionType.BSS) {
				for (SubSection s : subSections) {
					if (s.type == SubSection.SubSectionType.DATA) {
						buf.seek(s.offset);
						//System.out.println(Integer.toHexString(s.offset) + ", " + Integer.toHexString(s.endOffset));

						while (buf.getPosition() <= (s.endOffset - 4)) {
							//even if the data is something else than offsets, it has to be word-aligned
							int pos = buf.getPosition();
							int symbOffs = buf.readInt();
							if (symbOffs == 0) {
								continue;
							}
							ElfSymbol symb = findSymbol(symbOffs);
							if (symb != null) {
								if (esdb.isFuncExternal(symb.getName())) {
									buf.seek(pos);
									buf.writeInt(esdb.getOffsetOfFunc(symb.getName()));
								} else {
									int targetSectionOffset = state.getTargetSectionOffsetById(symb.st_shndx);
									if (targetSectionOffset == -1) {
										System.err.println("WARN: Could not find section for symbol: " + symb + " at " + Integer.toHexString(symbOffs));
									} else {
										int srcSectionOffset = state.getSourceSectionOffsetById(symb.st_shndx);
										int finalSymbOffset = getSymbolValue(symb) - srcSectionOffset + targetSectionOffset;
										//System.out.println("Relocating symbol ref " + Integer.toHexString(pos) + " from " + srcSectionOffset + " to " + targetSectionOffset);
										buf.seek(pos);
										buf.writeInt(finalSymbOffset);
									}
								}
							}
							else {
								//System.out.println("notfound symbol " + Integer.toHexString(symbOffs));
							}
						}
					}
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(ExecElfSection.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void relocate(ETExecRelocationState state) {
		relocateSubSections(state);
		buf.setBase(state.getTargetSectionOffsetById(id));
		relocateDataSymbolReferences(state);
		relocateLocalFunctions(state);
		relocateBranchesForExternalFunctions(state.getESDB());
		removeExternalFunctions(state.getESDB());
	}

	private void removeExternalFunctions(ExternalSymbolDB efdb) {
		for (int i = 0; i < functions.size(); i++) {
			if (efdb.isFuncExternal(functions.get(i).name)) {
				functions.remove(i);
				i--;
			}
		}
	}

	private void relocateSubSections(ETExecRelocationState state) {
		int so = state.getSourceSectionOffsetById(id);
		int to = state.getTargetSectionOffsetById(id);
		int diff = to - so;
		for (SubSection s : subSections) {
			s.offset += diff;
			s.endOffset += diff;
		}
	}

	private void relocateLocalFunctions(ETExecRelocationState state) {
		int so = state.getSourceSectionOffsetById(id);
		int to = state.getTargetSectionOffsetById(id);
		int diff = to - so;
		for (int i = 0; i < functions.size(); i++) {
			SymbInfo f = functions.get(i);
			f.absoluteAddress += diff;
		}
	}

	public byte[] getBinary() {
		return buf.toByteArray();
	}

	private void relocateBranchesForExternalFunctions(ExternalSymbolDB efdb) {
		try {
			if (type == SectionType.TEXT) {
				for (SubSection s : subSections) {
					if (s.type == SubSection.SubSectionType.TEXT) {
						buf.seek(s.offset);

						while (buf.getPosition() < (s.endOffset - 2)) {
							int pos = buf.getPosition();
							int instruction = buf.readUnsignedShort();

							if ((instruction & ThumbAssembler.BL_MASK) == ThumbAssembler.BL_HIGH_IDENT) {
								buf.seek(pos);
								int brnchAddr = ThumbAssembler.getBranchInstructionTarget(buf);
								String funcName = findNameOfFunction(brnchAddr);
								int externalOffset = efdb.getOffsetOfFunc(funcName);
								if (externalOffset != 0) {
									System.out.println("Relocating external function " + funcName);
									buf.seek(pos);
									ThumbAssembler.writeBranchLinkInstruction(buf, externalOffset);
									
									RPMRelocation r = new RPMRelocation();
									r.sourceType = RPMRelocation.RPMRelSourceType.SYMBOL_INTERNAL;
									r.target = new RPMRelocationTarget(pos);
									r.targetType = RPMRelocation.RPMRelTargetType.THUMB_BRANCH_LINK;
									r.source = new RPMRelocationSource.RPMRelSrcInternalSymbol(rpm, findRPMFuncByAddr(externalOffset));
									relocs.add(r);
								}
								else {
									RPMRelocation r = new RPMRelocation();
									r.sourceType = RPMRelocation.RPMRelSourceType.SYMBOL_INTERNAL;
									r.target = new RPMRelocationTarget(pos);
									r.targetType = RPMRelocation.RPMRelTargetType.THUMB_BRANCH_LINK;
									r.source = new RPMRelocationSource.RPMRelSrcInternalSymbol(rpm, findRPMFuncByAddr(brnchAddr));
									relocs.add(r);
								}
							}
						}
					}
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(ExecElfSection.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private RPMSymbol findRPMFuncByAddr(int addr){
		for (RPMSymbol s : rpm.symbols){
			if (s.type.isFunction() && s.address.getAddrAbs() == addr){
				return s;
			}
		}
		return null;
	}

	public static class SubSection {

		public SubSectionType type;
		public int offset;
		public int endOffset;

		public SubSection(SubSectionType t, int offset) {
			type = t;
			this.offset = offset;
		}

		public static enum SubSectionType {
			TEXT,
			DATA
		}
	}

}
