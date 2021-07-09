package ctrmap.stdlib.arm.elf;

import ctrmap.stdlib.arm.elf.format.sections.ELFSymbolSection;

public interface IELFExternResolver {
	public void resolveSymAddr(ELFMerge merge, ELFSymbolSection.ELFSymbol sym);
}
