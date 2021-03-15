package ctrmap.stdlib.io.util.serialization;

public class SerializerSettings {
	
	public PointerType pointerType = PointerType.INLINE;
	
	public static enum PointerType{
		INLINE,
		ABSOLUTE,
		SELF_RELATIVE
	}
}
