package ctrmap.stdlib.arm.elf.format.sections;

import ctrmap.stdlib.arm.elf.format.ELFSectionHeader;
import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import ctrmap.stdlib.io.serialization.BinarySerializer;
import java.io.IOException;

public class ELFProgBitsSection extends ELFSection {

	public transient DataIOStream programImage;
	
	public ELFProgBitsSection(ELFProgBitsSection sec){
		super(new ELFSectionHeader(sec.header));
		programImage = new DataIOStream(sec.programImage.toByteArray());
	}
	
	public ELFProgBitsSection(String name, DataIOStream programImage){
		super(new ELFSectionHeader());
		header.name = name;
		header.alignment = 4;
		header.flags = ELFSectionHeader.SHF_ALLOC;
		header.size = programImage.getLength();
		header.type.setSectionType(ELFSectionHeader.ELFSectionType.PROGBITS);
		this.programImage = programImage;
	}
	
	public ELFProgBitsSection(DataIOStream io, ELFSectionHeader hdr) throws IOException {
		super(hdr);
		byte[] data = new byte[hdr.size];
		io.read(data);
		programImage = new DataIOStream(data);
	}

	@Override
	public void serialize(BinarySerializer serializer) throws IOException {
		header.offset = serializer.baseStream.getPosition();
		header.size = programImage.getLength();
		serializer.baseStream.write(programImage.toByteArray());
	}

	@Override
	public ELFSection clone() {
		return new ELFProgBitsSection(this);
	}

}
