package xstandard.io.serialization.annotations.typechoice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.lang.model.type.NullType;

/**
 * A TypeChoice attribute decided using a String magic.
 */
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(TypeChoicesStr.class)
@Target({ElementType.FIELD, ElementType.TYPE})
@Inherited
public @interface TypeChoiceStr {
    public String key();
    public Class value() default NullType.class;
}

