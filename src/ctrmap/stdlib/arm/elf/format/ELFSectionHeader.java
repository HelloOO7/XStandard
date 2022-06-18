package ctrmap.stdlib.arm.elf.format;

import ctrmap.stdlib.io.serialization.ISerializableEnum;
import ctrmap.stdlib.io.serialization.annotations.Ignore;
import ctrmap.stdlib.io.serialization.annotations.Inline;
import java.util.HashMap;
import java.util.Map;

public class ELFSectionHeader {

	public static final int SHF_WRITE = 1;
	public static final int SHF_ALLOC = 2;
	public static final int SHF_EXECINSTR = 4;
	public static final int SHF_MERGE = 16;
	public static final int SHF_STRINGS = 32;
	public static final int SHF_INFO_LINK = 64;
	public static final int SHF_LINK_ORDER = 128;
	public static final int SHF_OS_NONCONFORMING = 256;
	public static final int SHF_GROUP = 512;
	public static final int SHF_TLS = 1024;
	public static final int SHF_COMPRESSED = 2048;
	public static final int SHF_MASKPROC = 0xF0000000;

	public static final int SHT_LOPROC = 0x70000000;
	public static final int SHT_HIPROC = 0x7fffffff;

	public static final long SHT_LOUSER = 0x80000000L;
	public static final long SHT_HIUSER = 0xffffffffL;

	@Ignore
	public String name;
	
	int nameIdx;
	public SectionType type = new SectionType();
	public int flags;

	public int loadAddr;
	
	public int offset;
	public int size;
	
	public int link;
	public int info;
	
	public int alignment = 1;
	public int entrySize;
	
	public ELFSectionHeader(){
		
	}
	
	public ELFSectionHeader(ELFSectionHeader src){
		name = src.name;
		type = new SectionType(src.type);
		flags = src.flags;
		loadAddr = src.loadAddr;
		offset = src.offset;
		size = src.size;
		link = src.link;
		info = src.info;
		alignment = src.alignment;
		entrySize = src.entrySize;
	}

	@Inline
	public static class SectionType {

		private int value;
		
		public SectionType(){
			
		}
		
		public SectionType(SectionType st){
			value = st.value;
		}

		public ELFSectionType getSectionType() {
			return ELFSectionType.valueOf(value & ~SHF_MASKPROC);
		}
		
		public void setSectionType(ELFSectionType t){
			value = (value & SHF_MASKPROC) | (t.ordinal());
		}

		public boolean isProc() {
			return value >= SHT_LOPROC && value <= SHT_HIPROC;
		}
		
		public void setProc(){
			value = (value & ~SHF_MASKPROC) | 0x70000000;
		}
		
		public void setUser(){
			value = (value & ~SHF_MASKPROC) | 0x80000000;
		}

		public boolean isUser() {
			long v = Integer.toUnsignedLong(value);
			return v >= SHT_LOUSER && v <= SHT_HIUSER;
		}
	}

	public static enum ELFSectionType implements ISerializableEnum {
		NULL,
		PROGBITS,
		SYMTAB,
		STRTAB,
		RELA,
		HASH,
		DYNAMIC,
		NOTE,
		NOBITS,
		REL,
		SHLIB,
		DYNSYM,
		INIT_ARRAY(14),
		FINI_ARRAY(15),
		PREINIT_ARRAY(16),
		GROUP(17),
		SYMTAB_SHNDX(18);
		
		public final int ordinal;
		
		private static final Map<Integer, ELFSectionType> TYPE_MAP = new HashMap<>();
		
		static {
			for (ELFSectionType t : values()) {
				TYPE_MAP.put(t.getOrdinal(), t);
			}
		}
		
		public static ELFSectionType valueOf(int index) {
			return TYPE_MAP.get(index);
		}
		
		private ELFSectionType(){
			ordinal = -1;
		}
		
		private ELFSectionType(int ordinal){
			this.ordinal = ordinal;
		}

		@Override
		public int getOrdinal() {
			if (ordinal == -1){
				return ordinal();
			}
			return ordinal;
		}
	}
}
