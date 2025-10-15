package io.github.brqnko.bytekin.transformer.method;

import io.github.brqnko.bytekin.data.VariableModification;
import io.github.brqnko.bytekin.injection.VariableTarget;
import io.github.brqnko.bytekin.transformer.api.IBytekinMethodTransformer;
import io.github.brqnko.bytekin.transformer.visitor.BytekinMethodVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ModifyVariableMethodTransformer implements IBytekinMethodTransformer {

    private final String ownerInternalName;
    private final String targetMethodName;
    private final String targetMethodDesc;
    private final VariableTarget target;

    private final int ordinal;
    private final int index;
    private final boolean argsOnly;
    private final boolean captureSelf;
    private final String variableDesc;

    private final String hookOwner;
    private final String hookName;

    private boolean headApplied;
    private int occurrence;

    public ModifyVariableMethodTransformer(String ownerInternalName,
                                           String mappedName,
                                           String mappedDesc,
                                           VariableModification modification) {
        this.ownerInternalName = ownerInternalName;
        this.targetMethodName = mappedName;
        this.targetMethodDesc = mappedDesc;
        this.target = modification.getTarget();
        this.ordinal = modification.getOrdinal();
        this.index = modification.getIndex();
        this.argsOnly = modification.isArgsOnly();
        this.captureSelf = modification.isCaptureSelf();
        String desc = modification.getVariableDesc();
        this.variableDesc = (desc == null || desc.isEmpty()) ? null : desc;
        this.hookOwner = modification.getHookMethodOwner().replace('.', '/');
        this.hookName = modification.getHookMethodName();
    }

    private boolean matchesContext(BytekinMethodVisitor visitor) {
        return !visitor.getName().equals(targetMethodName) || !visitor.getDescriptor().equals(targetMethodDesc);
    }

    @Override
    public void beforeCode(MethodVisitor mv, BytekinMethodVisitor visitor) {
        if (matchesContext(visitor)) {
            return;
        }

        if (target != VariableTarget.HEAD || headApplied) {
            return;
        }

        if (!argsOnly) {
            throw new IllegalStateException("HEAD modify-variable currently requires argsOnly=true");
        }

        Type[] argumentTypes = Type.getArgumentTypes(targetMethodDesc);
        if (ordinal < 0 || ordinal >= argumentTypes.length) {
            throw new IllegalArgumentException("Invalid argument ordinal for method " + targetMethodName);
        }

        int localIndex = ((visitor.getAccess() & Opcodes.ACC_STATIC) != 0) ? 0 : 1;
        for (int i = 0; i < ordinal; i++) {
            localIndex += argumentTypes[i].getSize();
        }

        Type argumentType = argumentTypes[ordinal];
        String descriptor = '(' + argumentType.getDescriptor() + ')' + argumentType.getDescriptor();

        if (captureSelf) {
            ensureInstanceContext(visitor);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(argumentType.getOpcode(Opcodes.ILOAD), localIndex);
            String ownerDescriptor = 'L' + ownerInternalName + ';';
            descriptor = '(' + ownerDescriptor + argumentType.getDescriptor() + ')' + argumentType.getDescriptor();
        } else {
            mv.visitVarInsn(argumentType.getOpcode(Opcodes.ILOAD), localIndex);
        }

        mv.visitMethodInsn(Opcodes.INVOKESTATIC, hookOwner, hookName, descriptor, false);
        mv.visitVarInsn(argumentType.getOpcode(Opcodes.ISTORE), localIndex);

        headApplied = true;
    }

    @Override
    public boolean transformVarInsn(MethodVisitor mv, BytekinMethodVisitor visitor, int opcode, int varIndex) {
        if (matchesContext(visitor) || target != VariableTarget.STORE) {
            return false;
        }

        if (!isStoreOpcode(opcode)) {
            return false;
        }

        if (!matchesDescriptor(opcode)) {
            return false;
        }

        if (index >= 0 && index != varIndex) {
            return false;
        }

        int current = occurrence;
        if (ordinal >= 0 && current != ordinal) {
            occurrence++;
            return false;
        }
        occurrence++;

        String valueDesc = resolveDescriptor(opcode);
        String descriptor = '(' + valueDesc + ')' + valueDesc;

        if (captureSelf) {
            ensureInstanceContext(visitor);

            Type valueType = Type.getType(valueDesc);
            if (valueType.getSize() != 1) {
                throw new IllegalStateException("captureSelf only supports category 1 values for now");
            }

            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitInsn(Opcodes.SWAP);

            String ownerDescriptor = 'L' + ownerInternalName + ';';
            descriptor = '(' + ownerDescriptor + valueDesc + ')' + valueDesc;
        }

        mv.visitMethodInsn(Opcodes.INVOKESTATIC, hookOwner, hookName, descriptor, false);
        mv.visitVarInsn(opcode, varIndex);
        return true;
    }

    private boolean isStoreOpcode(int opcode) {
        return opcode == Opcodes.ISTORE
                || opcode == Opcodes.LSTORE
                || opcode == Opcodes.FSTORE
                || opcode == Opcodes.DSTORE
                || opcode == Opcodes.ASTORE;
    }

    private boolean matchesDescriptor(int opcode) {
        if (variableDesc == null) {
            return true;
        }

        switch (opcode) {
            case Opcodes.ISTORE:
                return isIntCompatible(variableDesc);
            case Opcodes.LSTORE:
                return "J".equals(variableDesc);
            case Opcodes.FSTORE:
                return "F".equals(variableDesc);
            case Opcodes.DSTORE:
                return "D".equals(variableDesc);
            case Opcodes.ASTORE:
                return isObjectDescriptor(variableDesc);
            default:
                return false;
        }
    }

    private boolean isIntCompatible(String descriptor) {
        return "I".equals(descriptor)
                || "Z".equals(descriptor)
                || "B".equals(descriptor)
                || "C".equals(descriptor)
                || "S".equals(descriptor);
    }

    private boolean isObjectDescriptor(String descriptor) {
        return descriptor.startsWith("L") || descriptor.startsWith("[");
    }

    private String resolveDescriptor(int opcode) {
        if (variableDesc != null && !variableDesc.isEmpty()) {
            return variableDesc;
        }

        switch (opcode) {
            case Opcodes.ISTORE:
                return "I";
            case Opcodes.LSTORE:
                return "J";
            case Opcodes.FSTORE:
                return "F";
            case Opcodes.DSTORE:
                return "D";
            case Opcodes.ASTORE:
                return "Ljava/lang/Object;";
            default:
                throw new IllegalArgumentException("Unsupported store opcode " + opcode);
        }
    }

    private void ensureInstanceContext(BytekinMethodVisitor visitor) {
        if ((visitor.getAccess() & Opcodes.ACC_STATIC) != 0) {
            throw new IllegalStateException("captureSelf requires an instance method: " + visitor.getName() + visitor.getDescriptor());
        }
    }
}
