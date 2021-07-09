package ctrmap.stdlib.arm.elf.format.sections;

import ctrmap.stdlib.arm.elf.format.ELFSectionHeader;
import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import ctrmap.stdlib.io.serialization.BinarySerializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ELFGroupSection extends ELFSection {
	public static final int GRP_COMDAT = 1;

	public int flags;
	public List<Integer> sectionIndices = new ArrayList<>();
	
	public ELFGroupSection(ELFGroupSection sec){
		super(new ELFSectionHeader(sec.header));
		flags = sec.flags;
		sectionIndices.addAll(sec.sectionIndices);
	}
	
	public ELFGroupSection(DataIOStream io, ELFSectionHeader hdr) throws IOException {
		super(hdr);
		
		if (hdr.entrySize != 4){
			throw new UnsupportedOperationException();
		}
		else {
			flags = io.readInt();
			int count = (hdr.size - 4 / hdr.entrySize);
			for (int i = 0; i < count; i++){
				sectionIndices.add(io.readInt());
			}
		}
	}

	@Override
	public void serialize(BinarySerializer serializer) throws IOException {
		header.offset = serializer.baseStream.getPosition();
		header.size = sectionIndices.size() * header.entrySize + Integer.BYTES;
		serializer.baseStream.writeInt(flags);
		for (int idx : sectionIndices){
			serializer.baseStream.writeInt(idx);
		}
	}

	@Override
	public ELFSection clone() {
		return new ELFGroupSection(this);
	}
}
