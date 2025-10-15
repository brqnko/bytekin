package io.github.brqnko.bytekin.logging;

/**
 * A simple logger interface.
 * This is needed to allow the user to provide their own logger implementation.
 */
public interface ILogger {

    void log(String message);

}
