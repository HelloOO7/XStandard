
package ctrmap.stdlib.arm.elf.rpmconv.rel;

import ctrmap.stdlib.arm.elf.format.sections.ELFSymbolSection;
import ctrmap.stdlib.formats.rpm.RPMSymbol;

/**
 *
 */
public class Elf2RPMSymbolAdapter extends RPMSymbol {
	public ELFSymbolSection.ELFSymbol origin;
	
	public Elf2RPMSymbolAdapter(ELFSymbolSection.ELFSymbol origin){
		this.origin = origin;
	}
}
