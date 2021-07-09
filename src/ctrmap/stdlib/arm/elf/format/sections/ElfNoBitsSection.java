package ctrmap.stdlib.arm.elf.format.sections;

import ctrmap.stdlib.arm.elf.format.ELFSectionHeader;
import ctrmap.stdlib.io.serialization.BinarySerializer;

public class ElfNoBitsSection extends ELFSection {

	public ElfNoBitsSection(String name, int size){
		this(new ELFSectionHeader());
		header.name = name;
		header.size = size;
		header.flags = ELFSectionHeader.SHF_ALLOC | ELFSectionHeader.SHF_WRITE;
		header.type.setSectionType(ELFSectionHeader.ELFSectionType.NOBITS);
	}
	
	public ElfNoBitsSection(ELFSectionHeader hdr) {
		super(hdr);
	}

	@Override
	public void serialize(BinarySerializer serializer) {
		
	}

	@Override
	public ELFSection clone() {
		return new ElfNoBitsSection(new ELFSectionHeader(header));
	}

}
