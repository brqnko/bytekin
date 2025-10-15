package io.github.brqnko.bytekin.logging.impl;

import io.github.brqnko.bytekin.logging.ILogger;

/**
 * A simple logger implementation that logs messages to the standard output
 */
public class StdLogger implements ILogger {

    @Override
    public void log(String message) {
        System.out.println(message);
    }

}
