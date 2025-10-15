package io.github.brqnko.bytekin.injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModifyVariable {

    String targetMethodName();

    String targetMethodDesc();

    VariableTarget target() default VariableTarget.HEAD;

    int ordinal() default -1;

    int index() default -1;

    boolean argsOnly() default false;

    boolean captureSelf() default false;

    String variableDesc() default "";
}
