
package ctrmap.stdlib.arm.elf.rpmconv.rel;

import net.fornwall.jelf.ElfSymbol;
import ctrmap.stdlib.formats.rpm.RPMSymbol;

/**
 *
 */
public class Elf2RPMSymbolAdapter extends RPMSymbol {
	public ElfSymbol origin;
	
	public Elf2RPMSymbolAdapter(ElfSymbol origin){
		this.origin = origin;
	}
}
