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
import ctrmap.stdlib.io.base.iface.IOStream;
import ctrmap.stdlib.io.base.impl.access.MemoryStream;
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

	public static final int RPM_FOOTER_SIZE = 0x10;

	public int baseAddress;
	public List<RPMSymbol> symbols = new ArrayList<>();
	public List<RPMRelocation> relocations = new ArrayList<>();

	private RPMExternalSymbolResolver extResolver;

	private DataIOStream code;

	public RPM(FSFile fsf) {
		this(fsf.getBytes());
	}

	public RPM(byte[] bytes) {
		try {
			DataIOStream ft = new DataIOStream(bytes);
			ft.seek(bytes.length - 16);
			if (!StringIO.checkMagic(ft, RPM_MAGIC)) {
				throw new InvalidMagicException("Not an RPM file!");
			}
			int version = ft.read() - '0';
			int symbolsOffset = ft.readInt();
			int relocationsOffset = ft.readInt();
			int codeSize = ft.readInt();

			ft.seek(symbolsOffset);
			if (!StringIO.checkMagic(ft, SYM_MAGIC)) {
				throw new InvalidMagicException("SYM section not present!");
			}
			int symbolCount = ft.readUnsignedShort();
			for (int i = 0; i < symbolCount; i++) {
				symbols.add(new RPMSymbol(this, ft, version));
			}

			ft.seek(relocationsOffset);
			if (!StringIO.checkMagic(ft, REL_MAGIC)) {
				throw new InvalidMagicException("REL section not present!");
			}
			baseAddress = ft.readInt();
			int relocationCount = ft.readInt();
			for (int i = 0; i < relocationCount; i++) {
				relocations.add(new RPMRelocation(ft, this));
			}

			code = new DataIOStream(Arrays.copyOf(bytes, codeSize));
		} catch (IOException ex) {
			Logger.getLogger(RPM.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public RPM() {
		code = new DataIOStream();
	}

	public static boolean isRPM(FSFile f) {
		if (f.isFile() && f.length() > 0x10) {
			try {
				DataIOStream io = f.getDataIOStream();

				io.seek(f.length() - 16);
				boolean rsl = StringIO.checkMagic(io, RPM_MAGIC);

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
