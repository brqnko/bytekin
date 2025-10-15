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
        boolean handled = false;
        for (IBytekinMethodTransformer transformer : transformers) {
            if (!handled) {
                handled = transformer.transformMethodInsn(mv, this, opcode, owner, name, descriptor, isInterface);
            }
            if (!handled) {
                transformer.beforeMethodInsn(mv, this, opcode, owner, name, descriptor, isInterface);
            }
        }

        if (!handled) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        if (!handled) {
            transformers.forEach(transformer -> transformer.afterMethodInsn(mv, this, opcode, owner, name, descriptor, isInterface));
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        boolean handled = false;
        for (IBytekinMethodTransformer transformer : transformers) {
            if (!handled) {
                handled = transformer.transformFieldInsn(mv, this, opcode, owner, name, descriptor);
            }
            if (!handled) {
                transformer.beforeFieldInsn(mv, this, opcode, owner, name, descriptor);
            }
        }

        if (!handled) {
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }

        if (!handled) {
            transformers.forEach(transformer -> transformer.afterFieldInsn(mv, this, opcode, owner, name, descriptor));
        }
    }

    @Override
    public void visitLdcInsn(Object value) {
        boolean handled = false;
        for (IBytekinMethodTransformer transformer : transformers) {
            if (!handled) {
                handled = transformer.transformLdcInsn(mv, this, value);
            }
        }

        if (!handled) {
            super.visitLdcInsn(value);
        }
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        boolean handled = false;
        for (IBytekinMethodTransformer transformer : transformers) {
            if (!handled) {
                handled = transformer.transformVarInsn(mv, this, opcode, var);
            }
        }

        if (!handled) {
            super.visitVarInsn(opcode, var);
        }
    }
}
