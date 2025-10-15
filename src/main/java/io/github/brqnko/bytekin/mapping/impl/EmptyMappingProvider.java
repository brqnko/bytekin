package io.github.brqnko.bytekin.mapping.impl;

import io.github.brqnko.bytekin.mapping.IMappingProvider;

public class EmptyMappingProvider implements IMappingProvider {

    @Override
    public String getClassName(String className) {
        return className;
    }

    @Override
    public String getMethodName(String className, String methodName, String methodDesc) {
        return methodName;
    }

    @Override
    public String getMethodDesc(String className, String methodName, String methodDesc) {
        return methodDesc;
    }

    @Override
    public String getFieldName(String className, String fieldName, String fieldDesc) {
        return fieldName;
    }

    @Override
    public String getFieldDesc(String className, String fieldName, String fieldDesc) {
        return fieldDesc;
    }

}
