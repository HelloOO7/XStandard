
package ctrmap.stdlib.arm.elf;

import ctrmap.stdlib.arm.elf.format.ELF;
import ctrmap.stdlib.arm.elf.rpmconv.ExternalSymbolDB;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ctrmap.stdlib.arm.elf.rpmconv.rel.ETRel2RPMConverter;
import ctrmap.stdlib.arm.elf.rpmconv.exec.ETExec2RPMConverter;
import ctrmap.stdlib.formats.rpm.RPM;
import ctrmap.stdlib.arm.elf.rpmconv.IElf2RpmConverter;
import ctrmap.stdlib.fs.FSFile;

/**
 *
 */
public class ELF2RPM {
	public static RPM getRPM(FSFile elfFile, ExternalSymbolDB esdb){
		try {
			ELF elf = new ELF(elfFile);
			
			IElf2RpmConverter conv = null;
			
			switch (elf.header.type){
				case EXEC:
					conv = new ETExec2RPMConverter();
					break;
				case REL:
					conv = new ETRel2RPMConverter();
					break;
			}
			
			if (conv != null){
				RPM rpm = conv.getRPM(elf, esdb);
				rpm.updateBytesForSetBaseAddr(); //relocate to base 0 for better analysis
				return rpm;
			}
		} catch (IOException ex) {
			Logger.getLogger(ELF2RPM.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
