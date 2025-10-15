package io.github.brqnko.bytekin.transformer.method;

import io.github.brqnko.bytekin.transformer.api.IBytekinMethodTransformer;
import io.github.brqnko.bytekin.transformer.visitor.BytekinMethodVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ModifyConstantMethodTransformer implements IBytekinMethodTransformer {

    private final String targetMethodName;
    private final String targetMethodDesc;

    private final Object constantValue;
    private final int ordinal;

    private final String hookOwner;
    private final String hookName;
    private final String hookDesc;

    private int occurrence;

    public ModifyConstantMethodTransformer(String targetMethodName,
                                           String targetMethodDesc,
                                           Object constantValue,
                                           int ordinal,
                                           String hookOwner,
                                           String hookName) {
        this.targetMethodName = targetMethodName;
        this.targetMethodDesc = targetMethodDesc;

        this.constantValue = constantValue;
        this.ordinal = ordinal;

        this.hookOwner = hookOwner.replace('.', '/');
        this.hookName = hookName;
        this.hookDesc = buildHookDescriptor(constantValue);
    }

    private String buildHookDescriptor(Object constant) {
        String typeDesc;
        if (constant instanceof Double) {
            typeDesc = "D";
        } else if (constant instanceof Float) {
            typeDesc = "F";
        } else if (constant instanceof Long) {
            typeDesc = "J";
        } else if (constant instanceof Integer || constant instanceof Short || constant instanceof Byte || constant instanceof Boolean) {
            typeDesc = "I";
        } else if (constant instanceof Character) {
            typeDesc = "C";
        } else if (constant instanceof String) {
            typeDesc = "Ljava/lang/String;";
        } else {
            throw new IllegalArgumentException("Unsupported constant type: " + constant.getClass());
        }
        return "(" + typeDesc + ")" + typeDesc;
    }

    private boolean matchesContext(BytekinMethodVisitor visitor) {
        return visitor.getName().equals(targetMethodName) && visitor.getDescriptor().equals(targetMethodDesc);
    }

    private boolean matchesConstant(Object value) {
        if (constantValue == null) {
            return value == null;
        }
        return constantValue.equals(value);
    }

    private boolean shouldHandle(int current) {
        return ordinal < 0 || current == ordinal;
    }

    @Override
    public void beforeCode(MethodVisitor mv, BytekinMethodVisitor visitor) {
        occurrence = 0;
        IBytekinMethodTransformer.super.beforeCode(mv, visitor);
    }

    @Override
    public boolean transformLdcInsn(MethodVisitor mv, BytekinMethodVisitor visitor, Object value) {
        if (!matchesContext(visitor)) {
            return false;
        }

        if (!matchesConstant(value)) {
            return false;
        }

        int current = occurrence++;
        if (!shouldHandle(current)) {
            return false;
        }

        pushConstant(mv, value);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, hookOwner, hookName, hookDesc, false);
        return true;
    }

    private void pushConstant(MethodVisitor mv, Object value) {
        if (value == null) {
            mv.visitInsn(Opcodes.ACONST_NULL);
            return;
        }
        mv.visitLdcInsn(value);
    }
}
