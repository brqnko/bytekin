package io.github.brqnko.bytekin.data;

import io.github.brqnko.bytekin.injection.VariableTarget;
import lombok.Data;

@Data
public class VariableModification {

    private final String methodName;
    private final String methodDesc;
    private final VariableTarget target;

    private final int ordinal;
    private final int index;
    private final boolean argsOnly;
    private final boolean captureSelf;
    private final String variableDesc;

    private final String hookMethodOwner;
    private final String hookMethodName;
}
