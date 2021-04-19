package ctrmap.stdlib.formats.rpm;

import ctrmap.stdlib.io.util.StringUtils;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public interface RPMRelocationSource {

	public int getAddress();
	
	public int getAbsoluteAddress();
	
	public int getWritableAddress();

	public void write(DataOutput out) throws IOException;

	public static class RPMRelSrcInternalSymbol implements RPMRelocationSource {

		public RPMSymbol symb;
		private RPM rpm;
		
		protected RPMRelSrcInternalSymbol(){
			
		}
		
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

		@Override
		public int getAddress() {
			return symb.address.getAddr();
		}

		@Override
		public int getWritableAddress() {
			int a = getAbsoluteAddress();
			if (symb.type == RPMSymbolType.FUNCTION_THM){
				a++;
			}
			return a;
		}
	}

	public static class RPMRelSrcExternalSymbol extends RPMRelSrcInternalSymbol {

		private String ns;

		public RPMRelSrcExternalSymbol(RPM rpm, DataInput in) throws IOException {
			symb = rpm.getExternalSymbol(ns = StringUtils.readString(in), StringUtils.readString(in));
		}

		@Override
		public void write(DataOutput out) throws IOException {
			StringUtils.writeString(out, ns);
			StringUtils.writeString(out, symb.name);
		}
	}
}
