package io.github.brqnko.bytekin.transformer.api;

import io.github.brqnko.bytekin.data.MethodData;
import io.github.brqnko.bytekin.data.VariableModification;
import io.github.brqnko.bytekin.injection.Inject;
import io.github.brqnko.bytekin.injection.Invoke;
import io.github.brqnko.bytekin.injection.ModifyVariable;
import io.github.brqnko.bytekin.injection.Redirect;
import io.github.brqnko.bytekin.logging.ILogger;
import io.github.brqnko.bytekin.mapping.IMappingProvider;
import io.github.brqnko.bytekin.transformer.method.InjectMethodTransformer;
import io.github.brqnko.bytekin.transformer.method.InvokeMethodTransformer;
import io.github.brqnko.bytekin.transformer.method.ModifyVariableMethodTransformer;
import io.github.brqnko.bytekin.transformer.method.RedirectMethodTransformer;
import io.github.brqnko.bytekin.transformer.visitor.BytekinMethodVisitor;
import org.objectweb.asm.MethodVisitor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IBytekinMethodTransformer {

    default void beforeCode(MethodVisitor mv, BytekinMethodVisitor visitor) {}

    default void beforeInsn(MethodVisitor mv, BytekinMethodVisitor visitor, int opcode) {}

    default void beforeMethodInsn(MethodVisitor mv, BytekinMethodVisitor visitor, int opcode, String owner, String name, String descriptor, boolean isInterface) {}

    default void afterMethodInsn(MethodVisitor mv, BytekinMethodVisitor visitor, int opcode, String owner, String name, String descriptor, boolean isInterface) {}

    default void beforeFieldInsn(MethodVisitor mv, BytekinMethodVisitor visitor, int opcode, String owner, String name, String descriptor) {}

    default void afterFieldInsn(MethodVisitor mv, BytekinMethodVisitor visitor, int opcode, String owner, String name, String descriptor) {}

    default boolean transformMethodInsn(MethodVisitor mv, BytekinMethodVisitor visitor, int opcode, String owner, String name, String descriptor, boolean isInterface) {
        return false;
    }

    default boolean transformFieldInsn(MethodVisitor mv, BytekinMethodVisitor visitor, int opcode, String owner, String name, String descriptor) {
        return false;
    }

    default boolean transformLdcInsn(MethodVisitor mv, BytekinMethodVisitor visitor, Object value) {
        return false;
    }

    default boolean transformVarInsn(MethodVisitor mv, BytekinMethodVisitor visitor, int opcode, int varIndex) {
        return false;
    }

    static Map<MethodData, List<IBytekinMethodTransformer>> createTransformers(ILogger logger, IMappingProvider mapping, Class<?> clazz, String className) {

        Map<MethodData, List<IBytekinMethodTransformer>> transformers = new HashMap<>();

        for (Method method : clazz.getDeclaredMethods()) {

            for (Annotation annotation : method.getDeclaredAnnotations()) {
                if (annotation instanceof Inject) {
                    Inject inject = (Inject) annotation;
                    MethodData methodData = new MethodData(
                            mapping.getMethodName(className, inject.methodName(), inject.methodDesc()),
                            mapping.getDesc(inject.methodDesc())
                    );
                    List<IBytekinMethodTransformer> methodTransformers = transformers.computeIfAbsent(methodData, k -> new ArrayList<>());
                    methodTransformers.add(new InjectMethodTransformer(logger, mapping, method, inject, className));
                }

                if (annotation instanceof Invoke) {
                    Invoke invoke = (Invoke) annotation;
                    MethodData methodData = new MethodData(
                            mapping.getMethodName(className, invoke.targetMethodName(), invoke.targetMethodDesc()),
                            mapping.getDesc(invoke.targetMethodDesc())
                    );
                    List<IBytekinMethodTransformer> methodTransformers = transformers.computeIfAbsent(methodData, k -> new ArrayList<>());
                    methodTransformers.add(new InvokeMethodTransformer(logger, mapping, clazz, method, invoke, className));
                }

                if (annotation instanceof Redirect) {
                    Redirect redirect = (Redirect) annotation;
                    MethodData methodData = new MethodData(
                            mapping.getMethodName(className, redirect.targetMethodName(), redirect.targetMethodDesc()),
                            mapping.getDesc(redirect.targetMethodDesc())
                    );
                    List<IBytekinMethodTransformer> methodTransformers = transformers.computeIfAbsent(methodData, k -> new ArrayList<>());
                    methodTransformers.add(new RedirectMethodTransformer(
                            mapping,
                            redirect.type(),
                            redirect.owner(),
                            redirect.name(),
                            redirect.desc(),
                            redirect.ordinal(),
                            clazz.getName(),
                            method.getName()
                    ));
                }

                if (annotation instanceof ModifyVariable) {
                    ModifyVariable modifyVariable = (ModifyVariable) annotation;
                    MethodData methodData = new MethodData(
                            mapping.getMethodName(className, modifyVariable.targetMethodName(), modifyVariable.targetMethodDesc()),
                            mapping.getDesc(modifyVariable.targetMethodDesc())
                    );
                    List<IBytekinMethodTransformer> methodTransformers = transformers.computeIfAbsent(methodData, k -> new ArrayList<>());
                    VariableModification modification = new VariableModification(
                            modifyVariable.targetMethodName(),
                            modifyVariable.targetMethodDesc(),
                            modifyVariable.target(),
                            modifyVariable.ordinal(),
                            modifyVariable.index(),
                            modifyVariable.argsOnly(),
                            modifyVariable.captureSelf(),
                            modifyVariable.variableDesc(),
                            clazz.getName(),
                            method.getName()
                    );
                    methodTransformers.add(new ModifyVariableMethodTransformer(className.replace('.', '/'), methodData.getName(), methodData.getDescriptor(), modification));
                }
            }

        }

        return transformers;
    }

}
