package io.github.brqnko.bytekin.injection;

/**
 * Used in {@link Inject} to specify the injection point
 */
public enum At {

    /** Injects the code at the beginning of the method */
    HEAD,

    /** Injects the code before the return statement */
    RETURN;

}
