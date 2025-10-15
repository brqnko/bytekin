package io.github.brqnko.bytekin.data;

import lombok.Data;

@Data
public class ConstantModification {

    private final String methodName;
    private final String methodDesc;
    private final Object constantValue;
    private final int ordinal;

    private final String hookMethodOwner;
    private final String hookMethodName;
}
