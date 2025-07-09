package io.github.brqnko.bytekin.data;

import io.github.brqnko.bytekin.injection.At;
import io.github.brqnko.bytekin.injection.Shift;
import lombok.Data;

@Data
public class Injection {

    private final String methodName;
    private final String methodDesc;
    private final At at;

    private final String hookMethodOwner;
    private final String hookMethodName;
}
