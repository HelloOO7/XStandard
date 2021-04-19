
package ctrmap.stdlib.formats.rpm;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class RPMSymbolAddress {
	
	private int bits;
	private RPM rpm;
	
	public RPMSymbolAddress(RPM rpm, DataInput in) throws IOException{
		this.rpm = rpm;
		bits = in.readInt();
	}
	
	public RPMSymbolAddress(RPM rpm, RPMAddrType t, int addr){
		this.rpm = rpm;
		setAddr(addr);
		setAddrType(t);
	}
	
	public void write(DataOutput out) throws IOException {
		out.writeInt(bits);
	}
	
	public int getAddr(){
		return bits & 0x7FFFFFFF;
	}
	
	public RPMAddrType getAddrType(){
		return RPMAddrType.values()[(bits >> 31) & 1];
	}
	
	public int getAddrAbs(){
		if (getAddrType() == RPMAddrType.GLOBAL){
			return getAddr();
		}
		else {
			return rpm.baseAddress + getAddr();
		}
	}
	
	public void setAddr(int addr){
		bits &= ~0x7FFFFFFF;
		bits |= addr & 0x7FFFFFFF;
	}
	
	public void setAddrType(RPMAddrType t){
		bits &= 0x7FFFFFFF;
		bits |= (t.ordinal() << 31);
	}
	
	public static enum RPMAddrType {
		GLOBAL,
		LOCAL
	}
}
