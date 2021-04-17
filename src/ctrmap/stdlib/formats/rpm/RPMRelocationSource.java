package ctrmap.stdlib.formats.rpm;

import ctrmap.stdlib.io.util.StringUtils;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public interface RPMRelocationSource {

	public int getAbsoluteAddress();

	public void write(DataOutput out) throws IOException;

	public static class RPMRelSrcInternalSymbol implements RPMRelocationSource {

		private RPMSymbol symb;
		private RPM rpm;
		
		public RPMRelSrcInternalSymbol(RPM rpm, RPMSymbol symb){
			this.symb = symb;
			this.rpm = rpm;
		}

		public RPMRelSrcInternalSymbol(RPM rpm, DataInput in) throws IOException {
			symb = rpm.getSymbol(in.readUnsignedShort());
			this.rpm = rpm;
		}

		@Override
		public int getAbsoluteAddress() {
			return symb.address.getAddrAbs();
		}

		@Override
		public void write(DataOutput out) throws IOException {
			out.writeShort(rpm.getSymbolNo(symb));
		}
	}

	public static class RPMRelSrcExternalSymbol implements RPMRelocationSource {

		private RPMSymbol symb;
		private String ns;

		public RPMRelSrcExternalSymbol(RPM rpm, DataInput in) throws IOException {
			ns = StringUtils.readString(in);
			symb = rpm.getExternalSymbol(ns, StringUtils.readString(in));
		}

		@Override
		public int getAbsoluteAddress() {
			return symb.address.getAddrAbs();
		}

		@Override
		public void write(DataOutput out) throws IOException {
			StringUtils.writeString(out, ns);
			StringUtils.writeString(out, symb.name);
		}

	}
}
