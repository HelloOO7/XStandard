
package ctrmap.stdlib.formats.rpm;

import ctrmap.stdlib.io.iface.SeekableDataInput;
import ctrmap.stdlib.io.iface.SeekableDataOutput;
import ctrmap.stdlib.io.structs.StringTable;
import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class RPMRelocation {
	
	public RPMRelTargetType targetType;
	public RPMRelSourceType sourceType;
	
	public RPMRelocationTarget target;
	
	public RPMRelocationSource source;
	
	public RPMRelocation(){
		
	}
	
	public RPMRelocation(RPM rpm, RPMRelocation rel, Map<RPMSymbol, RPMSymbol> symbolTransferMap){
		sourceType = rel.sourceType;
		targetType = rel.targetType;
		
		target = new RPMRelocationTarget(rel.target);
		
		switch (sourceType){
			case SYMBOL_EXTERNAL:
				RPMRelocationSource.RPMRelSrcExternalSymbol es = (RPMRelocationSource.RPMRelSrcExternalSymbol)rel.source;
				source = new RPMRelocationSource.RPMRelSrcExternalSymbol(rpm, es.ns, es.symbolName);
				break;
			case SYMBOL_INTERNAL:
				RPMRelocationSource.RPMRelSrcInternalSymbol is = (RPMRelocationSource.RPMRelSrcInternalSymbol)rel.source;
				source = new RPMRelocationSource.RPMRelSrcInternalSymbol(rpm, symbolTransferMap.get(is.symb));
				break;
		}
	}
	
	public RPMRelocation(SeekableDataInput in, RPM rpm) throws IOException {
		int cfg = in.readUnsignedByte();
		sourceType = RPMRelSourceType.values()[cfg & 0b11]; //reserved 4 values
		targetType = RPMRelTargetType.values()[(cfg >> 2) & 0b111]; //reserved 8 values
		
		target = new RPMRelocationTarget(in);
		
		switch (sourceType){
			case SYMBOL_EXTERNAL:
				source = new RPMRelocationSource.RPMRelSrcExternalSymbol(rpm, in);
				break;
			case SYMBOL_INTERNAL:
				source = new RPMRelocationSource.RPMRelSrcInternalSymbol(rpm, in);
				break;
		}
	}
	
	public void write(SeekableDataOutput out, StringTable strtbl) throws IOException{
		out.write((targetType.ordinal() << 2) | sourceType.ordinal());
		target.write(out, strtbl);
		source.write(out);
	}
	
	public int getSize(){
		return 1 + target.getSize() + source.getDataSize();
	}

	public static enum RPMRelTargetType {
		OFFSET,
		THUMB_BRANCH_LINK,
		ARM_BRANCH_LINK,
		THUMB_BRANCH,
		ARM_BRANCH,
		
		FULL_COPY;
		
		public static RPMRelTargetType fromName(String name){
			for (RPMRelTargetType t : values()){
				if (t.name().equals(name)){
					return t;
				}
			}
			return null;
		}
	}
	
	public static enum RPMRelSourceType {
		SYMBOL_INTERNAL,
		SYMBOL_EXTERNAL
	}
}
