package ctrmap.stdlib.arm.elf.format.sections;

import ctrmap.stdlib.arm.elf.format.ELFARMRelocationType;
import ctrmap.stdlib.arm.elf.format.ELFSectionHeader;
import ctrmap.stdlib.io.serialization.BinaryDeserializer;
import ctrmap.stdlib.io.serialization.BinarySerializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class ELFRelocationSectionBase<R extends ELFRelocationSectionBase.RelocationEntry> extends ELFSection {
		
	public List<R> entries = new ArrayList<>();
	
	protected ELFRelocationSectionBase(String name){
		super(new ELFSectionHeader());
		header.name = name;
		header.flags = ELFSectionHeader.SHF_INFO_LINK;
		header.alignment = 4;
	}
	
	protected ELFRelocationSectionBase(ELFRelocationSectionBase<R> sec){
		super(new ELFSectionHeader(sec.header));
		for (R entry : sec.entries){
			entries.add((R)entry.clone());
		}
	}
	
	public ELFRelocationSectionBase(BinaryDeserializer deserializer, ELFSectionHeader shdr) throws IOException {
		super(shdr);
		int entryCount = shdr.size / shdr.entrySize;
		deserializer.baseStream.seek(shdr.offset);
		for (int i = 0; i < entryCount; i++){
			entries.add(deserializer.deserialize(getRelocEntryClass()));
		}
	}
	
	public int getRelocatedSegmentNo(){
		return header.info;
	}
	
	public int getSymTabSegmentNo(){
		return header.link;
	}
	
	public void setRelocatedSegmentNo(int segNo){
		header.info = segNo;
	}
	
	public void setSymTabSegmentNo(int segNo){
		header.link = segNo;
	}
	
	protected abstract Class<R> getRelocEntryClass();
	protected abstract int getRelocEntrySize();

	@Override
	public void serialize(BinarySerializer serializer) throws IOException {
		header.offset = serializer.baseStream.getPosition();
		header.entrySize = getRelocEntrySize();
		header.size = entries.size() * header.entrySize;
		for (RelocationEntry e : entries){
			serializer.serialize(e);
		}
	}
	
	public static class RelocationEntry {
		public static final int BYTES = 8;
		
		public int offset;
		protected int info;
		
		public ELFARMRelocationType getRelType(){
			return ELFARMRelocationType.values()[info & 0xFF];
		}
		
		public int getAddend(){
			return 0;
		}
		
		public int getRelSymbol(){
			return info >>> 8;
		}
		
		public void setRelSymbol(int smb){
			if (smb < 0){
				throw new IllegalArgumentException("'smb' can not be negative!");
			}
			info = (info & 0xFF) | (smb << 8);
		}
		
		public void setRelType(ELFARMRelocationType type){
			info = (info & 0xFFFFFF00) | (type.ordinal());
		}
		
		@Override
		public RelocationEntry clone(){
			RelocationEntry e = new RelocationEntry();
			e.offset = offset;
			e.info = info;
			return e;
		}
	}
}
