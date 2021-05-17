package ctrmap.stdlib.arm;

import ctrmap.stdlib.fs.accessors.DiskFile;
import ctrmap.stdlib.io.MemoryStream;
import ctrmap.stdlib.io.base.LittleEndianIO;
import ctrmap.stdlib.io.iface.PositionedDataInput;
import ctrmap.stdlib.io.iface.SeekableDataOutput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThumbAssembler {

	public static final int NOP_BYTES = 0xBF00;
	
	public static final int BL_MASK = (0b11111 << 11);
	public static final int BL_HIGH_IDENT = (0b11110 << 11);
	public static final int BL_LOW_IDENT = (0b11111 << 11);

	public static void writeBranchLinkInstruction(SeekableDataOutput out, int branchTarget) throws IOException {
		int currentOffset = out.getPosition() + 4;
		int diff = branchTarget - currentOffset;
		int value = diff >> 1;
		int lowbits = value & 0x7FF;
		int highbits = (value >> 11) & 0x7FF;

		int first = BL_HIGH_IDENT | highbits;
		int second = BL_LOW_IDENT | lowbits;

		out.writeShort(first);
		out.writeShort(second);
	}
	
	public static void writeBXInstruction(SeekableDataOutput out, int register) throws IOException {
		int byte0 = 0b01000111;
		int byte1 = ((register > 7 ? 1 : 0) << 6) | ((register % 7) << 3);
		out.write(byte1);
		out.write(byte0);
	}
	
	public static void writeSmallBranchInstruction(SeekableDataOutput out, int branchTarget) throws IOException {
		int currentOffset = out.getPosition() + 4;
		int diff = branchTarget - currentOffset;
		int value = diff >> 1;

		int instruction = 0b11100 | (value & 0x7FF);

		out.writeShort(instruction);
	}

	public static int getBranchInstructionTarget(PositionedDataInput in) throws IOException {
		int first = in.readUnsignedShort();
		int second = in.readUnsignedShort();
		int pos = in.getPosition();
		int high = ((first & BL_MASK) == BL_HIGH_IDENT) ? first : second;
		int low = (high == first) ? second : first;
		int value = ((high & 0x7FF) << 21 >> 10) | (low & 0x7FF);
		int diff = value << 1;
		return pos + diff;
	}
	
	public static void writeNopInstructions(DataOutput out, int count) throws IOException {
		for (int i = 0; i < count; i++) {
			out.writeShort(NOP_BYTES);
		}
	}

	public static void writeAddSubInstruction(DataOutput out, int destReg, int srcReg, int imm, boolean isImmReg, boolean isSub) throws IOException {
		int byte0 = (0b00011 << 3) | ((isImmReg ? 0 : 1) << 2) | ((isSub ? 1 : 0) << 1) | (imm >> 3 & 0b1);
		int byte1 = (imm << 6) | (srcReg << 3) | destReg;
		out.write(byte1);
		out.write(byte0);
	}

	public static void writeImmDPInstruction(DataOutput out, ThumbDPOpCode opCode, int srcDestReg, int imm) throws IOException {
		int byte0 = (0b001 << 5) | (opCode.ordinal() << 3) | (srcDestReg & 7);
		out.write(imm);
		out.write(byte0);
	}

	public static void writeImmOffsetSDTInstruction(DataOutput out, boolean isStore, boolean byteQty, int srcDestReg, int baseReg, int off) throws IOException {
		int byte0 = (0b011 << 5) | ((byteQty ? 1 : 0) << 4) | ((isStore ? 0 : 1) << 3) | (off >> 3);
		int byte1 = (off << 6) | (baseReg << 3) | (srcDestReg);
		out.write(byte1);
		out.write(byte0);
	}

	public static void writeSpRelativeSDTInstruction(DataOutput out, boolean isStore, int destReg, int off) throws IOException {
		int byte0 = (0b1001 << 4) | ((isStore ? 0 : 1) << 3) | destReg;
		out.write(off >> 2);
		out.write(byte0);
	}

	public static int writePcRelativeLoadAbs(SeekableDataOutput out, int destReg, int off) throws IOException {
		return writePcRelativeLoad(out, destReg, off, true);
	}
	
	public static int writePcRelativeLoad(SeekableDataOutput out, int destReg, int off) throws IOException {
		return writePcRelativeLoad(out, destReg, off, false);
	}
	
	public static int writePcRelativeLoad(SeekableDataOutput out, int destReg, int off, boolean offAbs) throws IOException {
		int pc = out.getPosition() + 4;
		if (offAbs){
			off -= pc;
		}
		else {
			off -= 4;
		}
		int alignedPC = pc & ~3;
		if (alignedPC != pc) {
			off += 2;
		}
		if ((off & 3) > 0) {
			off += 2;
		}
		int byte0 = (0b01001 << 3) | destReg;
		out.write(off >> 2);
		out.write(byte0);
		return alignedPC + off;
	}

	public static void writeLSInstruction(DataOutput out, ThumbLSOpCode opCode, int shift, int srcReg, int destReg) throws IOException {
		int byte0 = (opCode.ordinal() << 3) | ((shift >> 2) & 3);
		int byte1 = (shift << 6) | (srcReg << 3) | destReg;
		out.write(byte1);
		out.write(byte0);
	}

	public static void writeALUInstruction(DataOutput out, ThumbALUOpCode op, int srcReg, int destReg) throws IOException {
		int byte0 = (0b010000 << 2) | (op.ordinal() >> 2);
		int byte1 = (op.ordinal() << 6) | (srcReg << 3) | destReg;
		out.write(byte1);
		out.write(byte0);
	}

	public static void writeCondBranchInstruction(DataOutput out, ThumbCondOp cond, int skip) throws IOException {
		skip -= 4;
		skip >>= 1;
		out.write(skip);
		out.write((0b1101 << 4) | cond.ordinal());
	}

	public enum ThumbDPOpCode {
		MOV,
		CMP,
		ADD,
		SUB
	}

	public enum ThumbLSOpCode {
		LSL,
		LSR,
		ASR
	}

	public enum ThumbALUOpCode {
		AND,
		EOR,
		LSL,
		LSR,
		ASR,
		ADC,
		SBC,
		ROR,
		TST,
		NEG,
		CMP,
		CMN,
		ORR,
		MUL,
		BIC,
		MVN
	}

	public enum ThumbCondOp {
		EQ,
		NE,
		CS,
		CC,
		MI,
		PL,
		VS,
		VC,
		HI,
		LS,
		GE,
		LT,
		GT,
		LE
	}
}
