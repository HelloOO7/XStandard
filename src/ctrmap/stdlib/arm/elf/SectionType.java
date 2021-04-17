package ctrmap.stdlib.arm.elf;

/**
 *
 */
public enum SectionType {
	TEXT, 
	DATA, 
	RODATA, 
	BSS;
	
	public static SectionType getSectionTypeFromElf(String elfSecName) {
		if (elfSecName == null) {
			return null;
		}
		switch (elfSecName) {
			case ".text":
				return TEXT;
			case ".rodata":
				return RODATA;
			case ".data":
				return DATA;
			case ".bss":
				return BSS;
		}
		return null;
	}
}
