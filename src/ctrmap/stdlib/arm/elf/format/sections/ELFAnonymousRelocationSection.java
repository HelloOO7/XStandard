package ctrmap.stdlib.arm.elf.format.sections;

import ctrmap.stdlib.arm.elf.format.ELFSectionHeader;
import ctrmap.stdlib.io.serialization.BinaryDeserializer;
import java.io.IOException;

public class ELFAnonymousRelocationSection extends ELFRelocationSectionBase<ELFAnonymousRelocationSection.AnonymousRelocationEntry> {

	public ELFAnonymousRelocationSection(String nameWithoutRel) {
		super(".rela" + nameWithoutRel);
		header.type.setSectionType(ELFSectionHeader.ELFSectionType.RELA);
	}
	
	public ELFAnonymousRelocationSection(ELFAnonymousRelocationSection sec){
		super(sec);
	}
	
	public ELFAnonymousRelocationSection(BinaryDeserializer deserializer, ELFSectionHeader shdr) throws IOException {
		super(deserializer, shdr);
	}
		
	@Override
	protected Class<AnonymousRelocationEntry> getRelocEntryClass() {
		return AnonymousRelocationEntry.class;
	}

	@Override
	protected int getRelocEntrySize() {
		return AnonymousRelocationEntry.BYTES;
	}

	@Override
	public ELFSection clone() {
		return new ELFAnonymousRelocationSection(this);
	}

	public static class AnonymousRelocationEntry extends ELFRelocationSection.RelocationEntry {
		public static final int BYTES = 12;

		public int addend;
		
		@Override
		public AnonymousRelocationEntry clone(){
			AnonymousRelocationEntry e = new AnonymousRelocationEntry();
			e.offset = offset;
			e.addend = addend;
			e.info = info;
			return e;
		}
	}
}
