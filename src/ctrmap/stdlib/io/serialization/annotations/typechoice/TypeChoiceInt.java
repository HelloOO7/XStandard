package ctrmap.stdlib.io.serialization.annotations.typechoice;

import javax.lang.model.type.NullType;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(TypeChoicesInt.class)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface TypeChoiceInt {
    public int key();
    public Class value() default NullType.class;
}

