package io.github.brqnko.bytekin.injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Redirect {

    String targetMethodName();

    String targetMethodDesc();

    String owner();

    String name();

    String desc();

    RedirectType type() default RedirectType.METHOD;

    int ordinal() default -1;
}
