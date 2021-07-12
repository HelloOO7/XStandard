package ctrmap.stdlib.arm.elf.format.sections;

import ctrmap.stdlib.arm.elf.format.ELFSectionHeader;
import ctrmap.stdlib.io.serialization.BinarySerializer;

public class ELFNullSection extends ELFSection {

	public ELFNullSection(){
		this(new ELFSectionHeader());
		header.alignment = 0;
	}
	
	public ELFNullSection(ELFSectionHeader hdr) {
		super(hdr);
	}

	@Override
	public void serialize(BinarySerializer serializer) {
		
	}

	@Override
	public ELFSection clone() {
		return new ELFNullSection(new ELFSectionHeader(header));
	}

}
