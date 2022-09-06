package xstandard.io.serialization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LengthPos {
    public LengthPosType value() default LengthPosType.BEFORE_PTR;
	
	public enum LengthPosType {
		AS_FIELD,
		BEFORE_PTR,
		AFTER_PTR,
	}
}
