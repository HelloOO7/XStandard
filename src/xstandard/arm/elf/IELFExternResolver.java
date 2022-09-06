package xstandard.arm.elf;

import xstandard.arm.elf.format.sections.ELFSymbolSection;

public interface IELFExternResolver {
	public void resolveSymAddr(ELFMerge merge, ELFSymbolSection.ELFSymbol sym);
}
