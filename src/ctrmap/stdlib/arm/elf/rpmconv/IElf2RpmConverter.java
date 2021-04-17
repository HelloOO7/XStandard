package ctrmap.stdlib.arm.elf.rpmconv;

import java.io.File;
import java.io.IOException;
import net.fornwall.jelf.ElfFile;
import ctrmap.stdlib.formats.rpm.RPM;

public interface IElf2RpmConverter {
	public RPM getRPM(ElfFile elf, File f, ExternalSymbolDB esdb) throws IOException;
}
