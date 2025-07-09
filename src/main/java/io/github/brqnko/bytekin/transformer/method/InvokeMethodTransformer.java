package io.github.brqnko.bytekin.transformer.method;

import io.github.brqnko.bytekin.injection.CallbackInfo;
import io.github.brqnko.bytekin.injection.Invoke;
import io.github.brqnko.bytekin.injection.Shift;
import io.github.brqnko.bytekin.logging.ILogger;
import io.github.brqnko.bytekin.mapping.IMappingProvider;
import io.github.brqnko.bytekin.data.TypeData;
import io.github.brqnko.bytekin.transformer.api.IBytekinMethodTransformer;
import io.github.brqnko.bytekin.transformer.visitor.BytekinMethodVisitor;
import io.github.brqnko.bytekin.util.BytecodeManipulator;
import io.github.brqnko.bytekin.util.DescriptorParser;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;
import java.util.List;

public class InvokeMethodTransformer implements IBytekinMethodTransformer {

    private final List<TypeData> invokeParameters;

    private final Shift shift;

    private final String targetMethodOwner;
    private final String targetMethodName;
    private final String targetMethodDesc;

    private final String invokeMethodOwner;
    private final String invokeMethodName;
    private final String invokeMethodDesc;

    private final String callMethodOwner;
    private final String callMethodName;

    private boolean isStatic;

    public InvokeMethodTransformer(ILogger logger, IMappingProvider mapping, Class<?> clazz, Method method, Invoke invoke, String className) {
        this.shift = invoke.shift();

        this.targetMethodOwner = className.replaceAll("\\.", "/");
        this.targetMethodName = mapping.getMethodName(className, invoke.targetMethodName(), invoke.targetMethodDesc());
        this.targetMethodDesc = mapping.getMethodDesc(className, invoke.targetMethodName(), invoke.targetMethodDesc());

        this.invokeMethodOwner = mapping.getClassName(invoke.invokeMethodOwner()).replaceAll("\\.", "/");
        this.invokeMethodName = mapping.getMethodName(invoke.invokeMethodOwner(), invoke.invokeMethodName(), invoke.invokeMethodDesc());
        this.invokeMethodDesc = mapping.getMethodDesc(invoke.invokeMethodOwner(), invoke.invokeMethodName(), invoke.invokeMethodDesc());

        this.callMethodOwner = clazz.getName().replace(".", "/");
        this.callMethodName = method.getName();

        this.invokeParameters = DescriptorParser.parseParameterTypes(invokeMethodDesc);
    }

    public InvokeMethodTransformer(ILogger logger, String targetMethodOwner, String targetMethodName, String targetMethodDesc, String invokeMethodOwner, String invokeMethodName, String invokeMethodDesc, Shift shift, String callMethodOwner, String callMethodName) {
        this.shift = shift;
        this.targetMethodOwner = targetMethodOwner.replaceAll("\\.", "/");
        this.targetMethodName = targetMethodName;
        this.targetMethodDesc = targetMethodDesc;
        this.invokeMethodOwner = invokeMethodOwner.replaceAll("\\.", "/");
        this.invokeMethodName = invokeMethodName;
        this.invokeMethodDesc = invokeMethodDesc;

        this.callMethodOwner = callMethodOwner.replaceAll("\\.", "/");
        this.callMethodName = callMethodName;

        this.invokeParameters = DescriptorParser.parseParameterTypes(invokeMethodDesc);
    }

    @Override
    public void beforeCode(MethodVisitor mv, BytekinMethodVisitor visitor) {
        isStatic = (visitor.getAccess() & Opcodes.ACC_STATIC) != 0;
        IBytekinMethodTransformer.super.beforeCode(mv, visitor);
    }

    private String getCallMethodDesc(String targetMethodOwner, String targetDesc, boolean isStatic) {
        StringBuilder builder = new StringBuilder();

        builder.append("(");

        if (!isStatic) {
            builder.append("L");
            builder.append(targetMethodOwner);
            builder.append(";");
        }

        builder.append(targetDesc, 1, targetDesc.indexOf(")"));

        for (TypeData data : this.invokeParameters) {
            builder.append(data.getDesc());
        }

        builder.append(")");

        builder.append(CallbackInfo.CALLBACK_DESC);

        return builder.toString();
    }

    private void invoke(MethodVisitor mv, int localIndex, int callbackInfoIndex) {
        // load parameters from local variables to stack
        if (!isStatic) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
        }

        // load all parameters that target method has to stack
        int offset = isStatic ? 0 : 1;
        List<TypeData> types = DescriptorParser.parseParameterTypes(targetMethodDesc);
        for (int i = 0; i < types.size(); i++) {
            BytecodeManipulator.load(mv, types.get(i).getCategory(), i + offset);
        }

