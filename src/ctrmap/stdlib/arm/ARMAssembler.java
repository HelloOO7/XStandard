package ctrmap.stdlib.arm;

import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import ctrmap.stdlib.io.util.IOUtils;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ARMAssembler {
	
	public static final int CONDITION_ALWAYS = 0b1110;
	public static final int CONDITION_EQ = 0b0000;
	public static final int CONDITION_NE = 0b0001;
	
	public static final int ARM_STACKPTR_REG_NUM = 13;
	public static final int ARM_PRGCNT_REG_NUM = 15;
	public static final int INS_NOP_MOV_R0_R0 = 0xE1A00000;
		
	public static void writeNOPInstructions(DataIOStream out, int count) throws IOException{
		for (int i = 0; i < count; i++){
			out.writeInt(INS_NOP_MOV_R0_R0);
		}
	}
	
	public static void writeCMPInstruction(DataOutput out, int registerToCompare, int operand2Register) throws IOException{
		writeDataProcessingInstruction(out, ARMDataTransferOpCode.CMP, 0, registerToCompare, operand2Register, true, 0, ARMCondition.AL);
	}
	
	public static void writeCMPInstruction(DataOutput out, int registerToCompare, int valueToCompareTo, ARMCondition cond) throws IOException{
		writeDataProcessingInstruction(out, ARMDataTransferOpCode.CMP, 0, registerToCompare, valueToCompareTo, false, 0, cond);
	}
	
	public static void writeTSTInstruction(DataOutput out, int registerToCompare, int bitsToTest) throws IOException{
		writeDataProcessingInstruction(out, ARMDataTransferOpCode.TST, 0, registerToCompare, bitsToTest, false, 0, ARMCondition.AL);
	}
	
	public static void writeMOVInstruction(DataOutput out, int targetRegister, int sourceRegister) throws IOException{
		writeMOVInstruction(out, targetRegister, sourceRegister, ARMShiftType.LSL, 0);
	}
	
	public static void writeMOVInstruction(DataOutput out, int targetRegister, int sourceRegister, ARMShiftType op2ShiftType, int op2ShiftAmount) throws IOException{
		writeMOVInstruction(out, targetRegister, sourceRegister, op2ShiftType, op2ShiftAmount, ARMCondition.AL);
	}
	
		public static void writeMOVInstruction(DataOutput out, int targetRegister, int sourceRegister, ARMShiftType op2ShiftType, int op2ShiftAmount, ARMCondition condition) throws IOException{
		writeDataProcessingInstruction(out, ARMDataTransferOpCode.MOV, targetRegister, 0, sourceRegister, true, makeOperand2RegShift(op2ShiftType, op2ShiftAmount), condition);
	}
	
	public static void writeMOVInstruction(DataOutput out, int targetRegister, int sourceValue, ARMCondition condition) throws IOException{
		writeDataProcessingInstruction(out, ARMDataTransferOpCode.MOV, targetRegister, 0, sourceValue, false, 0, condition);
	}
	
	public static void writeADDInstruction(DataOutput out, int targetRegister, int op1reg, int op2reg, ARMShiftType shiftType, int shiftAmount) throws IOException {
		writeDataProcessingInstruction(out, ARMDataTransferOpCode.ADD, targetRegister, op1reg, op2reg, true, makeOperand2RegShift(shiftType, shiftAmount), ARMCondition.AL);
	}
	
	public static int makeOperand2RegShift(ARMShiftType shiftType, int shiftAmount){
		return (shiftType.ordinal() << 1) | (shiftAmount << 3);
	}
	
	private static void writeDataProcessingInstruction(DataOutput out, ARMDataTransferOpCode opCode, int targetRegister, int operand1Register, int operand2, boolean isOp2Register, int op2RegShift, ARMCondition condition) throws IOException{
		int opc = opCode.opCode;
		int byte0 = (condition.bits) << 4 | (!isOp2Register ? (1 << 1) : 0) | ((opc >> 3) & 1);
		int byte1 = (opc << 5) & 0b11100000;
		
		if (opCode.isTargetCondCode){
			byte1 |= 0b00010000;
		}
		byte1 |= operand1Register;
		
		int op2;
		if (isOp2Register){
			op2 = op2RegShift << 4 | operand2;
		}
		else {
			op2 = encodeBarrelShiftedInt(operand2);
		}
		
		int byte2 = targetRegister << 4 | (op2 >> 8 & 0b1111);
		int byte3 = op2 & 0xFF;
		out.write(byte3);
		out.write(byte2);
		out.write(byte1);
		out.write(byte0);
	}
	
	public static void writeLDRInstruction(DataIOStream out, int targetRegister, int targetOffsetAbs) throws IOException{
		int offsetDiff = targetOffsetAbs - (out.getPosition() + 8);
		writeLDRInstruction(out, targetRegister, ARM_PRGCNT_REG_NUM, offsetDiff);
	}
	
	public static void writeLDRInstruction(DataIOStream out, int targetRegister, int baseRegister, int targetOffset) throws IOException{
		writeSingleDataTransferInstruction(out, targetRegister, baseRegister, targetOffset, true, false, ARMCondition.AL);
	}
	
	public static void writeSTRInstruction(DataIOStream out, int sourceRegister, int baseRegister, int targetOffset, boolean isByteQuantity, ARMCondition cnd) throws IOException {
		writeSingleDataTransferInstruction(out, sourceRegister, baseRegister, targetOffset, false, isByteQuantity, cnd);
	}
	
	private static void writeSingleDataTransferInstruction(DataIOStream out, int targetRegister, int baseRegister, int targetOffset, boolean isLDR, boolean isByteQuantity, ARMCondition cnd) throws IOException{
		byte condImmPrePostByte = 0;
		condImmPrePostByte |= cnd.bits << 4;	//Always condition
		condImmPrePostByte |= 0b01 << 2;				//Constant
		condImmPrePostByte |= 0b0 << 1;					//Is immediate offset
		condImmPrePostByte |= 0b1;						//Is pre-indexed
		byte configByte = 0;
		configByte |= (targetOffset > 0 ? 1 : 0) << 7;	//Up if positive, down if negative offset
		configByte |= (isByteQuantity ? 1 : 0) << 6;	//Word quantity		
		configByte |= 0b0 << 5;							//No write-back
		configByte |= (isLDR ? 1 : 0) << 4;				//LDR
		configByte |= baseRegister & 0xF;				//base register
		int absOffset = Math.abs(targetOffset);
		byte opByte0 = 0;
		opByte0 |= targetRegister << 4;
		opByte0 |= (absOffset >> 8) & 0b1111;
		byte opByte1 = (byte)(absOffset & 0xFF);
		out.writeByte(opByte1);	//cause lil' endian
		out.writeByte(opByte0);
		out.writeByte(configByte);
		out.writeByte(condImmPrePostByte);
	}
	
	public static int getBranchInstructionTarget(DataIOStream in) throws IOException{
		int currentOffset = in.getPosition() + 8;
		int v = (in.readInt24() << 2) + currentOffset;
		in.skipBytes(1);
		return v;
	}
	
	public static void writeBranchInstruction(DataIOStream out, int branchTarget, boolean link) throws IOException{
		writeBranchInstruction(out, branchTarget, link, ARMCondition.AL);
	}
	
	public static void writeBranchInstruction(DataIOStream out, int branchTarget, boolean link, ARMCondition cond) throws IOException{
		int currentOffset = out.getPosition() + 8;
		int diff = branchTarget - currentOffset;
		int value = (diff >> 2);
		out.writeInt24(value);
		out.write(cond.bits << 4 | 0b101 << 1 | (link ? 1 : 0));
	}
	
	public static void setImmValue(DataIOStream io, int targetValue) throws IOException{
		int basePos = io.getPosition();
		int bsi = encodeBarrelShiftedInt(targetValue);
		io.skipBytes(1);
		int currentValue = io.readUnsignedByte();
		io.seek(basePos);
		io.write(bsi & 0xFF);
		io.write(currentValue & 0b11110000 | (bsi >>> 8 & 0xF));
		io.seek(basePos);
	}
	
	public static int getInstructionOperand2(DataInput in) throws IOException {
		return decodeBarrelShiftedInt(in.readInt() & 0xFFF);
	}
	
	public static int getDecEncBarrelShiftDiff(int value) {
		return ARMAssembler.decodeBarrelShiftedInt(ARMAssembler.encodeBarrelShiftedInt(value)) - value;
	}
	
	public static int decodeBarrelShiftedInt(int bs){
		return Integer.rotateRight(bs & 0xFF, ((bs >> 8) & 0xF) * 2);
	}
	
	public static int encodeBarrelShiftedInt(int v){
		int MSB = 31;//MSB search from top
		int LSB = 0;//LSB search from bottom
		int c = 0;
		for (; MSB >= 0; MSB--){
			if (((v >> MSB) & 1) > 0){
				break;//If bit searched from top is set, set MSB and break
			}
		}
		
		for (; LSB < 32; LSB++){
			if (((v >> LSB) & 1) > 0){
				break;//If bit searched from bottom is set, set LSB and break
			}
		}
		
		//Cull of LSBs to match MSB - LSB < 8
		int culledLSB = Math.max(MSB - 7, LSB);
		//If culled, up the LSB so that we get the ceiling
		if (culledLSB != LSB){
			c |= 1 << culledLSB;
		}
		LSB = culledLSB;//Culling done
		int availableLSBSpace = 7 - (MSB - LSB);//Number of unallocated bits in the 8bit space
		
		if ((LSB & 1) > 0){
			//Due to the way the ARM barrel shift works, RoRs have to be an even number.
			//LSB is not even, sigh. Have to omit yet another bit. If LSB space is available, slam it in there, otherwise, shift LSB to left by 1.
			if (availableLSBSpace > 0){
				LSB --;//If there are available unallocated bits, no problem, we don't have to cull anything. Just shift the LSB to right by one.
			}
			else {
				LSB++;//Ouch. We're going to have to shift LSB to the left.
				c |= 1 << LSB;//...meaning we have to set the new LSB to 1 so that the final value is greater than the original.
			}
		}
		
		int calc = (v | c) >> LSB;//Value OR ceiling bits SHR by LSB
		int armShift = (Integer.SIZE - LSB) / 2;
		return calc | (armShift << 8);
	}
	
	public static enum ARMShiftType {
		LSL,
		LSR,
		ASR,
		ROR
	}
	
	public static enum ARMDataTransferOpCode{
		MOV(0b1101, false),
		CMP(0b1010, true),
		TST(0b1000, true),
		ADD(0b0100, false);
		
		public final int opCode;
		public final boolean isTargetCondCode;
		
		private ARMDataTransferOpCode(int opc, boolean isTargetCondCode){
			opCode = opc;
			this.isTargetCondCode = isTargetCondCode;
		}
	}
	
	public static enum ARMCondition {
		EQ(0b0000),
		NE(0b0001),
		AL(0b1110),
		GE(0b1010),
		GT(0b1100);
		
		public final int bits;
		
		private ARMCondition(int bits){
			this.bits = bits;
		}
	}
}
