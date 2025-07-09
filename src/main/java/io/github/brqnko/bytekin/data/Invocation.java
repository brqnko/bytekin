package io.github.brqnko.bytekin.data;

import io.github.brqnko.bytekin.injection.Shift;
import lombok.Data;

@Data
public class Invocation {
    private final String targetMethodName;
    private final String targetMethodDesc;
    private final String invokeMethodOwner;
    private final String invokeMethodName;
    private final String invokeMethodDesc;
    private final Shift shift;

    private final String hookMethodOwner;
    private final String hookMethodName;
}