        // load all parameters that invoke method has to stack
        for (int i = 0; i < invokeParameters.size(); i++) {
            TypeData typeData = invokeParameters.get(i);
            BytecodeManipulator.load(mv, typeData.getCategory(), localIndex + i);
        }

        // invoke the method
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, callMethodOwner, callMethodName, getCallMethodDesc(targetMethodOwner, targetMethodDesc, isStatic), false);

        // assign the CallbackInfo to variable
        mv.visitVarInsn(Opcodes.ASTORE, callbackInfoIndex);

        // load CallbackInfo#cancelled to stack
        mv.visitVarInsn(Opcodes.ALOAD, callbackInfoIndex);
        mv.visitFieldInsn(Opcodes.GETFIELD, CallbackInfo.CALLBACK_OWNER, CallbackInfo.FIELD_CANCELLED, "Z");

        // create a label for the jump
        Label cancelledLabel = new Label();

        // if CallbackInfo#cancelled is true, jump to the label
        mv.visitJumpInsn(Opcodes.IFEQ, cancelledLabel);

        // load CallbackInfo#returnValue to stack
        mv.visitVarInsn(Opcodes.ALOAD, callbackInfoIndex);
        mv.visitFieldInsn(Opcodes.GETFIELD, CallbackInfo.CALLBACK_OWNER, CallbackInfo.FIELD_RETURN_VALUE, "Ljava/lang/Object;");

        // cast the return value to the target method return type
        TypeData returnType = DescriptorParser.parseReturnType(targetMethodDesc);
        BytecodeManipulator.cast(mv, returnType);
        BytecodeManipulator.doReturn(mv, returnType);

        // mark the label
        mv.visitLabel(cancelledLabel);
    }

    @Override
    public void beforeMethodInsn(MethodVisitor mv, BytekinMethodVisitor visitor, int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (!owner.equals(invokeMethodOwner) || !name.equals(invokeMethodName) || !descriptor.equals(invokeMethodDesc)) {
            IBytekinMethodTransformer.super.beforeMethodInsn(mv, visitor, opcode, owner, name, descriptor, isInterface);
            return;
        }
        // now we have parameters in stack and assign them to local variables
        int localIndex = 10000;
        int callbackInfoIndex = localIndex + invokeParameters.size();
        for (int i = invokeParameters.size() - 1; i >= 0; i--) {
            TypeData typeData = invokeParameters.get(i);
            BytecodeManipulator.store(mv, typeData.getCategory(), localIndex + i);
        }

        if (shift == Shift.BEFORE) {
            invoke(mv, localIndex, callbackInfoIndex);
        }

        if (shift == Shift.BEFORE) {
            // load parameters from CallbackInfo#modifiedArgs to stack
            List<TypeData> params = DescriptorParser.parseParameterTypes(this.invokeMethodDesc);
            for (int i = 0; i < params.size(); i++) {
                mv.visitVarInsn(Opcodes.ALOAD, callbackInfoIndex);
                mv.visitFieldInsn(Opcodes.GETFIELD, CallbackInfo.CALLBACK_OWNER, CallbackInfo.FIELD_MODIFY_ARGS, "[Ljava/lang/Object;");
                mv.visitIntInsn(Opcodes.BIPUSH, i);
                mv.visitInsn(Opcodes.AALOAD);
                BytecodeManipulator.cast(mv, params.get(i));
            }
        } else {
            // load parameters from local variables to stack
            for (int i = 0; i < invokeParameters.size(); i++) {
                TypeData typeData = invokeParameters.get(i);
                BytecodeManipulator.load(mv, typeData.getCategory(), localIndex + i);
            }
        }
        IBytekinMethodTransformer.super.beforeMethodInsn(mv, visitor, opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void afterMethodInsn(MethodVisitor mv, BytekinMethodVisitor visitor, int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (!owner.equals(invokeMethodOwner) || !name.equals(invokeMethodName) || !descriptor.equals(invokeMethodDesc)) {
            IBytekinMethodTransformer.super.afterMethodInsn(mv, visitor, opcode, owner, name, descriptor, isInterface);
            return;
        }

        if (shift == Shift.AFTER) {
            int localIndex = 10000;
            int callbackInfoIndex = localIndex + invokeParameters.size();
            invoke(mv, localIndex, callbackInfoIndex);
        }

        IBytekinMethodTransformer.super.afterMethodInsn(mv, visitor, opcode, owner, name, descriptor, isInterface);
    }
}
