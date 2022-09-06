package xstandard.util;

import java.util.HashSet;

public class EnumBitflags<E extends Enum> {
	
	private HashSet<E> setFlags = new HashSet<>();
	
	public EnumBitflags() {
		
	}
	
	public EnumBitflags(E... setFlags) {
		for (E f : setFlags) {
			this.setFlags.add(f);
		}
	}
	
	public void setValue(E flag, boolean isSet) {
		if (isSet) {
			set(flag);
		}
		else {
			unset(flag);
		}
	}
	
	public void set(E flag) {
		setFlags.add(flag);
	}
	
	public void unset(E flag) {
		setFlags.remove(flag);
	}
	
	public boolean isSet(E flag) {
		return setFlags.contains(flag);
	}
}
