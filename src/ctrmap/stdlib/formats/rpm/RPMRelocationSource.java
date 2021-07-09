package ctrmap.stdlib.formats.rpm;

import ctrmap.stdlib.io.structs.StringTable;
import ctrmap.stdlib.util.ArraysEx;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

/**
 *
 */
public interface RPMRelocationSource {

	public int getAddress();
	
	public int getAbsoluteAddress();
	
	public int getWritableAddress();
	
	public int getLength();
	
	public int getDataSize();
	
	public void addStrings(List<String> l);

	public void write(DataOutput out, StringTable strtab) throws IOException;

	public static class RPMRelSrcInternalSymbol implements RPMRelocationSource {

		public RPMSymbol symb;
		protected RPM rpm;
		
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
		public void write(DataOutput out, StringTable strtab) throws IOException {
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

		@Override
		public int getLength() {
			return symb.size;
		}

		@Override
		public int getDataSize() {
			return 2;
		}

		@Override
		public void addStrings(List<String> l) {
		}
	}

	public static class RPMRelSrcExternalSymbol extends RPMRelSrcInternalSymbol {

		public String ns;
		public String symbolName;

		RPMRelSrcExternalSymbol(RPM rpm, RPMReader in) throws IOException {
			ns = in.readStringWithAddress();
			symbolName = in.readStringWithAddress();
			this.rpm = rpm;
			symb = rpm.getExternalSymbol(ns, symbolName);
		}
		
		public RPMRelSrcExternalSymbol(RPM rpm, String ns, String symbolName){
			this.ns = ns;
			this.symbolName = symbolName;
			this.rpm = rpm;
			symb = rpm.getExternalSymbol(ns, symbolName);
		}

		@Override
		public void write(DataOutput out, StringTable strtab) throws IOException {
			strtab.putStringOffset(ns);
			strtab.putStringOffset(symb.name);
		}
		
		@Override
		public void addStrings(List<String> l) {
			ArraysEx.addIfNotNullOrContains(l, ns);
			ArraysEx.addIfNotNullOrContains(l, symb.name);
		}
		
		@Override
		public int getDataSize() {
			return 4;
		}
	}
}
