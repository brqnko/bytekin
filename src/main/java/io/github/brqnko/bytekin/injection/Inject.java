package io.github.brqnko.bytekin.injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * With this annotation, you can insert your method call into the target method
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {

    /** The name of the target method that might be changed by the mapping */
    String methodName();

    /** The description of the target method that might be changed by the mapping */
    String methodDesc();

    At at();

}
