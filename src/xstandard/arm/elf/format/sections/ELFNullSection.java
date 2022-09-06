package xstandard.arm.elf.format.sections;

import xstandard.arm.elf.format.ELFSectionHeader;
import xstandard.io.serialization.BinarySerializer;

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
