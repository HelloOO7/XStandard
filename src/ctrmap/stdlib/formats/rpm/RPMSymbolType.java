package ctrmap.stdlib.formats.rpm;

public enum RPMSymbolType {
	NULL,
	VALUE,
	FUNCTION_ARM,
	FUNCTION_THM,
	SECTION;
	
	public boolean isFunction(){
		return this == FUNCTION_ARM || this == FUNCTION_THM;
	}
}
