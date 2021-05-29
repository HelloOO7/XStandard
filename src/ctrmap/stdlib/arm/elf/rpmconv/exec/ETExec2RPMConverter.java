package ctrmap.stdlib.arm.elf.rpmconv.exec;

import ctrmap.stdlib.fs.accessors.DiskFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.fornwall.jelf.ElfFile;
import net.fornwall.jelf.ElfSection;
import net.fornwall.jelf.ElfSymbol;
import ctrmap.stdlib.arm.elf.rpmconv.ExternalSymbolDB;
import ctrmap.stdlib.arm.elf.SectionType;
import ctrmap.stdlib.arm.elf.rpmconv.rel.Elf2RPMSymbolAdapter;
import ctrmap.stdlib.formats.rpm.RPM;
import ctrmap.stdlib.formats.rpm.RPMSymbolAddress;
import ctrmap.stdlib.formats.rpm.RPMSymbolType;
import ctrmap.stdlib.arm.elf.rpmconv.IElf2RpmConverter;
import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;

/**
 *
 */
public class ETExec2RPMConverter implements IElf2RpmConverter {

	@Override
	public RPM getRPM(ElfFile elf, File elfFile, ExternalSymbolDB esdb) throws IOException {
		RPM rpm = new RPM();

		List<ExecElfSection> sections = new ArrayList<>();

		DataIOStream io = new DiskFile(elfFile).getDataIOStream();

		for (int i = 0; i < elf.num_sh; i++) {
			ElfSection sect = elf.getSection(i);
			SectionType t = SectionType.getSectionTypeFromElf(sect.header.getName());
			if (t != null) {
				sections.add(new ExecElfSection(i, sect, t, elf, io, rpm));
			}
		}

		io.close();

		ETExecRelocationState relocState = new ETExecRelocationState(sections, esdb);

		for (ElfSymbol smb : elf.getSymbolTableSection().symbols) {
			if (!smb.getName().startsWith("$") && acceptsSymType(smb.getType())) {
				Elf2RPMSymbolAdapter s = new Elf2RPMSymbolAdapter(smb);
				s.name = smb.getName();
				s.type = getRpmSymType(smb.getType());

				if (s.name != null && esdb.isFuncExternal(s.name)) {
					s.address = new RPMSymbolAddress(rpm, RPMSymbolAddress.RPMAddrType.GLOBAL, esdb.getOffsetOfFunc(s.name));
				} else {
					int smbValue = (int) smb.st_value;
					if ((smbValue & 1) != 0 && s.type == RPMSymbolType.FUNCTION_ARM){
						s.type = RPMSymbolType.FUNCTION_THM;
						smbValue--;
					}
					s.address = new RPMSymbolAddress(rpm, RPMSymbolAddress.RPMAddrType.LOCAL, smbValue - relocState.getSourceSectionOffsetById(smb.st_shndx) + relocState.getTargetSectionOffsetById(smb.st_shndx));
				}
				rpm.symbols.add(s);
			}
		}

		for (ExecElfSection s : sections) {
			s.relocate(relocState);
		}

		DataIOStream out = new DataIOStream();
		for (ExecElfSection c : sections) {
			rpm.relocations.addAll(c.relocs);
			out.seek(relocState.getTargetSectionOffsetById(c.id));
			out.write(c.getBinary());
		}

		rpm.setCode(out);

		return rpm;
	}

	private static boolean acceptsSymType(int symType) {
		switch (symType) {
			case ElfSymbol.STT_FUNC:
			case ElfSymbol.STT_NOTYPE:
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
		}
		return null;
	}
}
