package io.github.brqnko.bytekin.transformer.visitor;

import io.github.brqnko.bytekin.data.MethodData;
import io.github.brqnko.bytekin.transformer.BytekinClassTransformer;
import io.github.brqnko.bytekin.transformer.api.IBytekinMethodTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

public class BytekinClassVisitor extends ClassVisitor {

    private final BytekinClassTransformer transformer;

    public BytekinClassVisitor(int api, ClassWriter writer, BytekinClassTransformer transformer) {
        super(api, writer);
        this.transformer = transformer;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);

        List<IBytekinMethodTransformer> transformers = transformer.getMethodTransformers().get(new MethodData(name, descriptor));
        if (transformers == null) {
            return visitor;
        }

        return new BytekinMethodVisitor(api, visitor, transformers, access, name, descriptor, signature, exceptions);
    }
}
