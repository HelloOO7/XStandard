package ctrmap.stdlib.arm.elf;

/**
 *
 */
public enum SectionType {
	TEXT, 
	DATA, 
	RODATA, 
	BSS,
	EXTERN;
	
	public static SectionType getSectionTypeFromElf(String elfSecName) {
		if (elfSecName == null) {
			return EXTERN;
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
