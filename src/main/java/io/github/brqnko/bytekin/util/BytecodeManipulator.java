package io.github.brqnko.bytekin.util;

import io.github.brqnko.bytekin.data.TypeCategory;
import io.github.brqnko.bytekin.data.TypeData;
import lombok.experimental.UtilityClass;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@UtilityClass
public class BytecodeManipulator {

    public void cast(MethodVisitor mv, TypeData returnType) {
        switch (returnType.getCategory()) {
            case BOOLEAN:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                break;
            case BYTE:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Byte");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false);
                break;
            case CHAR:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Character");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
                break;
            case SHORT:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Short");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false);
                break;
            case INT:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
                break;
            case LONG:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Long");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
                break;
            case FLOAT:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Float");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
                break;
            case DOUBLE:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Double");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
                break;
            case OBJECT:
            case ARRAY:
                mv.visitTypeInsn(Opcodes.CHECKCAST, returnType.getDesc());
                break;
        }
    }

    public void doReturn(MethodVisitor mv, TypeData returnType) {
        switch (returnType.getCategory()) {
            case VOID:
                mv.visitInsn(Opcodes.RETURN);
                break;
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case SHORT:
            case INT:
                mv.visitInsn(Opcodes.IRETURN);
                break;
            case LONG:
                mv.visitInsn(Opcodes.LRETURN);
                break;
            case FLOAT:
                mv.visitInsn(Opcodes.FRETURN);
                break;
            case DOUBLE:
                mv.visitInsn(Opcodes.DRETURN);
                break;
            case OBJECT:
            case ARRAY:
                mv.visitInsn(Opcodes.ARETURN);
                break;
        }
    }

    public void load(MethodVisitor mv, TypeCategory returnType, int varIndex) {
        switch (returnType) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case SHORT:
            case INT:
                mv.visitVarInsn(Opcodes.ILOAD, varIndex);
                break;
            case LONG:
                mv.visitVarInsn(Opcodes.LLOAD, varIndex);
                break;
            case FLOAT:
                mv.visitVarInsn(Opcodes.FLOAD, varIndex);
                break;
            case DOUBLE:
                mv.visitVarInsn(Opcodes.DLOAD, varIndex);
                break;
            case OBJECT:
            case ARRAY:
                mv.visitVarInsn(Opcodes.ALOAD, varIndex);
                break;
        }
    }

    public static void store(MethodVisitor mv, TypeCategory category, int i) {
        switch (category) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case SHORT:
            case INT:
                mv.visitVarInsn(Opcodes.ISTORE, i);
                break;
            case LONG:
                mv.visitVarInsn(Opcodes.LSTORE, i);
                break;
            case FLOAT:
                mv.visitVarInsn(Opcodes.FSTORE, i);
                break;
            case DOUBLE:
                mv.visitVarInsn(Opcodes.DSTORE, i);
                break;
            case OBJECT:
            case ARRAY:
                mv.visitVarInsn(Opcodes.ASTORE, i);
                break;
        }
    }
}
