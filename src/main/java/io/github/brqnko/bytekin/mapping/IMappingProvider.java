package io.github.brqnko.bytekin.mapping;

public interface IMappingProvider {
    String getClassName(String className);
    String getMethodName(String className, String methodName, String methodDesc);
    String getMethodDesc(String className, String methodName, String methodDesc);
    String getFieldName(String className, String fieldName, String fieldDesc);
    String getFieldDesc(String className, String fieldName, String fieldDesc);
}
