
package ctrmap.stdlib.arm.elf;

import ctrmap.stdlib.arm.elf.rpmconv.ExternalSymbolDB;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.fornwall.jelf.ElfException;
import net.fornwall.jelf.ElfFile;
import ctrmap.stdlib.arm.elf.rpmconv.rel.ETRel2RPMConverter;
import ctrmap.stdlib.arm.elf.rpmconv.exec.ETExec2RPMConverter;
import ctrmap.stdlib.formats.rpm.RPM;
import ctrmap.stdlib.arm.elf.rpmconv.IElf2RpmConverter;

/**
 *
 */
public class ELF2RPM {
	public static RPM getRPM(File elfFile, ExternalSymbolDB esdb){
		try {
			ElfFile elf = ElfFile.from(elfFile);
			
			IElf2RpmConverter conv = null;
			
			switch (elf.e_type){
				case ElfFile.ET_EXEC:
					conv = new ETExec2RPMConverter();
					break;
				case ElfFile.ET_REL:
					conv = new ETRel2RPMConverter();
					break;
			}
			
			if (conv != null){
				RPM rpm = conv.getRPM(elf, elfFile, esdb);
				rpm.updateBytesForSetBaseAddr(); //relocate to base 0 for better analysis
				return rpm;
			}
		} catch (ElfException | IOException ex) {
			Logger.getLogger(ELF2RPM.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
