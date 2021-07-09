package ctrmap.stdlib.io.serialization.annotations;

import java.lang.annotation.*;

/**
 * Makes the de/serializer read/store an array field's element count from a
 * 'defined' field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface DefinedArraySize {

	public String value();
}
