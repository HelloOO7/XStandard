package xstandard.arm.elf.format.sections;

import xstandard.arm.elf.format.ELFSectionHeader;
import xstandard.io.serialization.BinaryDeserializer;
import java.io.IOException;

public class ELFAddendRelocationSection extends ELFRelocationSectionBase<ELFAddendRelocationSection.AddendRelocationEntry> {

	public ELFAddendRelocationSection(String nameWithoutRel) {
		super(".rela" + nameWithoutRel);
		header.type.setSectionType(ELFSectionHeader.ELFSectionType.RELA);
	}
	
	public ELFAddendRelocationSection(ELFAddendRelocationSection sec){
		super(sec);
	}
	
	public ELFAddendRelocationSection(BinaryDeserializer deserializer, ELFSectionHeader shdr) throws IOException {
		super(deserializer, shdr);
	}
		
	@Override
	protected Class<AddendRelocationEntry> getRelocEntryClass() {
		return AddendRelocationEntry.class;
	}

	@Override
	protected int getRelocEntrySize() {
		return AddendRelocationEntry.BYTES;
	}

	@Override
	public ELFSection clone() {
		return new ELFAddendRelocationSection(this);
	}

	public static class AddendRelocationEntry extends ELFRelocationSection.RelocationEntry {
		public static final int BYTES = 12;

		public int addend;
		
		@Override
		public int getAddend(){
			return addend;
		}
		
		@Override
		public AddendRelocationEntry clone(){
			AddendRelocationEntry e = new AddendRelocationEntry();
			e.offset = offset;
			e.addend = addend;
			e.info = info;
			return e;
		}
	}
}
