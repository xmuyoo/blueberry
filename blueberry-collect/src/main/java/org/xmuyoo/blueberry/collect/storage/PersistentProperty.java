package org.xmuyoo.blueberry.collect.storage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface PersistentProperty {

    String name();

    ValueType valueType();

    boolean isUnique() default false;
}
