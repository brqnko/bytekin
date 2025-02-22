package io.github.brqnko.bytekin.transformer.visitor;

import io.github.brqnko.bytekin.transformer.api.IBytekinMethodTransformer;
import lombok.Getter;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

@Getter
public class BytekinMethodVisitor extends MethodVisitor {

    private final List<IBytekinMethodTransformer> transformers;

    private final int access;
    private final String name;
    private final String descriptor;
    private final String signature;
    private final String[] exceptions;

    public BytekinMethodVisitor(int api, MethodVisitor visitor, List<IBytekinMethodTransformer> transformers, int access, String name, String descriptor, String signature, String[] exceptions) {
        super(api, visitor);
        this.transformers = transformers;
        this.access = access;
        this.name = name;
        this.descriptor = descriptor;
        this.signature = signature;
        this.exceptions = exceptions;
    }

    @Override
    public void visitCode() {
        transformers.forEach(transformer -> transformer.beforeCode(mv, this));
        super.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
        transformers.forEach(transformer -> transformer.beforeInsn(mv, this, opcode));
        super.visitInsn(opcode);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        transformers.forEach(transformer -> transformer.beforeMethodInsn(mv, this, opcode, owner, name, descriptor, isInterface));
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        transformers.forEach(transformer -> transformer.afterMethodInsn(mv, this, opcode, owner, name, descriptor, isInterface));
    }
}
