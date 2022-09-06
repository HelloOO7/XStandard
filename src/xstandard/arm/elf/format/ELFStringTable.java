package xstandard.arm.elf.format;

import xstandard.arm.elf.format.sections.ELFSection;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.serialization.BinarySerializer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ELFStringTable extends ELFSection {

	private transient StringBuilder strings;

	private transient List<String> stringList = new ArrayList<>();
	
	public ELFStringTable(ELFStringTable strtab){
		super(new ELFSectionHeader(strtab.header));
		strings = new StringBuilder(strtab.strings.toString());
		stringList.addAll(strtab.stringList);
	}

	public ELFStringTable(DataIOStream io, ELFSectionHeader header) throws IOException {
		super(header);
		io.seek(header.offset);
		byte[] buf = new byte[header.size];
		io.read(buf);
		strings = new StringBuilder(new String(buf, StandardCharsets.US_ASCII));
		makeStrList();
	}

	public ELFStringTable(ELFSectionHeader header) {
		super(header);
		header.type.setSectionType(ELFSectionHeader.ELFSectionType.STRTAB);
		clear();
	}

	public ELFStringTable() {
		super(new ELFSectionHeader());
	}
	
	public Iterable<String> strings(){
		return stringList;
	}

	public void clear() {
		strings = new StringBuilder();
		stringList.clear();
	}

	private void makeStrList() {
		stringList.clear();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < strings.length(); i++) {
			char c = strings.charAt(i);
			if (c == 0) {
				String str = sb.toString();
				if (str.isEmpty()) {
					str = null;
				}
				stringList.add(str);
				sb = new StringBuilder();
			} else {
				sb.append(c);
			}
		}
	}

	String getString(int index) {
		StringBuilder out = new StringBuilder();
		for (int i = index; i < strings.length(); i++) {
			char c = strings.charAt(i);
			if (c != 0) {
				out.append(c);
			} else {
				break;
			}
		}
		if (out.length() == 0) {
			return null;
		}
		return out.toString();
	}

	public void putString(String str) {
		if (str == null) {
			if (strings.length() == 0 || strings.charAt(0) != 0) {
				strings.insert(0, (char) 0);
			}
			return;
		}
		String request = str + "\u0000";
		int index = strings.indexOf(request);
		if (index == -1) {
			for (int i = 0; i < stringList.size(); i++) {
				String existing = stringList.get(i);
				if (existing != null) {
					if (request.endsWith(existing)) {
						stringList.remove(i);
						i--;

						int indexOfExisting = strings.indexOf(existing);
						strings.delete(indexOfExisting, indexOfExisting + existing.length() + 1);
					}
				}
			}
			strings.append(request);
			stringList.add(str);
		}
	}

	public int getStrIndex(String str) {
		if (str == null) {
			return 0;
		}
		int idx = strings.indexOf(str + "\u0000");
		if (idx == -1) {
			throw new RuntimeException("String not found: " + str + " | String table dump:\n" + strings.toString().replace((char)0, '\n'));
		}
		return idx;
	}

	public void sortStringsAlphaReverse() {
		makeStrList();

		boolean hasNull = stringList.remove(null);

		Collections.sort(stringList);
		Collections.reverse(stringList);

		strings = new StringBuilder();
		if (hasNull) {
			strings.append((char) 0);
		}
		for (String str : stringList) {
			strings.append(str);
			strings.append((char) 0);
		}
	}

	@Override
	public void serialize(BinarySerializer serializer) throws IOException {
		header.offset = serializer.baseStream.getPosition();
		header.size = strings.length();

		serializer.baseStream.writeStringUnterminated(strings.toString());

		//Seems like GCC forces 4-byte alignment here, despite the section header
		serializer.baseStream.align(4);
	}

	@Override
	public ELFSection clone() {
		return new ELFStringTable(this);
	}
}
