package xstandard.io.serialization.annotations.typechoice;

import javax.lang.model.type.NullType;
import java.lang.annotation.*;

/**
 * A TypeChoice attribute decided using an integer magic.
 */
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(TypeChoicesInt.class)
@Target({ElementType.FIELD, ElementType.TYPE})
@Inherited
public @interface TypeChoiceInt {
    public int key();
    public Class value() default NullType.class;
}

