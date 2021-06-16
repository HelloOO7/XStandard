package ctrmap.stdlib.formats.rpm;

import ctrmap.stdlib.arm.ARMAssembler;
import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.text.FormattingUtils;
import ctrmap.stdlib.io.InvalidMagicException;
import ctrmap.stdlib.io.util.BitUtils;
import ctrmap.stdlib.io.util.StringIO;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ctrmap.stdlib.arm.ThumbAssembler;
import ctrmap.stdlib.gui.file.ExtensionFilter;
import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import ctrmap.stdlib.io.structs.StringTable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RPM {

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Relocatable Patch Module", "*.rpm");

	public static final String RPM_MAGIC = "RPM";
	public static final String SYM_MAGIC = "SYM0";
	public static final String STR_MAGIC = "STR0";
	public static final String REL_MAGIC = "REL0";

	public static final int RPM_PADDING = 0x10;

	public static final int RPM_FOOTER_SIZE_LEGACY = 0x10;
	public static final int RPM_FOOTER_SIZE = 0x20;

	public String productId;
	public int productVersion;

	public int baseAddress;
	public List<RPMSymbol> symbols = new ArrayList<>();
	public List<RPMRelocation> relocations = new ArrayList<>();

	private RPMExternalSymbolResolver extResolver;

	private DataIOStream code;

	public RPM(FSFile fsf) {
		this(fsf.getBytes());
	}
	
	public RPM(DataIOStream io, int startPos, int endPos){
		try {
			io.seek(endPos - RPM_FOOTER_SIZE);
			if (!StringIO.checkMagic(io, RPM_MAGIC)) {
				io.seek(endPos - RPM_FOOTER_SIZE_LEGACY);
				if (!StringIO.checkMagic(io, RPM_MAGIC)) {
					throw new InvalidMagicException("Not an RPM file!");
				}
			}
			int version = io.read();
			if (version != 0xFF) {
				version = version - '0';
			} else {
				version = io.readInt();
				if (version >= RPMRevisions.REV_PRODUCT_INFO) {
					productId = StringIO.readStringWithAddress(io);
					productVersion = io.readInt();
				}
				else {
					io.skipBytes(8);
				}
			}
			int symbolsOffset = io.readInt();
			int relocationsOffset = io.readInt();
			int codeSize = io.readInt();

			io.seek(symbolsOffset);
			if (!StringIO.checkMagic(io, SYM_MAGIC)) {
				throw new InvalidMagicException("SYM section not present!");
			}
			int symbolCount = io.readUnsignedShort();
			for (int i = 0; i < symbolCount; i++) {
				symbols.add(new RPMSymbol(this, io, version));
			}

			io.seek(relocationsOffset);
			if (!StringIO.checkMagic(io, REL_MAGIC)) {
				throw new InvalidMagicException("REL section not present!");
			}
			baseAddress = io.readInt();
			int relocationCount = io.readInt();
			for (int i = 0; i < relocationCount; i++) {
				relocations.add(new RPMRelocation(io, this));
			}

			byte[] codeArr = new byte[codeSize];
			if (startPos == -1){
				startPos = symbolsOffset - BitUtils.getPaddedInteger(codeSize, RPM_PADDING);
			}
			io.seek(startPos);
			io.read(codeArr);
			
			this.code = new DataIOStream(codeArr);
		} catch (IOException ex) {
			Logger.getLogger(RPM.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public RPM(byte[] bytes) {
		this(new DataIOStream(bytes), 0, bytes.length);
	}

	public RPM() {
		code = new DataIOStream();
	}

	public static boolean isRPM(FSFile f) {
		if (f.isFile() && f.length() > 0x10) {
			try {
				DataIOStream io = f.getDataIOStream();

				io.seek(f.length() - RPM_FOOTER_SIZE);
				boolean rsl = StringIO.checkMagic(io, RPM_MAGIC);
				if (!rsl) {
					io.seek(f.length() - RPM_FOOTER_SIZE_LEGACY);
					rsl = StringIO.checkMagic(io, RPM_MAGIC);;
				}

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
			int base = BitUtils.getPaddedInteger(code.getLength(), 4);
			code.seek(base);
			code.write(source.code.toByteArray());

			for (RPMSymbol sym : symbols) {
				if (sym.address.isNull()) {
					RPMSymbol newSym = source.getSymbol(sym.name);
					if (newSym != null) {
						sym.address = new RPMSymbolAddress(this, newSym.address);
					}
				}
			}

			Map<RPMSymbol, RPMSymbol> oldToNewSymbolMap = new HashMap<>();

			for (RPMSymbol symbol : source.symbols) {
				RPMSymbol newSymbol = new RPMSymbol(this, symbol);
				if (newSymbol.name != null) {
					RPMSymbol existingSymbol = getSymbol(newSymbol.name);
					if (existingSymbol != null) {
						oldToNewSymbolMap.put(symbol, existingSymbol);
						continue;
					}
				}
				if (newSymbol.address.getAddrType() == RPMSymbolAddress.RPMAddrType.LOCAL) {
					newSymbol.address.setAddr(base + newSymbol.address.getAddr());
				}
				symbols.add(newSymbol);
				oldToNewSymbolMap.put(symbol, newSymbol);
			}

			for (RPMRelocation rel : source.relocations) {
				RPMRelocation newRel = new RPMRelocation(this, rel, oldToNewSymbolMap);
				if (newRel.target.isInternal()) {
					newRel.target.address += base;
				}
				relocations.add(newRel);
			}
		} catch (IOException ex) {
			Logger.getLogger(RPM.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void writeMAPToFile(FSFile fsf) {
		PrintStream out = new PrintStream(fsf.getNativeOutputStream());

		for (RPMSymbol symb : symbols) {
			if (symb.name != null && !symb.name.isEmpty()) {
				int addr = symb.address.getAddrAbs();
				if (addr < baseAddress || addr > baseAddress + code.getLength()) {
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

	public void setCode(DataIOStream buf) {
		code = buf;
	}

	public byte[] getBytesForBaseOfs(int baseOfs) {
		updateBytesForBaseAddr(baseOfs);
		byte[] bytes = getBytes();
		updateBytesForSetBaseAddr();
		return bytes;
	}

	public int getByteSize() {
		int size = code.getLength();
		size = BitUtils.getPaddedInteger(size, RPM_PADDING);

		size += 8; //symbol header
		for (RPMSymbol s : symbols) {
			size += s.getByteSize();
		}

		List<String> strings = new ArrayList<>();

		size = BitUtils.getPaddedInteger(size, RPM_PADDING);
		size += 12; //relocation header
		for (RPMRelocation rel : relocations) {
			rel.target.addStrings(strings);
			size += rel.getSize();
		}
		strings.add(productId);

		size = BitUtils.getPaddedInteger(size, RPM_PADDING);
		size += 4;
		for (String s : strings) {
			size += s.length() + 1;
		}

		size = BitUtils.getPaddedInteger(size, RPM_PADDING);
		size += RPM_FOOTER_SIZE;
		return size;
	}

	public byte[] getBytes() {
		try {
			DataIOStream ba = new DataIOStream();
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
			strings.putString(productId);
			strings.writeTable();
			ba.pad(RPM_PADDING);

			//HEADER
			ba.writeStringUnterminated(RPM_MAGIC);
			ba.write(0xFF);
			ba.writeInt(RPMRevisions.REV_CURRENT);
			ba.writeInt(strings.getNonRegistStringAddr(productId));
			ba.writeInt(productVersion);

			ba.writeInt(symbOffset);
			ba.writeInt(relOffset);
			ba.writeInt(codeSize);
			ba.writeInt(0);

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

	public void updateBytesForSetBaseAddr() {
		relocateBufferToAddr(baseAddress);
	}

	public void updateBytesForBaseAddr(int baseAddr) {
		relocateBufferToAddr(baseAddr);
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
					code.seekUnbased(rel.target.getAddrHWordAligned());

					writeRelocationDataByType(this, rel, code);
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(RPM.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void writeRelocationDataByType(RPM rpm, RPMRelocation rel, DataIOStream out) throws IOException {
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
					//BUGFIX: DO NOT USE R0. It will overwrite function arguments.
					//ARM call standard uses registers R0,R1,R2,R3 for args
					//Using R4+ could potentially break caller functions that use them
					//(spoiler alert: it will)
					//The only option I think we are left with is to just do a branch with link
					//The size will be the same since BL does not require the extra 4 bytes of address

					/*int tgtAddr = ThumbAssembler.writePcRelativeLoadAbs(out, 4, out.getPosition() + 4);
					ThumbAssembler.writeBXInstruction(out, 4);
					System.out.println("load rsl " + Integer.toHexString(tgtAddr));
					out.seek(tgtAddr);
					out.writeInt(addr);*/
					//Potentially destructive if the address does not fit under 4MB
					ThumbAssembler.writePushPopInstruction(out, false, true);
					ThumbAssembler.writeBranchLinkInstruction(out, addr);
					ThumbAssembler.writePushPopInstruction(out, true, true);
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

						int pos = out.getPosition();

						out.write(bytes);

						System.out.println("FULL_COPIED to " + Integer.toHexString(pos));
						for (RPMRelocation copyRel : rpm.relocations) {
							if (copyRel.target.isInternal() && copyRel.targetType != RPMRelocation.RPMRelTargetType.FULL_COPY) {
								int copyRelAddr = copyRel.target.getAddrHWordAligned();
								if (copyRelAddr >= copyStartAdr && copyRelAddr < copyEndAdr) {
									out.seek(pos + (copyRelAddr - copyStartAdr));
									System.out.println("Applying mirrored relocation at " + Integer.toHexString(out.getPosition()));
									writeRelocationDataByType(rpm, copyRel, out);
								}
							}
						}
					} else {
						throw new UnsupportedOperationException("Can not FULL_COPY a symbol without a length! - " + ((RPMRelocationSource.RPMRelSrcInternalSymbol) rel.source).symb.name);
					}
				} else {
					throw new UnsupportedOperationException("Can not FULL_COPY an external symbol!");
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

	public RPMSymbol findGlobalSymbolByAddrAbs(int addr) {
		for (RPMSymbol s : symbols) {
			if (s.address.getAddrType() == RPMSymbolAddress.RPMAddrType.GLOBAL) {
				if (s.address.getAddrAbs() == addr) {
					return s;
				}
			}
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
