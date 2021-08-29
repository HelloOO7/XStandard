package ctrmap.stdlib.arm.elf;

import ctrmap.stdlib.arm.elf.format.ELFSectionHeader;

/**
 *
 */
public enum SectionType {
	TEXT,
	BSS,
	EXTERN;

	public static SectionType getSectionTypeFromElf(ELFSectionHeader hdr) {
		if (hdr.name == null) {
			return EXTERN;
		}
		if ((hdr.flags & ELFSectionHeader.SHF_ALLOC) != 0) {
			switch (hdr.type.getSectionType()) {
				case NOBITS:
					return BSS;
				case PROGBITS:
					return TEXT;
			}
		}
		return null;
	}
}
