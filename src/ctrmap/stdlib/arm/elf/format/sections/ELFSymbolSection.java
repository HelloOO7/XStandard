package ctrmap.stdlib.arm.elf.format.sections;

import ctrmap.stdlib.arm.elf.format.ELFSectionHeader;
import ctrmap.stdlib.io.serialization.BinaryDeserializer;
import ctrmap.stdlib.io.serialization.BinarySerializer;
import ctrmap.stdlib.io.serialization.ISerializableEnum;
import ctrmap.stdlib.io.serialization.annotations.Ignore;
import ctrmap.stdlib.io.serialization.annotations.Size;
import ctrmap.stdlib.util.ArraysEx;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ELFSymbolSection extends ELFSection {
	
	public List<ELFSymbol> symbols = new ArrayList<>();
	
	public ELFSymbolSection(){
		super(new ELFSectionHeader());
		header.name = ".symtab";
		header.type.setSectionType(ELFSectionHeader.ELFSectionType.SYMTAB);
		header.alignment = 4;
		header.entrySize = ELFSymbol.BYTES;
		
		symbols.add(new ELFSymbol()); //dummy symbol for index 0
	}
	
	public ELFSymbolSection(ELFSymbolSection sec){
		super(new ELFSectionHeader(sec.header));
		
		for (ELFSymbol s : sec.symbols){
			symbols.add(s.clone());
		}
	}
	
	public ELFSymbolSection(BinaryDeserializer deserializer, ELFSectionHeader shdr) throws IOException {
		super(shdr);
		int entryCount = header.size / header.entrySize;

		for (int i = 0; i < entryCount; i++) {
            symbols.add(deserializer.deserialize(ELFSymbol.class));
        }
	}
	
	public void setStrTblSectionNo(int idx){
		header.link = idx;
	}
	
	public int addSymbol(ELFSymbol sym){
		int idx = symbols.size();
		if (ArraysEx.addIfNotNullOrContains(symbols, sym)){
			return idx;
		}
		return -1;
	}
	
	public int getSymIndex(ELFSymbol sym){
		return symbols.indexOf(sym);
	}

	@Override
	public void serialize(BinarySerializer serializer) throws IOException {
		header.offset = serializer.baseStream.getPosition();
		header.size = symbols.size() * header.entrySize;
		header.info = symbols.size();
		for (ELFSymbol smb : symbols){
			serializer.serialize(smb);
		}
	}

	@Override
	public ELFSection clone() {
		return new ELFSymbolSection(this);
	}
	
	public static class ELFSymbol {
		public static final int BYTES = 16;
		
		@Ignore
		public String name;
		
		public int nameIdx;
		public int value;
		public int size;
		
		private byte info;
		public byte other;
		
		@Size(Short.BYTES)
		public int sectionIndex;
		
		public ELFSymbol(){
			
		}
		
		public ELFSymbol(ELFSymbol s){
			name = s.name;
			nameIdx = s.nameIdx;
			value = s.value;
			size = s.size;
			info = s.info;
			other = s.other;
			sectionIndex = s.sectionIndex;
		}
		
		@Override
		public ELFSymbol clone(){
			ELFSymbol s = new ELFSymbol(this);
			return s;
		}
		
		public void setBind(ELFSymbolBind bnd){
			info = (byte)((info & 0xF) | (bnd.ordinal() << 4));
		}
		
		public void setSymType(ELFSymbolType t){
			info = (byte)((info & 0xF0) | (t.ordinal()));
		}
		
		public ELFSymbolBind getBind(){
			return ELFSymbolBind.values()[info >>> 4];
		}
		
		public ELFSymbolType getSymType(){
			return ELFSymbolType.values()[info & 0xF];
		}
		
		public void setSpecialSectionIndex(ELFSpecialSectionIndex idx){
			sectionIndex = idx.getOrdinal();
		}
		
		public ELFSpecialSectionIndex getSpecialSectionIndex(){
			for (ELFSpecialSectionIndex spIdx : ELFSpecialSectionIndex.values()){
				if (spIdx.ordinal == sectionIndex){
					return spIdx;
				}
			}
			return null;
		}
	}
	
	public static enum ELFSpecialSectionIndex implements ISerializableEnum {
		UNDEF(0),
		
		LOPROC(0xFF00),
		HIPROC(0xFF1F),
		
		ABS(0xFFF1),
		
		COMMON(0xFFF2),
		
		HIRESERVE(0xFFFF)
		;
		public final int ordinal;
		
		private ELFSpecialSectionIndex(int ordinal){
			this.ordinal = ordinal;
		}

		@Override
		public int getOrdinal() {
			return ordinal;
		}
	}
	
	public static enum ELFSymbolBind {
		LOCAL,
		GLOBAL,
		WEAK
	}
	
	public static enum ELFSymbolType {
		NOTYPE,
		OBJECT,
		FUNC,
		SECTION,
		FILE
	}
}
