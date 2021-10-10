package ctrmap.stdlib.formats.rpm;

import ctrmap.stdlib.io.base.impl.ext.data.DataIOStream;
import ctrmap.stdlib.io.structs.StringTable;
import java.io.IOException;
import java.util.Map;

/**
 * Relocation info for binary addresses in a RPM's code image.
 */
public class RPMRelocation {

	public RPMRelTargetType targetType;
	public RPMRelSourceType sourceType;

	public RPMRelocationTarget target;

	public RPMRelocationSource source;

	public RPMRelocation() {

	}

	public RPMRelocation(RPM rpm, RPMRelocation rel, Map<RPMSymbol, RPMSymbol> symbolTransferMap) {
		sourceType = rel.sourceType;
		targetType = rel.targetType;

		target = new RPMRelocationTarget(rel.target);

		switch (sourceType) {
			case SYMBOL_EXTERNAL:
				RPMRelocationSource.RPMRelSrcExternalSymbol es = (RPMRelocationSource.RPMRelSrcExternalSymbol) rel.source;
				source = new RPMRelocationSource.RPMRelSrcExternalSymbol(rpm, es.ns, es.symbolName);
				break;
			case SYMBOL_INTERNAL:
				RPMRelocationSource.RPMRelSrcInternalSymbol is = (RPMRelocationSource.RPMRelSrcInternalSymbol) rel.source;
				System.out.println("Transferring internal relocation from symbol " + is.symb + " to " + symbolTransferMap.get(is.symb));
				source = new RPMRelocationSource.RPMRelSrcInternalSymbol(rpm, symbolTransferMap.get(is.symb));
				break;
		}
	}

	RPMRelocation(RPMReader in, RPM rpm) throws IOException {
		int cfg = in.readUnsignedByte();
		sourceType = RPMRelSourceType.values()[cfg & 0b11]; //reserved 4 values
		targetType = RPMRelTargetType.values()[(cfg >> 2) & 0b111]; //reserved 8 values

		target = new RPMRelocationTarget(in);

		switch (sourceType) {
			case SYMBOL_EXTERNAL:
				source = new RPMRelocationSource.RPMRelSrcExternalSymbol(rpm, in);
				break;
			case SYMBOL_INTERNAL:
				source = new RPMRelocationSource.RPMRelSrcInternalSymbol(rpm, in);
				break;
		}
	}

	void write(DataIOStream out, StringTable strtbl) throws IOException {
		out.write((targetType.ordinal() << 2) | sourceType.ordinal());
		target.write(out, strtbl);
		source.write(out, strtbl);
	}

	/**
	 * Gets the serialized size of the relocation info.
	 *
	 * @return
	 */
	public int getSize() {
		return 1 + target.getSize() + source.getDataSize();
	}

	/**
	 * Type of the relocated field at the target address,
	 */
	public static enum RPMRelTargetType {
		/**
		 * A 32-bit absolute offset.
		 */
		OFFSET,
		/**
		 * A thumb BL instruction.
		 */
		THUMB_BRANCH_LINK,
		/**
		 * An ARM BL instruction.
		 */
		ARM_BRANCH_LINK,
		/**
		 * A Thumb branch. For technical reasons, it is relocated as a BL with
		 * return.
		 */
		THUMB_BRANCH,
		/**
		 * An ARM B instruction.
		 */
		ARM_BRANCH,
		/**
		 * Full copy of the source data.
		 */
		FULL_COPY;

		public static RPMRelTargetType fromName(String name) {
			for (RPMRelTargetType t : values()) {
				if (t.name().equals(name)) {
					return t;
				}
			}
			return null;
		}
	}

	/**
	 * Type of the provider of the address that the relocated field points to.
	 */
	public static enum RPMRelSourceType {
		/**
		 * A symbol inside the RPM.
		 */
		SYMBOL_INTERNAL,
		/**
		 * A symbol outside of the RPM, handled by an RPMExternalSymbolResolver.
		 */
		SYMBOL_EXTERNAL
	}
}
