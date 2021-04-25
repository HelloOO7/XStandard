package ctrmap.stdlib.formats.rpm;

import ctrmap.stdlib.arm.ARMAssembler;
import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.text.FormattingUtils;
import ctrmap.stdlib.io.InvalidMagicException;
import ctrmap.stdlib.io.RandomAccessByteArray;
import ctrmap.stdlib.io.util.BitUtils;
import ctrmap.stdlib.io.util.StringUtils;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ctrmap.stdlib.arm.ThumbAssembler;
import ctrmap.stdlib.gui.file.ExtensionFilter;
import ctrmap.stdlib.io.base.LittleEndianIO;
import ctrmap.stdlib.io.iface.SeekableDataOutput;
import ctrmap.stdlib.io.structs.StringTable;
import java.util.Objects;

public class RPM {

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Relocatable Patch Module", "*.rpm");

	public static final String RPM_MAGIC = "RPM";
	public static final String SYM_MAGIC = "SYM0";
	public static final String STR_MAGIC = "STR0";
	public static final String REL_MAGIC = "REL0";
	
	public static final int RPM_PADDING = 0x10;

	public int baseAddress;
	public List<RPMSymbol> symbols = new ArrayList<>();
	public List<RPMRelocation> relocations = new ArrayList<>();

	private RPMExternalSymbolResolver extResolver;

	private RandomAccessByteArray code;

	public RPM(FSFile fsf) {
		this(fsf.getBytes());
	}

