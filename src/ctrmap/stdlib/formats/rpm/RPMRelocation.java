
package ctrmap.stdlib.formats.rpm;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class RPMRelocation {
	
	public RPMRelTargetType targetType;
	public RPMRelSourceType sourceType;
	public int target;
	public RPMRelocationSource source;
	
	public RPMRelocation(){
		
	}
	
	public RPMRelocation(DataInput in, RPM rpm) throws IOException {
		int cfg = in.readUnsignedByte();
		sourceType = RPMRelSourceType.values()[cfg & 0b11]; //reserved 4 values
		targetType = RPMRelTargetType.values()[(cfg >> 2) & 0b111]; //reserved 8 values
		target = in.readInt();
		
		switch (sourceType){
			case SYMBOL_EXTERNAL:
				source = new RPMRelocationSource.RPMRelSrcExternalSymbol(rpm, in);
				break;
			case SYMBOL_INTERNAL:
				source = new RPMRelocationSource.RPMRelSrcInternalSymbol(rpm, in);
				break;
		}
	}
	
	public void write(DataOutput out) throws IOException{
		out.write((targetType.ordinal() << 2) | sourceType.ordinal());
		out.writeInt(target);
		source.write(out);
	}

	public static enum RPMRelTargetType {
		OFFSET,
		THUMB_BRANCH_LINK,
		ARM_BRANCH_LINK
	}
	
	public static enum RPMRelSourceType {
		SYMBOL_INTERNAL,
		SYMBOL_EXTERNAL
	}
}
