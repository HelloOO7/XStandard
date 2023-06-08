package xstandard.io.serialization.annotations;

import java.lang.annotation.*;

/**
 * Forces an array's content to be read directly without seeking to a pointer.
 * This annotation is separate in order to not force inlining of the elements themselves.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@Inherited
public @interface InlineArray {
}
