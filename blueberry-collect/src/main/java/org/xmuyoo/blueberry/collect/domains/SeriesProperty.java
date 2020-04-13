package org.xmuyoo.blueberry.collect.domains;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface SeriesProperty {

    /**
     * If it's a series value field, then set value as true.
     */
    boolean value() default true;

    /**
     * Description of the series value, which will be set in tags if it is not blank.
     */
    String description() default "";
}
