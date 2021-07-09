package ctrmap.stdlib.formats.rpm;

import ctrmap.stdlib.arm.ARMAssembler;
import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.text.FormattingUtils;
import ctrmap.stdlib.io.InvalidMagicException;
import ctrmap.stdlib.io.util.StringIO;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ctrmap.stdlib.arm.ThumbAssembler;
import ctrmap.stdlib.gui.file.ExtensionFilter;
import ctrmap.stdlib.io.base.iface.IOStream;
import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import ctrmap.stdlib.io.structs.StringTable;
import ctrmap.stdlib.io.structs.TemporaryOffset;
import ctrmap.stdlib.math.MathEx;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Relocatable Program Module
 *
 * Format handler.
 */
public class RPM {

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Relocatable Program Module", "*.rpm");

	public static final String RPM_MAGIC = "RPM";
	public static final String SYM_MAGIC = "SYM0";
	public static final String STR_MAGIC = "STR0";
	public static final String REL_MAGIC = "REL0";

	public static final String INFO_MAGIC = "INFO";
	public static final String META_MAGIC = "META";

	public static final int RPM_PADDING = 0x10;

	public static final int RPM_FOOTER_SIZE_LEGACY = 0x10;
	public static final int RPM_FOOTER_SIZE = 0x20;

	public static final int RPM_INFO_HEADER_SIZE = 0x20;

	public int baseAddress;
	public List<RPMSymbol> symbols = new ArrayList<>();
	public List<RPMRelocation> relocations = new ArrayList<>();
	public final RPMMetaData metaData = new RPMMetaData();

	private RPMExternalSymbolResolver extResolver;

	private DataIOStream code;

	public RPM(FSFile fsf) {
		this(fsf.getBytes());
	}

