package xstandard.arm.elf.format.sections;

import xstandard.arm.elf.format.ELFSectionHeader;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.serialization.BinarySerializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ELFFuncArraySection extends ELFSection {

	public List<Integer> funcPointers = new ArrayList<>();

	public ELFFuncArraySection(ELFFuncArraySection sec){
		super(new ELFSectionHeader(sec.header));
		funcPointers.addAll(sec.funcPointers);
	}
	
	public ELFFuncArraySection(DataIOStream io, ELFSectionHeader hdr) throws IOException {
		super(hdr);

		if (hdr.entrySize != 4) {
			throw new UnsupportedOperationException();
		} else {
			for (int i = 0; i < (hdr.size / hdr.entrySize); i++) {
				funcPointers.add(io.readInt());
			}
		}
	}

	@Override
	public void serialize(BinarySerializer serializer) throws IOException {
		header.offset = serializer.baseStream.getPosition();
		header.size = funcPointers.size() * header.entrySize;
		for (int idx : funcPointers) {
			serializer.baseStream.writeInt(idx);
		}
	}

	@Override
	public ELFSection clone() {
		return new ELFFuncArraySection(this);
	}
}
