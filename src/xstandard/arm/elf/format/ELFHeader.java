package xstandard.arm.elf.format;

import xstandard.io.serialization.BinaryDeserializer;
import xstandard.io.serialization.BinarySerializer;
import xstandard.io.serialization.ICustomSerialization;
import xstandard.io.serialization.ISerializableEnum;
import xstandard.io.serialization.annotations.ArraySize;
import xstandard.io.serialization.annotations.ByteOrderMark;
import xstandard.io.serialization.annotations.Ignore;
import xstandard.io.serialization.annotations.Inline;
import xstandard.io.serialization.annotations.MagicStr;
import xstandard.io.serialization.annotations.ObjSize;
import xstandard.io.serialization.annotations.Size;
import xstandard.io.serialization.annotations.Version;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Inline
public class ELFHeader implements ICustomSerialization {

	public static final int BYTES = 0x34;

	@Ignore
	public List<ELFProgramHeader> programHeaders;
	@Ignore
	public List<ELFSectionHeader> sectionHeaders;

	@MagicStr("\u007fELF")
	public String magic = "\u007fELF";
	public ELFObjectSize objectSize = ELFObjectSize.CLASS_32;
	@ByteOrderMark(ifBE = 2, ifLE = 1)
	public byte BOM = 1;
	@Version
	public byte fileVersion = 1;
	public ELFOSABI abi = ELFOSABI.NONE;
	public byte abiVersion;

	@Inline
	@ArraySize(7)
	private byte[] padding;

	public ELFType type;
	public ELFMachine machine; //really not going to list all 243 machines
	public ELFVersion version = ELFVersion.CURRENT;

	public int entryPoint;

	private int programHeadersOff;
	private int sectionHeadersOff;

	public int flags;

	@ObjSize
	private short headerSize;

	private short programHeaderEntrySize = 0x20;
	private short programHeaderCount;
	private short sectionHeaderEntrySize = 0x28;
	private short sectionHeaderCount;

	private short shStrSectionIndex = -1;

	@Ignore
	public boolean allowSerializeSubHeaders = true;

	public ELFHeader() {
		programHeaders = new ArrayList<>();
		sectionHeaders = new ArrayList<>();
		padding = new byte[7];
	}

	public static ELFHeader makeSimple() {
		ELFHeader hdr = new ELFHeader();

		ELFSectionHeader sec0 = new ELFSectionHeader();
		hdr.sectionHeaders.add(sec0);
		ELFSectionHeader shStrTab = new ELFSectionHeader();
		shStrTab.name = ".shstrtab";
		shStrTab.type.setSectionType(ELFSectionHeader.ELFSectionType.STRTAB);
		hdr.sectionHeaders.add(shStrTab);
		hdr.shStrSectionIndex = (short) hdr.sectionHeaders.indexOf(shStrTab);

		return hdr;
	}

	public ELFSectionHeader getOrCreateSection(ELFSectionHeader.ELFSectionType type, String name) {
		for (ELFSectionHeader hdr : sectionHeaders) {
			if (hdr.type.getSectionType() == type && Objects.equals(hdr.name, name)) {
				return hdr;
			}
		}
		ELFSectionHeader hdr = new ELFSectionHeader();
		hdr.type.setSectionType(type);
		hdr.name = name;
		sectionHeaders.add(hdr);
		return hdr;
	}
	
	public boolean isShStrTab(int sectIndex){
		return sectIndex == shStrSectionIndex;
	}

	@Override
	public void deserialize(BinaryDeserializer deserializer) throws IOException {
		int pos = deserializer.baseStream.getPosition();
		if (programHeadersOff != 0) {
			deserializer.baseStream.seek(programHeadersOff);
			for (int i = 0; i < programHeaderCount; i++) {
				programHeaders.add(deserializer.deserialize(ELFProgramHeader.class));
			}
		}

		if (sectionHeadersOff != 0) {
			deserializer.baseStream.seek(sectionHeadersOff);
			for (int i = 0; i < sectionHeaderCount; i++) {
				ELFSectionHeader shdr = deserializer.deserialize(ELFSectionHeader.class);
				sectionHeaders.add(shdr);
			}
		}

		ELFStringTable shStr = new ELFStringTable(deserializer.baseStream, sectionHeaders.get(shStrSectionIndex));
		for (ELFSectionHeader shdr : sectionHeaders) {
			shdr.name = shStr.getString(shdr.nameIdx);
		}

		deserializer.baseStream.seek(pos);
	}