	/**
	 * Reads a program module from a stream.
	 *
	 * @param io The stream to read from.
	 * @param startPos Starting position of the module.
	 * @param endPos Ending position of the module.
	 */
	public RPM(IOStream io, int startPos, int endPos) {
		try {
			RPMReader reader = new RPMReader(io);
			reader.seek(endPos - RPM_FOOTER_SIZE);
			if (!StringIO.checkMagic(reader, RPM_MAGIC)) {
				reader.seek(endPos - RPM_FOOTER_SIZE_LEGACY);
				if (!StringIO.checkMagic(reader, RPM_MAGIC)) {
					throw new InvalidMagicException("Not an RPM file!");
				}
			}
			int version = reader.read();
			int infoSectionOffset = -1;
			if (version != 0xFF) {
				version = version - '0';
			} else {
				version = reader.readInt();
				if (version >= RPMRevisions.REV_PRODUCT_INFO) {
					if (version < RPMRevisions.REV_INFO_SECTION) {
						//product ID and product version fields. DISCONTINUED.
						reader.skipBytes(8);
					} else {
						infoSectionOffset = reader.readInt();
						reader.skipBytes(4);
					}
				} else {
					reader.skipBytes(8);
				}
			}
			int symbolsOffset;
			int relocationsOffset;
			int stringsOffset = -1;
			int codeSize;

			if (infoSectionOffset == -1) {
				symbolsOffset = reader.readInt();
				relocationsOffset = reader.readInt();
				if (version >= RPMRevisions.REV_SMALL_SYMBOLS) {
					stringsOffset = reader.readInt();
				}
				codeSize = reader.readInt();
			} else {
				reader.seek(infoSectionOffset);
				if (!StringIO.checkMagic(reader, INFO_MAGIC)) {
					throw new InvalidMagicException("INFO section not present!");
				}
				symbolsOffset = reader.readInt();
				relocationsOffset = reader.readInt();
				stringsOffset = reader.readInt();
				codeSize = reader.readInt();
				int metaDataOffset = reader.readInt();
				if (metaDataOffset != 0) {
					reader.seek(metaDataOffset);
					if (!StringIO.checkMagic(reader, META_MAGIC)) {
						throw new InvalidMagicException("INFO section not present!");
					}
					metaData.readMetaData(reader);
				}
			}

			if (stringsOffset != -1) {
				reader.seek(stringsOffset);
				if (!StringIO.checkMagic(reader, STR_MAGIC)) {
					throw new InvalidMagicException("STR section not present!");
				}
				reader.setStrTableOffsHere();
			}

			reader.seek(symbolsOffset);
			if (!StringIO.checkMagic(reader, SYM_MAGIC)) {
				throw new InvalidMagicException("SYM section not present!");
			}
			int symbolCount = reader.readUnsignedShort();
			for (int i = 0; i < symbolCount; i++) {
				symbols.add(new RPMSymbol(this, reader, version));
			}

			reader.seek(relocationsOffset);
			if (!StringIO.checkMagic(reader, REL_MAGIC)) {
				throw new InvalidMagicException("REL section not present!");
			}
			baseAddress = reader.readInt();
			int relocationCount = reader.readInt();
			for (int i = 0; i < relocationCount; i++) {
				relocations.add(new RPMRelocation(reader, this));
			}

			byte[] codeArr = new byte[codeSize];
			if (startPos == -1) {
				startPos = symbolsOffset - MathEx.padInteger(codeSize, RPM_PADDING);
			}
			reader.seek(startPos);
			reader.read(codeArr);

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

	/**
	 * Checks if a FSFile contains an RPM.
	 *
	 * @param f The FSFile.
	 * @return True if the file can be read as an RPM.
	 */
	public static boolean isRPM(FSFile f) {
		if (f.isFile() && f.length() > 0x10) {
			try {
				DataIOStream io = f.getDataIOStream();

				io.seek(f.length() - RPM_FOOTER_SIZE);
				boolean rsl = StringIO.checkMagic(io, RPM_MAGIC);
				if (!rsl) {
					io.seek(f.length() - RPM_FOOTER_SIZE_LEGACY);
					rsl = StringIO.checkMagic(io, RPM_MAGIC);
				}

				io.close();
				return rsl;
			} catch (IOException ex) {
				Logger.getLogger(RPM.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return false;
	}

	/**
	 * Merges the code, symbols and relocations from another RPM.
	 *
	 * @param source The RPM to merge.
	 */
	public void merge(RPM source) {
		try {
			int base = MathEx.padInteger(code.getLength(), 4);
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

	/**
	 * Writes a linker address map of this RPM's symbols to a file.
	 *
	 * @param fsf The FSFile to write into.
	 */
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

	/**
	 * Relocates the code buffer to the given base offset and returns the
	 * compiled binary, then relocates it back.
	 *
	 * @param baseOfs Offset base.
	 * @return
	 */
	public byte[] getBytesForBaseOfs(int baseOfs) {
		byte[] origCode = code.toByteArray();
		updateBytesForBaseAddr(baseOfs);
		byte[] bytes = getBytes();
		try {
			code.seek(0);
			code.write(origCode);
		} catch (IOException ex) {
			Logger.getLogger(RPM.class.getName()).log(Level.SEVERE, null, ex);
		}
		return bytes;
	}

	/**
	 * Calculates the exact size of the RPM image.
	 *
	 * @return
	 */
	public int getByteSize() {
		int size = code.getLength();
		size = MathEx.padInteger(size, RPM_PADDING);

		List<String> strings = new ArrayList<>();

		size += 6; //symbol header
		for (RPMSymbol s : symbols) {
			s.addStrings(strings);
			size += s.getByteSize();
		}

		size = MathEx.padInteger(size, RPM_PADDING);
		size += 12; //relocation header
		for (RPMRelocation rel : relocations) {
			rel.target.addStrings(strings);
			rel.source.addStrings(strings);
			size += rel.getSize();
		}

		size = MathEx.padInteger(size, RPM_PADDING);
		size += RPM_INFO_HEADER_SIZE;
		size += 4; //META magic
		size += metaData.getByteSize();
		metaData.addStrings(strings);

		size = MathEx.padInteger(size, RPM_PADDING);
		size += 4; //STR0 magic
		for (String s : strings) {
			size += s.length() + 1;
		}

		size = MathEx.padInteger(size, RPM_PADDING);
		size += RPM_FOOTER_SIZE;
		return size;
	}

	/**
	 * Makes all symbol names in this RPM null.
	 */
	public void stripSymbolNames() {
		for (RPMSymbol s : symbols) {
			if (!s.isExportSymbol()) {
				s.name = null;
			}
		}
	}

	/**
	 * Writes the RPM image into a byte array.
	 *
	 * @return
	 */
	public byte[] getBytes() {
		try {
			DataIOStream ba = new DataIOStream();
			ba.write(code.toByteArray());
			int codeSize = ba.getPosition();
			ba.pad(RPM_PADDING);

			StringTable strings = new StringTable(ba, true, true);

			int symbOffset = ba.getPosition();
			ba.writeStringUnterminated(SYM_MAGIC);
			ba.writeShort(symbols.size());
			for (RPMSymbol sym : symbols) {
				sym.write(ba, strings);
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

			//INFO section
			int infoSectionOffset = ba.getPosition();
			ba.writeStringUnterminated(INFO_MAGIC);
			ba.writeInt(symbOffset);
			ba.writeInt(relOffset);
			TemporaryOffset stringsOffset = new TemporaryOffset(ba);
			ba.writeInt(codeSize);
			TemporaryOffset metaDataOffset = new TemporaryOffset(ba);
			ba.writeLong(0); //reserved values

			//Metadata
			metaDataOffset.setHere();
			ba.writeStringUnterminated(META_MAGIC);
			metaData.writeMetaData(ba, strings);
			ba.pad(RPM_PADDING);

			stringsOffset.setHere();
			ba.writeStringUnterminated(STR_MAGIC);

			strings.writeTable();
			ba.pad(RPM_PADDING);

			//HEADER
			ba.writeStringUnterminated(RPM_MAGIC);
			ba.write(0xFF);

			ba.writeInts(
					RPMRevisions.REV_CURRENT, //Format version
					infoSectionOffset,
					0
			);
			ba.writeLong(0);
			ba.writeLong(0);

			ba.close();
			return ba.toByteArray();
		} catch (IOException ex) {
			Logger.getLogger(RPM.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 * Sets the base address of this module, but does not update the code image.
	 *
	 * @param baseAddress The new base address.
	 */
	public void setBaseAddrNoUpdateBytes(int baseAddress) {
		this.baseAddress = baseAddress;
	}

	/**
	 * Updates the code image for the current address.
	 */
	public void updateBytesForSetBaseAddr() {
		relocateBufferToAddr(baseAddress);
	}

	/**
	 * Updates the code image for a new base address.
	 *
	 * @param baseAddr The new base address.
	 */
	public void updateBytesForBaseAddr(int baseAddr) {
		relocateBufferToAddr(baseAddr);
	}

	/**
	 * Performs this RPM's external relocations using an external relocator.
	 *
	 * @param relocator The external relocator to use.
	 */
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

	/**
	 * Writes a relocation into a binary image.
	 *
	 * @param rpm The RPM of the relocation.
	 * @param rel The relocation to write.
	 * @param out The binary image to write into.
	 * @throws IOException
	 */
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

	/**
	 * Gets a symbol from this RPM by its name.
	 *
	 * @param symbName Name of the symbol.
	 * @return
	 */
	public RPMSymbol getSymbol(String symbName) {
		for (RPMSymbol s : symbols) {
			if (Objects.equals(s.name, symbName)) {
				return s;
			}
		}
		return null;
	}

	/**
	 * Gets a symbol from this RPM by its number.
	 *
	 * @param symbNo Index of the symbol.
	 * @return
	 */
	public RPMSymbol getSymbol(int symbNo) {
		if (symbNo < symbols.size() && symbNo >= 0) {
			return symbols.get(symbNo);
		}
		return null;
	}

	/**
	 * Finds a global (non-relocatable) symbol in the RPM by its address.
	 *
	 * @param addr Address of the global symbol.
	 * @return
	 */
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

	/**
	 * Gets the number of a symbol in this RPM.
	 *
	 * @param symb The symbol.
	 * @return Index of 'symb'.
	 */
	public int getSymbolNo(RPMSymbol symb) {
		return symbols.indexOf(symb);
	}

	/**
	 * Gets an external symbol using an attached external symbol resolver.
	 *
	 * @param namespace The named segment of the symbol.
	 * @param name Name of the symbol.
	 * @return
	 */
	public RPMSymbol getExternalSymbol(String namespace, String name) {
		if (extResolver == null) {
			return null;
		}
		return extResolver.resolveExSymbol(namespace, name);
	}
}
