package ctrmap.stdlib.io.serialization.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface Define {
    public String value();
}
