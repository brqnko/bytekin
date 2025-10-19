# Custom Transformers

Beyond annotations, bytekin allows you to create custom transformers for advanced use cases.

## Creating Custom Transformers

You can extend the transformation system by implementing custom logic:

```java
public class CustomTransformer implements IBytekinMethodTransformer {
    @Override
    public byte[] transform(byte[] bytecode) {
        // Custom transformation logic
        return bytecode;
    }
}
```

## Advanced Customization

For more complex scenarios, work directly with ASM visitor pattern:

```java
public class AdvancedCustomTransformer extends ClassVisitor {
    public AdvancedCustomTransformer(ClassVisitor cv) {
        super(ASM9, cv);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, 
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        
        // Return custom method visitor
        return new MethodVisitor(ASM9, mv) {
            @Override
            public void visitCode() {
                // Custom method instrumentation
                super.visitCode();
            }
        };
    }
}
```

## Combining Custom and Built-in Transformations

Mix custom transformers with bytekin's built-in features:

```java
BytekinTransformer transformer = new BytekinTransformer.Builder(BuiltInHooks.class)
    .build();

// Apply custom transformation after
byte[] original = getClassBytecode("com.example.MyClass");
byte[] withBuiltIn = transformer.transform("com.example.MyClass", original);
byte[] withCustom = applyCustom(withBuiltIn);
```

## Performance Considerations

- Keep custom transformers efficient
- Cache transformation results when possible
- Profile custom code for hotspots

## Next Steps

- Review [Advanced Usage](./advanced-usage.md)
- Check [Best Practices](./best-practices.md)
- Explore [Examples](./examples.md)
