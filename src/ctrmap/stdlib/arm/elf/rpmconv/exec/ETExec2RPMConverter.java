package ctrmap.stdlib.arm.elf.rpmconv.exec;

import ctrmap.stdlib.fs.accessors.DiskFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ctrmap.stdlib.arm.elf.rpmconv.ExternalSymbolDB;
import ctrmap.stdlib.arm.elf.SectionType;
import ctrmap.stdlib.arm.elf.format.ELF;
import ctrmap.stdlib.arm.elf.format.sections.ELFSection;
import ctrmap.stdlib.arm.elf.format.sections.ELFSymbolSection;
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
	public RPM getRPM(ELF elf, ExternalSymbolDB esdb) throws IOException {
		RPM rpm = new RPM();

		List<ExecElfSection> sections = new ArrayList<>();

		DataIOStream io = elf.getSourceFile().getDataIOStream();

		for (ELFSection sec : elf.sections()) {
			SectionType t = SectionType.getSectionTypeFromElf(sec.header);
			if (t != null) {
				sections.add(new ExecElfSection(sec, t, elf, io, rpm));
			}
		}

		io.close();

		ETExecRelocationState relocState = new ETExecRelocationState(sections, esdb);

		for (ELFSymbolSection.ELFSymbol smb : elf.sectionsByClass(ELFSymbolSection.class).get(0).symbols) {
			if (!smb.name.startsWith("$") && acceptsSymType(smb.getSymType())) {
				Elf2RPMSymbolAdapter s = new Elf2RPMSymbolAdapter(smb);
				s.name = smb.name;
				s.type = getRpmSymType(smb.getSymType());

				if (s.name != null && esdb.isFuncExternal(s.name)) {
					s.address = new RPMSymbolAddress(rpm, RPMSymbolAddress.RPMAddrType.GLOBAL, esdb.getOffsetOfFunc(s.name));
				} else {
					int smbValue = (int) smb.value;
					if ((smbValue & 1) != 0 && s.type == RPMSymbolType.FUNCTION_ARM){
						s.type = RPMSymbolType.FUNCTION_THM;
						smbValue--;
					}
					s.address = new RPMSymbolAddress(rpm, RPMSymbolAddress.RPMAddrType.LOCAL, smbValue - relocState.getSourceSectionOffsetById(smb.sectionIndex) + relocState.getTargetSectionOffsetById(smb.sectionIndex));
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

	private static boolean acceptsSymType(ELFSymbolSection.ELFSymbolType symType) {
		switch (symType) {
			case FUNC:
			case NOTYPE:
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
		}
		return null;
	}
}
