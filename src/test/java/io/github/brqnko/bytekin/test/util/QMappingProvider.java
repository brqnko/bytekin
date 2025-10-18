package io.github.brqnko.bytekin.test.util;

import io.github.brqnko.bytekin.mapping.IMappingProvider;

/**
 * Simple MappingProvider which just appends "Q" to the end of the name
 */
public class QMappingProvider implements IMappingProvider {
    @Override
    public String getClassName(String className) {
        return className + "Q";
    }

    @Override
    public String getMethodName(String className, String methodName, String methodDesc) {
        return methodName + "Q";
    }

    @Override
    public String getFieldName(String className, String fieldName, String fieldDesc) {
        return fieldName + "Q";
    }

    @Override
    public String getDesc(String desc) {
        return desc;
    }
}
