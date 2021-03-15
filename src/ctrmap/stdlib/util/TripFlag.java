package ctrmap.stdlib.util;

public class TripFlag {
	private final boolean defaultValue;
	private boolean v;
	
	public TripFlag(boolean defaultValue, boolean currentValue){
		this.defaultValue = defaultValue;
		v = currentValue;
	}
	
	public TripFlag(){
		this(false, false);
	}
	
	public boolean get(){
		boolean v2 = v;
		v = defaultValue;
		return v2;
	}
	
	public boolean peek(){
		return v;
	}
	
	public void raise(){
		v = !defaultValue;
	}
}
