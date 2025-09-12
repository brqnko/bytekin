package io.github.brqnko.bytekin.transformer;

import io.github.brqnko.bytekin.data.Injection;
import io.github.brqnko.bytekin.data.Invocation;
import io.github.brqnko.bytekin.logging.ILogger;
import io.github.brqnko.bytekin.mapping.IMappingProvider;
import io.github.brqnko.bytekin.data.MethodData;
import io.github.brqnko.bytekin.transformer.api.IBytekinMethodTransformer;
import io.github.brqnko.bytekin.transformer.method.InjectMethodTransformer;
import io.github.brqnko.bytekin.transformer.method.InvokeMethodTransformer;
import io.github.brqnko.bytekin.transformer.visitor.BytekinClassVisitor;
import lombok.Getter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class BytekinClassTransformer {

    private final Map<MethodData, List<IBytekinMethodTransformer>> methodTransformers;

    public BytekinClassTransformer() {
        this.methodTransformers = new HashMap<>();
    }

    public BytekinClassTransformer(ILogger logger, IMappingProvider mapping, Class<?> clazz, String className) {

        this.methodTransformers = IBytekinMethodTransformer.createTransformers(logger, mapping, clazz, className);
    }

    public void addInjection(ILogger logger, IMappingProvider mapping, Injection injection, String className) {
        List<IBytekinMethodTransformer> transformer = this.methodTransformers.computeIfAbsent(
                new MethodData(
                        mapping.getMethodName(className, injection.getMethodName(), injection.getMethodDesc()),
                        mapping.getMethodDesc(className, injection.getMethodName(), injection.getMethodDesc())),
                k -> new ArrayList<>());

        transformer.add(new InjectMethodTransformer(
                logger,
                className,
                mapping.getMethodName(className, injection.getMethodName(), injection.getMethodDesc()),
                mapping.getMethodDesc(className, injection.getMethodName(), injection.getMethodDesc()),
                injection.getHookMethodOwner(),
                injection.getHookMethodName(),
                injection.getAt()
                ));
    }

    public void addInvocation(ILogger logger, IMappingProvider mapping, Invocation invocation, String className) {
        List<IBytekinMethodTransformer> transformer = this.methodTransformers.computeIfAbsent(
                new MethodData(
                        mapping.getMethodName(className, invocation.getTargetMethodName(), invocation.getTargetMethodDesc()),
                        mapping.getMethodDesc(className, invocation.getTargetMethodName(), invocation.getTargetMethodDesc())
                ),
                k -> new ArrayList<>()
        );

        String invokeOwner = mapping.getClassName(invocation.getInvokeMethodOwner());

        transformer.add(new InvokeMethodTransformer(
                logger,
                className,
                mapping.getMethodName(className, invocation.getTargetMethodName(), invocation.getTargetMethodDesc()),
                mapping.getMethodDesc(className, invocation.getTargetMethodName(), invocation.getInvokeMethodDesc()),
                invokeOwner,
                mapping.getMethodName(invokeOwner, invocation.getInvokeMethodName(), invocation.getInvokeMethodDesc()),
                mapping.getMethodDesc(invokeOwner, invocation.getInvokeMethodName(), invocation.getInvokeMethodDesc()),
                invocation.getShift(),
                invocation.getHookMethodOwner(),
                invocation.getHookMethodName()
                ));
    }

    public byte[] transform(byte[] bytes, int api) {

        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                try {
                    return super.getCommonSuperClass(type1, type2);
                } catch (Exception e) {
                    return "java/lang/Object";
                }
            }
        };

        reader.accept(new BytekinClassVisitor(api, writer, this), 0);

        return writer.toByteArray();
    }

}
