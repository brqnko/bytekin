package io.github.brqnko.bytekin.injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Invoke {

    String targetMethodName();

    String targetMethodDesc();

    String invokeMethodOwner();

    String invokeMethodName();

    String invokeMethodDesc();

    Shift shift() default Shift.BEFORE;

}