	public RPM(byte[] bytes) {
		try {
			RandomAccessByteArray ft = new RandomAccessByteArray(bytes);
			ft.seek(bytes.length - 16);
			if (!StringUtils.checkMagic(ft, RPM_MAGIC)) {
				throw new InvalidMagicException("Not an RPM file!");
			}
			int version = ft.read() - '0';
			int symbolsOffset = ft.readInt();
			int relocationsOffset = ft.readInt();
			int codeSize = ft.readInt();

			ft.seek(symbolsOffset);
			if (!StringUtils.checkMagic(ft, SYM_MAGIC)) {
				throw new InvalidMagicException("SYM section not present!");
			}
			int symbolCount = ft.readUnsignedShort();
			for (int i = 0; i < symbolCount; i++) {
				symbols.add(new RPMSymbol(this, ft, version));
			}

			ft.seek(relocationsOffset);
			if (!StringUtils.checkMagic(ft, REL_MAGIC)) {
				throw new InvalidMagicException("REL section not present!");
			}
			baseAddress = ft.readInt();
			int relocationCount = ft.readInt();
			for (int i = 0; i < relocationCount; i++) {
				relocations.add(new RPMRelocation(ft, this));
			}

			code = new RandomAccessByteArray(Arrays.copyOf(bytes, codeSize));
		} catch (IOException ex) {
			Logger.getLogger(RPM.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public RPM() {
		code = new RandomAccessByteArray();
	}

	public static boolean isRPM(FSFile f) {
		if (f.isFile() && f.length() > 0x10) {
			try {
				LittleEndianIO io = f.getIO();

				io.seek(f.length() - 16);
				boolean rsl = StringUtils.checkMagic(io, RPM_MAGIC);

				io.close();
				return rsl;
			} catch (IOException ex) {
				Logger.getLogger(RPM.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return false;
	}

	public void merge(RPM source) {
		try {
			int base = code.length();
			code.seek(base);
			code.write(source.code.toByteArray());
			
			for (RPMSymbol sym : symbols) {
				if (sym.address.isNull()){
					RPMSymbol newSym = source.getSymbol(sym.name);
					if (newSym != null){
						sym.address = new RPMSymbolAddress(this, newSym.address);
					}
				}
			}

			for (RPMSymbol symbol : source.symbols) {
				RPMSymbol newSymbol = new RPMSymbol(this, symbol);
				RPMSymbol existingSymbol = getSymbol(newSymbol.name);
				if (existingSymbol != null){
					continue;
				}
				if (newSymbol.address.getAddrType() == RPMSymbolAddress.RPMAddrType.LOCAL) {
					newSymbol.address.setAddr(base + newSymbol.address.getAddr());
				}
				symbols.add(newSymbol);
			}

			for (RPMRelocation rel : source.relocations) {
				RPMRelocation newRel = new RPMRelocation(this, rel);
				if (newRel.target.isInternal()) {
					newRel.target.address += base;
				}
				relocations.add(rel);
			}
		} catch (IOException ex) {
			Logger.getLogger(RPM.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void writeMAPToFile(FSFile fsf) {
		PrintStream out = new PrintStream(fsf.getOutputStream());

		for (RPMSymbol symb : symbols) {
			if (symb.name != null && !symb.name.isEmpty()) {
				int addr = symb.address.getAddrAbs();
				if (addr < baseAddress || addr > baseAddress + code.length()) {
					continue; //external symbol
				}

				if (symb.type == RPMSymbolType.FUNCTION_THM) {
					addr++;
				}

				out.print("0000:");
				out.print(FormattingUtils.getStrWithLeadingZeros(8, Integer.toHexString(addr)));
				out.print("        ");
				out.println(symb.name);
			}
		}

		out.close();
	}

	public List<RPMRelocation> getExternalRelocations() {
		List<RPMRelocation> l = new ArrayList<>();
		for (RPMRelocation r : relocations) {
			if (r.target.isExternal()) {
				l.add(r);
			}
		}
		return l;
	}

	public void setExternalRelocations(List<RPMRelocation> l) {
		for (int i = 0; i < relocations.size(); i++) {
			if (relocations.get(i).target.isExternal()) {
				relocations.remove(i);
				i--;
			}
		}
		relocations.addAll(l);
	}

	public void setCode(RandomAccessByteArray buf) {
		code = buf;
	}

	public byte[] getBytes() {
		try {
			RandomAccessByteArray ba = new RandomAccessByteArray();
			ba.write(code.toByteArray());
			int codeSize = ba.getPosition();
			ba.pad(RPM_PADDING);

			StringTable strings = new StringTable(ba);

			int symbOffset = ba.getPosition();
			ba.writeStringUnterminated(SYM_MAGIC);
			ba.writeShort(symbols.size());
			for (RPMSymbol sym : symbols) {
				sym.write(ba);
			}
			ba.pad(RPM_PADDING);

			int relOffset = ba.getPosition();
			ba.writeStringUnterminated(REL_MAGIC);
			ba.writeInt(baseAddress);
			ba.writeInt(relocations.size());
			for (RPMRelocation rel : relocations) {
				rel.write(ba, strings);
			}
			ba.pad(RPM_PADDING);

			ba.writeStringUnterminated(STR_MAGIC);
			strings.writeTable();
			ba.pad(RPM_PADDING);

			ba.writeStringUnterminated(RPM_MAGIC);
			ba.write(RPMRevisions.REV_CURRENT + '0');
			ba.writeInt(symbOffset);
			ba.writeInt(relOffset);
			ba.writeInt(codeSize);
			ba.close();
			return ba.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(RPM.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public void setBaseAddrNoUpdateBytes(int baseAddress) {
		this.baseAddress = baseAddress;
	}

	public void updateBytesForBaseAddr() {
		relocateBufferToAddr(baseAddress);
	}

	public void doExternalRelocations(RPMExternalRelocator relocator) {
		for (RPMRelocation rel : relocations) {
			if (rel.target.isExternal()) {
				relocator.processExternalRelocation(this, rel);
			}
		}
	}

	private void relocateBufferToAddr(int baseAddress) {
		this.baseAddress = baseAddress;
		code.setBase(baseAddress);
		try {
			for (RPMRelocation rel : relocations) {
				if (rel.target.isInternal()) {
					//System.out.println("rel " + rel.target.address + ", " + rel.target.module);
					code.seekUnbased(rel.target.address);

					writeRelocationDataByType(this, rel, code);
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(RPM.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void writeRelocationDataByType(RPM rpm, RPMRelocation rel, SeekableDataOutput out) throws IOException {
		int addr = rel.source.getWritableAddress();

		switch (rel.targetType) {
			//TODO BLX when address is not from the same instruction set (&1 != 0)

			case ARM_BRANCH_LINK:
				ARMAssembler.writeBranchInstruction(out, addr, true);
				break;
			case THUMB_BRANCH_LINK:
				ThumbAssembler.writeBranchLinkInstruction(out, addr);
				break;
			case OFFSET:
				out.writeInt(addr);
				break;
			case ARM_BRANCH:
				ARMAssembler.writeBranchInstruction(out, addr, false);
				break;
			case THUMB_BRANCH:
				if (Math.abs((out.getPosition() + 4) - addr) < 2048) {
					ThumbAssembler.writeSmallBranchInstruction(out, addr);
				} else {
					int tgtAddr = ThumbAssembler.writePcRelativeLoadAbs(out, 0, out.getPosition() + 4);
					ThumbAssembler.writeBXInstruction(out, 0);
					System.out.println("load rsl " + Integer.toHexString(tgtAddr));
					out.seek(tgtAddr);
					out.writeInt(addr);
				}
				break;
			case FULL_COPY:
				if (rel.sourceType == RPMRelocation.RPMRelSourceType.SYMBOL_INTERNAL) {
					int copyStartAdr = rel.source.getAddress();
					int copyEndAdr;

					int len = rel.source.getLength();
					if (len > 0) {
						copyEndAdr = copyStartAdr + len;
						rpm.code.seekUnbased(copyStartAdr);
						byte[] bytes = new byte[len];
						rpm.code.read(bytes);
						out.seek(addr);
						out.write(bytes);

						for (RPMRelocation copyRel : rpm.relocations) {
							if (copyRel.target.isInternal() && copyRel.targetType != RPMRelocation.RPMRelTargetType.FULL_COPY) {
								int copyRelAddr = copyRel.target.address;
								if (copyRelAddr >= copyStartAdr && copyRelAddr < copyEndAdr) {
									out.seek(addr + (copyRelAddr - copyStartAdr));
									writeRelocationDataByType(rpm, copyRel, out);
								}
							}
						}
					} else {
						throw new UnsupportedOperationException("Can not FULL_COPY a symbol without a length!");
					}
				} else {
					throw new UnsupportedOperationException("Can not FULL_COPY and external symbol!");
				}
				break;
		}
	}

	public RPMSymbol getSymbol(String symbName) {
		for (RPMSymbol s : symbols) {
			if (Objects.equals(s.name, symbName)) {
				return s;
			}
		}
		return null;
	}

	public RPMSymbol getSymbol(int symbNo) {
		if (symbNo < symbols.size() && symbNo >= 0) {
			return symbols.get(symbNo);
		}
		return null;
	}

	public int getSymbolNo(RPMSymbol symb) {
		return symbols.indexOf(symb);
	}

	public RPMSymbol getExternalSymbol(String namespace, String name) {
		return extResolver.resolveExSymbol(namespace, name);
	}
}