	@Override
	public boolean preSerialize(BinarySerializer serializer) throws IOException {
		ELFSectionHeader shStrTab = getShdrByName(".shstrtab");
		if (shStrTab == null) {
			throw new RuntimeException("No .shstrtab!");
		}
		shStrSectionIndex = (short) (sectionHeaders.indexOf(shStrTab));

		programHeaderCount = (short) programHeaders.size();
		sectionHeaderCount = (short) sectionHeaders.size();

		if (allowSerializeSubHeaders) {
			programHeadersOff = programHeaderCount == 0 ? 0 : BYTES;
			sectionHeadersOff = sectionHeaderCount == 0 ? 0 : (BYTES + programHeaderCount * programHeaderEntrySize);
		}

		return false;
	}

	@Override
	public void postSerialize(BinarySerializer serializer) throws IOException {
		if (allowSerializeSubHeaders) {
			serializeSubHeaders(serializer);
		}
	}

	public void serializeSubHeaders(BinarySerializer serializer) throws IOException {
		if (!programHeaders.isEmpty()) {
			programHeadersOff = serializer.baseStream.getPosition();
			for (ELFProgramHeader phdr : programHeaders) {
				serializer.serialize(phdr);
			}
		}
		if (!sectionHeaders.isEmpty()) {
			sectionHeadersOff = serializer.baseStream.getPosition();
			for (ELFSectionHeader shdr : sectionHeaders) {
				serializer.serialize(shdr);
			}
		}
	}

	public void syncShStrTab(ELFStringTable shStrTab) {
		shStrTab.clear();
		for (ELFSectionHeader hdr : sectionHeaders) {
			shStrTab.putString(hdr.name);
		}
		reloadShStrTabIndices(shStrTab);
	}

	public void reloadShStrTabIndices(ELFStringTable shStrTab) {
		for (ELFSectionHeader hdr : sectionHeaders) {
			hdr.nameIdx = shStrTab.getStrIndex(hdr.name);
		}
	}

	public int getFullAllocSize() {
		int size = BYTES;
		size += programHeaders.size() * programHeaderEntrySize;
		size += sectionHeaders.size() * sectionHeaderEntrySize;
		return size;
	}

	public ELFSectionHeader getShdrByName(String name) {
		for (ELFSectionHeader hdr : sectionHeaders) {
			if (Objects.equals(hdr.name, name)) {
				return hdr;
			}
		}
		return null;
	}

	public static enum ELFObjectSize {
		CLASS_NONE,
		CLASS_32,
		CLASS_64
	}

	public static enum ELFOSABI {
		NONE,
		HPUX,
		NETBSD,
		GNU,
		LINUX,
		SOLARIS,
		AIX,
		IRIX,
		FREEBSD,
		TRU64,
		MODESTO,
		OPENBSD,
		OPENVMS,
		NSK,
		AROS,
		FENIXOS,
		CLOUDABI,
		OPENVOS
	}

	@Size(Short.BYTES)
	public static enum ELFType implements ISerializableEnum {
		NONE,
		REL,
		EXEC,
		DYN,
		CORE,
		LOOS(0xFE00),
		HIOS(0xFEFF),
		LOPROC(0xFF00),
		HIPROC(0xFFFF);

		public final int ordinal;

		private ELFType() {
			ordinal = -1;
		}

		private ELFType(int ordinal) {
			this.ordinal = ordinal;
		}

		@Override
		public int getOrdinal() {
			if (ordinal == -1) {
				return ordinal();
			}
			return ordinal;
		}
	}

	@Size(Short.BYTES)
	public static enum ELFMachine implements ISerializableEnum {
		NONE(0),
		X86(3),
		PPC(20),
		ARM(40);
		public final int ordinal;

		private ELFMachine(int ordinal) {
			this.ordinal = ordinal;
		}

		@Override
		public int getOrdinal() {
			if (ordinal == -1) {
				return ordinal();
			}
			return ordinal;
		}
	}

	@Size(Integer.BYTES)
	public static enum ELFVersion {
		NONE,
		CURRENT
	}
}
