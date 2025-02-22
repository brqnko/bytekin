package io.github.brqnko.bytekin.injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to specify the class name of the class that should be modified
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModifyClass {

    /**
     * The name of the class that should be modified.
     * This might be changed by the mapping.
     * */
    String className();

}
