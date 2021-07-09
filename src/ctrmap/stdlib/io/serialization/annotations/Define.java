package ctrmap.stdlib.io.serialization.annotations;

import java.lang.annotation.*;

/**
 * 'Defines' a field's value in the de/serializer cache under a tag.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface Define {
    public String value();
}
