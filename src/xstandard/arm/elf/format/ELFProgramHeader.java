package xstandard.arm.elf.format;

import xstandard.io.serialization.annotations.Ignore;
import xstandard.io.serialization.annotations.Size;

public class ELFProgramHeader {

	public static final int PT_LOPROC = 0x70000000;
	public static final int PT_HIPROC = 0x7fffffff;

	public int type;
	@Ignore
	public int sectionNo_Internal = -1;
	public int offset;
	public int vaddr;
	@Size(Short.BYTES)
	public int fileSize;
	@Size(Short.BYTES)
	public int memSize;
	public short flags;
	public short align;

	public void setProgramType(ELFProgramType t) {
		type = type & 0xF0000000;
		type |= t.ordinal();
	}

	public static enum ELFProgramType {
		NULL,
		LOAD,
		DYNAMIC,
		INTERP,
		NOTE,
		SHLIB,
		PHDR
	}
}
