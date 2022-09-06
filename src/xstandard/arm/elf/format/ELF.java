package xstandard.arm.elf.format;

import xstandard.arm.elf.format.sections.ELFAddendRelocationSection;
import xstandard.arm.elf.format.sections.ELFFuncArraySection;
import xstandard.arm.elf.format.sections.ELFGroupSection;
import xstandard.arm.elf.format.sections.ELFNullSection;
import xstandard.arm.elf.format.sections.ELFProgBitsSection;
import xstandard.arm.elf.format.sections.ELFRelocationSection;
import xstandard.arm.elf.format.sections.ELFSection;
import xstandard.arm.elf.format.sections.ELFSymbolSection;
import xstandard.arm.elf.format.sections.ElfNoBitsSection;
import xstandard.fs.FSFile;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.serialization.BinaryDeserializer;
import xstandard.io.serialization.BinarySerializer;
import xstandard.io.serialization.ReferenceType;
import xstandard.util.ArraysEx;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ELF {

	private transient FSFile source;

	public ELFHeader header;

	private List<ELFSection> sections = new ArrayList<>();

	public ELFStringTable strTab;
	public ELFStringTable shStrTab;

	public ELF() {
		header = ELFHeader.makeSimple();

		strTab = new ELFStringTable(header.getOrCreateSection(ELFSectionHeader.ELFSectionType.STRTAB, ".strtab"));
		shStrTab = new ELFStringTable(header.getOrCreateSection(ELFSectionHeader.ELFSectionType.STRTAB, ".shstrtab"));
		
		sections.add(shStrTab);
		sections.add(strTab);
	}

	public ELF(FSFile fsf) {
		try (IOStream io = fsf.getIO()) {
			BinaryDeserializer deserializer = new BinaryDeserializer(io, ByteOrder.LITTLE_ENDIAN, ReferenceType.ABSOLUTE_POINTER);
			header = deserializer.deserialize(ELFHeader.class);

			for (ELFSectionHeader shdr : header.sectionHeaders) {
				deserializer.baseStream.seek(shdr.offset);
				
				switch (shdr.type.getSectionType()) {
					case DYNAMIC:
					case DYNSYM:
					case HASH:
					case NOTE:
					case SHLIB:
					case SYMTAB_SHNDX:
						throw new UnsupportedOperationException("Unsupported section type: " + shdr.type.getSectionType());
					case REL:
						sections.add(new ELFRelocationSection(deserializer, shdr));
						break;
					case RELA:
						sections.add(new ELFAddendRelocationSection(deserializer, shdr));
						break;
					case STRTAB:
						sections.add(new ELFStringTable(deserializer.baseStream, shdr));
						break;
					case SYMTAB:
						sections.add(new ELFSymbolSection(deserializer, shdr));
						break;
					case NOBITS:
						sections.add(new ElfNoBitsSection(shdr));
						break;
					case PROGBITS:
						sections.add(new ELFProgBitsSection(deserializer.baseStream, shdr));
						break;
					case PREINIT_ARRAY:
					case INIT_ARRAY:
					case FINI_ARRAY:
						sections.add(new ELFFuncArraySection(deserializer.baseStream, shdr));
						break;
					case GROUP:
						sections.add(new ELFGroupSection(deserializer.baseStream, shdr));
						break;
					case NULL:
						sections.add(new ELFNullSection(shdr));
						break;
				}
			}

			strTab = (ELFStringTable) getSectionByName(".strtab");
			shStrTab = (ELFStringTable) getSectionByName(".shstrtab");

			for (ELFSymbolSection symSec : sectionsByClass(ELFSymbolSection.class)) {
				for (ELFSymbolSection.ELFSymbol sym : symSec.symbols) {
					sym.name = strTab.getString(sym.nameIdx);
				}
			}

			source = fsf;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public FSFile getSourceFile(){
		return source;
	}

	public Iterable<ELFSection> sections(){
		return sections;
	}
	
	public int addSection(ELFSection sec) {
		if (ArraysEx.addIfNotNullOrContains(sections, sec)) {
			ArraysEx.addIfNotNullOrContains(header.sectionHeaders, sec.header);
			return header.sectionHeaders.size() - 1;
		}
		return -1;
	}
	
	public int getSectionIndex(ELFSection sec){
		return header.sectionHeaders.indexOf(sec.header);
	}
	
	public ELFSection getSectionByIndex(int index){
		ELFSectionHeader hdr = header.sectionHeaders.get(index);
		for (ELFSection sec : sections){
			if (sec.header == hdr){
				return sec;
			}
		}
		return null;
	}
	
	public int getSectionCount(){
		return header.sectionHeaders.size();
	}
	
	private void syncStrTab(){
		strTab.clear();
		for (ELFSymbolSection symSec : sectionsByClass(ELFSymbolSection.class)){
			for (ELFSymbolSection.ELFSymbol smb : symSec.symbols){
				strTab.putString(smb.name);
			}
			for (ELFSymbolSection.ELFSymbol smb : symSec.symbols){
				smb.nameIdx = strTab.getStrIndex(smb.name);
			}
		}
	}

	public void write() {
		write(source);
	}
	
	private List<ELFSection> getSectionsOrderForWriting(){
		//GCC order of writing elf sections
		//This will NOT mess up the section header since the section indices stay the same
		List<ELFSection> l = new ArrayList<>();
		l.addAll(sectionsByClass(ELFProgBitsSection.class));
		l.addAll(sectionsByClass(ElfNoBitsSection.class));
		List<ELFStringTable> stringTables = sectionsByClass(ELFStringTable.class);
		stringTables.remove(shStrTab);
		stringTables.remove(strTab);
		l.addAll(stringTables);
		l.addAll(sectionsByClass(ELFSymbolSection.class));
		l.addAll(sectionsByClass(ELFGroupSection.class));
		l.addAll(sectionsByClass(ELFFuncArraySection.class));
		l.add(strTab);
		l.addAll(sectionsByClass(ELFAddendRelocationSection.class));
		l.addAll(sectionsByClass(ELFRelocationSection.class));
		l.add(shStrTab);
		
		return l;
	}
	
	public void write(FSFile file) {
		if (file != null) {
			source = file;
			try {
				file.setBytes(new byte[0]);

				BinarySerializer serializer = new BinarySerializer(file.getDataIOStream(), header.BOM == 1 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN, ReferenceType.ABSOLUTE_POINTER);
				DataIOStream out = serializer.baseStream; //the serializer wraps its own stream about the parameter

				header.syncShStrTab(shStrTab);
				shStrTab.sortStringsAlphaReverse();
				header.reloadShStrTabIndices(shStrTab);
				syncStrTab();
				
				//Allocate program header bytes
				out.seek(ELFHeader.BYTES);
								
				//Write individual section data
				for (ELFSection section : getSectionsOrderForWriting()){
					section.serialize(serializer);
					serializer.baseStream.pad(section.header.alignment);
				}
				
				for (ELFProgramHeader phdr : header.programHeaders){
					if (phdr.sectionNo_Internal != -1){
						phdr.offset = sections.get(phdr.sectionNo_Internal).header.offset;
					}
				}
				
				header.serializeSubHeaders(serializer);
				
				serializer.baseStream.seek(0);
				header.allowSerializeSubHeaders = false;
				serializer.serialize(header);

				serializer.baseStream.close();
			} catch (IOException ex) {
				Logger.getLogger(ELF.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public <T extends ELFSection> List<T> sectionsByClass(Class<T> cls) {
		List<T> l = new ArrayList<>();
		for (ELFSection sec : sections) {
			if (sec.getClass() == cls) {
				l.add((T) sec);
			}
		}
		return l;
	}

	private ELFSection getSectionByName(String name) {
		for (ELFSection sec : sections) {
			if (Objects.equals(sec.header.name, name)) {
				return sec;
			}
		}
		return null;
	}

	private ELFSection getSectionByType(ELFSectionHeader.ELFSectionType type) {
		for (ELFSection sec : sections) {
			if (sec.header.type.getSectionType() == type) {
				return sec;
			}
		}
		return null;
	}
}
