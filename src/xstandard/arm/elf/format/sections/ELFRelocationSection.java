package xstandard.arm.elf.format.sections;

import xstandard.arm.elf.format.ELFSectionHeader;
import xstandard.io.serialization.BinaryDeserializer;
import java.io.IOException;

public class ELFRelocationSection extends ELFRelocationSectionBase<ELFRelocationSectionBase.RelocationEntry> {

	public ELFRelocationSection(String nameWithoutRel) {
		super(".rel" + nameWithoutRel);
		header.type.setSectionType(ELFSectionHeader.ELFSectionType.REL);
	}
	
	public ELFRelocationSection(ELFRelocationSection sec) {
		super(sec);
	}
	
	public ELFRelocationSection(BinaryDeserializer deserializer, ELFSectionHeader shdr) throws IOException {
		super(deserializer, shdr);
	}
		
	@Override
	protected Class<RelocationEntry> getRelocEntryClass() {
		return RelocationEntry.class;
	}

	@Override
	protected int getRelocEntrySize() {
		return RelocationEntry.BYTES;
	}

	@Override
	public ELFSection clone() {
		return new ELFRelocationSection(this);
	}

}
