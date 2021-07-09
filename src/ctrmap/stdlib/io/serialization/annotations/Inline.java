package ctrmap.stdlib.io.serialization.annotations;

import java.lang.annotation.*;

/**
 * Forces the field/class to always be read as inline without pointer
 * references.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@Inherited
public @interface Inline {
}
