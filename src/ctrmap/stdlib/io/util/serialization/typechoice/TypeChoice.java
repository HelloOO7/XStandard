
package ctrmap.stdlib.io.util.serialization.typechoice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.lang.model.type.NullType;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(TypeChoices.class)
@Target(ElementType.FIELD)
public @interface TypeChoice {
	public int key() default -1;
	public Class value() default NullType.class;
}
