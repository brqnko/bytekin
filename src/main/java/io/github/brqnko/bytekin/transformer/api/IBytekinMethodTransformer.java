package io.github.brqnko.bytekin.transformer.api;

import io.github.brqnko.bytekin.injection.Inject;
import io.github.brqnko.bytekin.injection.Invoke;
import io.github.brqnko.bytekin.logging.ILogger;
import io.github.brqnko.bytekin.mapping.IMappingProvider;
import io.github.brqnko.bytekin.target.MethodData;
import io.github.brqnko.bytekin.transformer.method.InjectMethodTransformer;
import io.github.brqnko.bytekin.transformer.method.InvokeMethodTransformer;
import io.github.brqnko.bytekin.transformer.visitor.BytekinMethodVisitor;
import org.objectweb.asm.MethodVisitor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public interface IBytekinMethodTransformer {

    default void beforeCode(MethodVisitor mv, BytekinMethodVisitor visitor) {}

    default void beforeInsn(MethodVisitor mv, BytekinMethodVisitor visitor, int opcode) {}

    default void beforeMethodInsn(MethodVisitor mv, BytekinMethodVisitor visitor, int opcode, String owner, String name, String descriptor, boolean isInterface) {}

    default void afterMethodInsn(MethodVisitor mv, BytekinMethodVisitor visitor, int opcode, String owner, String name, String descriptor, boolean isInterface) {}

    static Map<MethodData, List<IBytekinMethodTransformer>> createTransformers(ILogger logger, IMappingProvider mapping, Class<?> clazz, String className) {

        Map<MethodData, List<IBytekinMethodTransformer>> transformers = new HashMap<>();

        for (Method method : clazz.getDeclaredMethods()) {

            for (Annotation annotation : method.getDeclaredAnnotations()) {
                if (annotation instanceof Inject inject) {
                    MethodData methodData = new MethodData(
                            mapping.getMethodName(clazz.getName(), inject.methodName(), inject.methodDesc()),
                            mapping.getMethodDesc(clazz.getName(), inject.methodName(), inject.methodDesc())
                    );
                    List<IBytekinMethodTransformer> methodTransformers = transformers.computeIfAbsent(methodData, k -> new ArrayList<>());
                    methodTransformers.add(new InjectMethodTransformer(logger, mapping, clazz, method, inject, className));
                }

                if (annotation instanceof Invoke invoke) {
                    MethodData methodData = new MethodData(
                            mapping.getMethodName(clazz.getName(), invoke.targetMethodName(), invoke.targetMethodDesc()),
                            mapping.getMethodDesc(clazz.getName(), invoke.targetMethodName(), invoke.targetMethodDesc())
                    );
                    List<IBytekinMethodTransformer> methodTransformers = transformers.computeIfAbsent(methodData, k -> new ArrayList<>());
                    methodTransformers.add(new InvokeMethodTransformer(logger, mapping, clazz, method, invoke, className));
                }
            }

        }

        return transformers;
    }

}
