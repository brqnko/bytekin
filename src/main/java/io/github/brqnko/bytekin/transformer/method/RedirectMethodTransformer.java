package io.github.brqnko.bytekin.transformer.method;

import io.github.brqnko.bytekin.injection.RedirectType;
import io.github.brqnko.bytekin.mapping.IMappingProvider;
import io.github.brqnko.bytekin.transformer.api.IBytekinMethodTransformer;
import io.github.brqnko.bytekin.transformer.visitor.BytekinMethodVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class RedirectMethodTransformer implements IBytekinMethodTransformer {

    private final RedirectType type;
    private final String targetOwner;
    private final String targetName;
    private final String targetDesc;
    private final int ordinal;

    private final String hookOwner;
    private final String hookName;

    private int occurrences;
    private boolean handled;

    public RedirectMethodTransformer(IMappingProvider mapping, RedirectType type,
                                     String owner, String name, String desc, int ordinal,
                                     String hookMethodOwner, String hookMethodName) {
        this.type = type;
        this.ordinal = ordinal;

        String normalizedOwner = owner.replace('/', '.');
        String mappedOwner = mapping.getClassName(normalizedOwner);
        this.targetOwner = mappedOwner.replace('.', '/');

        switch (type) {
            case METHOD:
                this.targetName = mapping.getMethodName(normalizedOwner, name, desc);
                this.targetDesc = mapping.getMethodDesc(normalizedOwner, name, desc);
                break;
            case FIELD_GET:
            case FIELD_SET:
                this.targetName = mapping.getFieldName(normalizedOwner, name, desc);
                this.targetDesc = mapping.getFieldDesc(normalizedOwner, name, desc);
                break;
            default:
                throw new IllegalStateException("Unsupported redirect type: " + type);
        }

        this.hookOwner = hookMethodOwner.replace('.', '/');
        this.hookName = hookMethodName;
    }

    private boolean shouldHandle(int currentOrdinal) {
        if (handled) {
            return true;
        }

        if (ordinal >= 0) {
            if (currentOrdinal == ordinal) {
                handled = true;
                return false;
            }
            return true;
        }

        handled = true;
        return false;
    }

    @Override
    public boolean transformMethodInsn(MethodVisitor mv, BytekinMethodVisitor visitor, int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (type != RedirectType.METHOD) {
            return false;
        }

        if (!owner.equals(targetOwner) || !name.equals(targetName) || !descriptor.equals(targetDesc)) {
            return false;
        }

        int current = occurrences++;
        if (shouldHandle(current)) {
            return false;
        }

        String hookDescriptor = buildMethodHookDescriptor(opcode, descriptor, owner);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, hookOwner, hookName, hookDescriptor, false);
        return true;
    }

    @Override
    public boolean transformFieldInsn(MethodVisitor mv, BytekinMethodVisitor visitor, int opcode, String owner, String name, String descriptor) {
        if (type == RedirectType.METHOD) {
            return false;
        }

        if (!owner.equals(targetOwner) || !name.equals(targetName) || !descriptor.equals(targetDesc)) {
            return false;
        }

        int current = occurrences++;
        if (shouldHandle(current)) {
            return false;
        }

        String hookDescriptor;
        if (type == RedirectType.FIELD_GET) {
            hookDescriptor = buildFieldGetHookDescriptor(opcode, descriptor, owner);
        } else {
            hookDescriptor = buildFieldSetHookDescriptor(opcode, descriptor, owner);
        }

        mv.visitMethodInsn(Opcodes.INVOKESTATIC, hookOwner, hookName, hookDescriptor, false);
        return true;
    }

    private String buildMethodHookDescriptor(int opcode, String descriptor, String owner) {
        if (opcode == Opcodes.INVOKESTATIC) {
            return descriptor;
        }
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append('L').append(owner).append(';');
        builder.append(descriptor, 1, descriptor.length());
        return builder.toString();
    }

    private String buildFieldGetHookDescriptor(int opcode, String descriptor, String owner) {
        if (opcode == Opcodes.GETSTATIC) {
            return "()" + descriptor;
        }
        return "(L" + owner + ";)" + descriptor;
    }

    private String buildFieldSetHookDescriptor(int opcode, String descriptor, String owner) {
        if (opcode == Opcodes.PUTSTATIC) {
            return "(" + descriptor + ")V";
        }
        return "(L" + owner + ";" + descriptor + ")V";
    }
}
