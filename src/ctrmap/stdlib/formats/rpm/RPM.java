package ctrmap.stdlib.formats.rpm;

import ctrmap.stdlib.arm.ARMAssembler;
import ctrmap.stdlib.fs.FSFile;
import ctrmap.stdlib.gui.FormattingUtils;
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
import ctrmap.stdlib.io.base.LittleEndianIO;
import java.io.File;

public class RPM {

	public static final String RPM_MAGIC = "RPM0";
	public static final String SYM_MAGIC = "SYM0";
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
			int symbolsOffset = ft.readInt();
			int relocationsOffset = ft.readInt();
			int codeSize = ft.readInt();

			ft.seek(symbolsOffset);
			if (!StringUtils.checkMagic(ft, SYM_MAGIC)) {
				throw new InvalidMagicException("SYM section not present!");
			}
			int symbolCount = ft.readUnsignedShort();
			for (int i = 0; i < symbolCount; i++) {
				symbols.add(new RPMSymbol(this, ft));
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

	public void writeMAPToFile(FSFile fsf) {
		PrintStream out = new PrintStream(fsf.getOutputStream());

		for (RPMSymbol symb : symbols) {
			if (symb.name != null && !symb.name.isEmpty()) {
				int addr = symb.address.getAddrAbs();
				if (addr < baseAddress || addr > baseAddress + code.length()) {
					continue; //external symbol
				}
				if (symb.type == RPMSymbolType.FUNCTION) {
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

	public void setCode(RandomAccessByteArray buf) {
		code = buf;
	}

	public byte[] getBytes() {
		try {
			RandomAccessByteArray ba = new RandomAccessByteArray();
			ba.write(code.toByteArray());
			int codeSize = ba.getPosition();
			ba.pad(RPM_PADDING);

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
				rel.write(ba);
			}
			ba.pad(RPM_PADDING);

			ba.writeStringUnterminated(RPM_MAGIC);
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

	private void relocateBufferToAddr(int baseAddress) {
		this.baseAddress = baseAddress;
		code.setBase(baseAddress);
		try {
			for (RPMRelocation rel : relocations) {
				code.seekUnbased(rel.target);
				int source = rel.source.getAbsoluteAddress();

				switch (rel.targetType) {
					case ARM_BRANCH_LINK:
						ARMAssembler.writeBranchInstruction(code, source, true);
						break;
					case THUMB_BRANCH_LINK:
						ThumbAssembler.writeBranchLinkInstruction(code, source);
						break;
					case OFFSET:
						code.writeInt(source);
						break;
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(RPM.class.getName()).log(Level.SEVERE, null, ex);
		}
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
