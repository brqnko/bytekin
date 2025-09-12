package io.github.brqnko.bytekin.transformer.api;

import io.github.brqnko.bytekin.data.Invocation;
import io.github.brqnko.bytekin.injection.Inject;
import io.github.brqnko.bytekin.injection.Invoke;
import io.github.brqnko.bytekin.logging.ILogger;
import io.github.brqnko.bytekin.mapping.IMappingProvider;
import io.github.brqnko.bytekin.data.MethodData;
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
                if (annotation instanceof Inject) {
                    Inject inject = (Inject) annotation;
                    MethodData methodData = new MethodData(
                            mapping.getMethodName(className, inject.methodName(), inject.methodDesc()),
                            mapping.getMethodDesc(className, inject.methodName(), inject.methodDesc())
                    );
                    List<IBytekinMethodTransformer> methodTransformers = transformers.computeIfAbsent(methodData, k -> new ArrayList<>());
                    methodTransformers.add(new InjectMethodTransformer(logger, mapping, method, inject, className));
                }

                if (annotation instanceof Invoke) {
                    Invoke invoke = (Invoke) annotation;
                    MethodData methodData = new MethodData(
                            mapping.getMethodName(className, invoke.targetMethodName(), invoke.targetMethodDesc()),
                            mapping.getMethodDesc(className, invoke.targetMethodName(), invoke.targetMethodDesc())
                    );
                    List<IBytekinMethodTransformer> methodTransformers = transformers.computeIfAbsent(methodData, k -> new ArrayList<>());
                    methodTransformers.add(new InvokeMethodTransformer(logger, mapping, clazz, method, invoke, className));
                }
            }

        }

        return transformers;
    }

}
