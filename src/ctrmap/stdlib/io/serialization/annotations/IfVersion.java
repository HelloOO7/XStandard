package ctrmap.stdlib.io.serialization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IfVersion {
	
	public CmpOp op();
	public int rhs();
	
	public static enum CmpOp {
		EQUAL,
		NOTEQUAL,
		LEQUAL,
		GEQUAL,
		LESS,
		GREATER
	}
}