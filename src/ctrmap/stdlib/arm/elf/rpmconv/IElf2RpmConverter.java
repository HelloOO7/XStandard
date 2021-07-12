package ctrmap.stdlib.arm.elf.rpmconv;

import ctrmap.stdlib.arm.elf.format.ELF;
import java.io.IOException;
import ctrmap.stdlib.formats.rpm.RPM;

public interface IElf2RpmConverter {
	public RPM getRPM(ELF elf, ExternalSymbolDB esdb) throws IOException;
}
