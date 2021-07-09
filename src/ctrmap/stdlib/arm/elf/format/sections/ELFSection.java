package ctrmap.stdlib.arm.elf.format.sections;

import ctrmap.stdlib.arm.elf.format.ELFSectionHeader;
import ctrmap.stdlib.io.serialization.BinarySerializer;
import java.io.IOException;

public abstract class ELFSection {
	public final ELFSectionHeader header;
	
	public ELFSection(ELFSectionHeader hdr){
		header = hdr;
	}
	
	@Override
	public abstract ELFSection clone();
	
	public String getName(){
		return header.name;
	}
	
	public abstract void serialize(BinarySerializer serializer) throws IOException;
}
