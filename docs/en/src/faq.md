# FAQ - Frequently Asked Questions

## General Questions

### What is bytekin?

bytekin is a lightweight Java bytecode transformation framework built on ASM. It allows you to modify Java classes at the bytecode level without touching the source code.

### Why would I need bytecode transformation?

Common use cases include:
- Adding logging without modifying source code
- Implementing cross-cutting concerns
- Testing and mocking
- Performance profiling
- Security enhancements

### How does bytekin compare to other tools?

| Tool | Size | Complexity | Use Case |
|------|------|-----------|----------|
| bytekin | Small | Simple | Direct bytecode manipulation |
| Spring AOP | Large | Complex | Enterprise framework |
| Mockito | Medium | Medium | Testing/Mocking |
| Aspect | Medium | Complex | Aspect-oriented programming |

### Is bytekin production-ready?

Yes, bytekin is designed for production use. It has minimal dependencies (only ASM) and has been tested thoroughly.

## Technical Questions

### What Java versions does bytekin support?

bytekin requires **Java 8 or higher**.

### Can I use bytekin with Spring Boot?

Yes! bytekin can work alongside Spring Boot. You would typically apply transformations during a custom `ClassLoader` setup or at build time.

### Does bytekin work with obfuscated code?

Yes, with mappings! Use the mapping system to handle obfuscated class and method names.

### Can I combine multiple transformations?

Yes! You can use multiple `@Inject`, `@Invoke`, and other annotations on the same class. They all get applied.

## Usage Questions

### How do I find the method descriptor for a method?

Use `javap`:
```bash
javap -c MyClass.class
```

Look at the method signature and convert it to JVM descriptor format:
- `int add(int a, int b)` → `(II)I`
- `String process(String s)` → `(Ljava/lang/String;)Ljava/lang/String;`

### What's the difference between Inject and Invoke?

- **Inject**: Insert your code at a specific point in a method
- **Invoke**: Intercept a method call within a method and possibly modify arguments

### Can I cancel a method execution?

Yes, set `ci.cancelled = true` in your hook method. However, this only works for certain transformation types.

### How do I modify method arguments?

Use `CallbackInfo.modifyArgs`:
```java
ci.modifyArgs = new Object[]{ modifiedArg1, modifiedArg2 };
```

### Can I access static fields from hook methods?

Yes, you can reference static fields from your hook class:
```java
@Inject(...)
public static CallbackInfo hook() {
    // Access static fields
    if (cacheEnabled) {
        // ...
    }
}
```

## Performance Questions

### What's the overhead of using bytekin?

- **Transformation time**: Minimal, happens once at class load
- **Runtime overhead**: Zero! Transformed bytecode runs at same speed as hand-written code

### Should I rebuild transformers for each transform?

No! Build once and reuse:
```java
// Good
BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class).build();
for (String className : classNames) {
    byte[] transformed = transformer.transform(className, bytecode);
}

// Bad
for (String className : classNames) {
    BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class).build();
    byte[] transformed = transformer.transform(className, bytecode);
}
```

### How much does bytecode transformation impact startup time?

Impact is minimal when transformations are simple and applied only to necessary classes.

## Troubleshooting Questions

### My transformations aren't being applied

Common causes:
1. **Wrong class name**: Check the `@ModifyClass` value matches exactly
2. **Wrong method descriptor**: Verify the `methodDesc` parameter
3. **Class not loaded**: Ensure the class is loaded before transformation

### I'm getting ClassCastException

This usually means:
1. Type mismatch in `CallbackInfo.returnValue`
2. Wrong type in hook method signature
3. Modifying arguments to incompatible types

### Hook method is not being called

Check:
1. Is the hook class passed to the Builder?
2. Are method name and descriptor correct?
3. Is the target class name correct?

### java.lang.VerifyError

This means the transformed bytecode is invalid. Common causes:
1. Incorrect bytecode modification
2. Type mismatches
3. Invalid method signatures

### Performance degradation after transformation

If transformations are slow:
1. Simplify hook methods
2. Avoid expensive operations in hooks
3. Use conditional logic to skip unnecessary work
4. Profile with a JVM profiler

## Advanced Questions

### Can I create custom transformers?

Yes! You can extend the transformer classes or use the programmatic API instead of annotations.

### Does bytekin support method overloading?

Yes, by using the complete method descriptor which includes parameter types and return type.

### Can I transform the same class multiple times?

Yes, you can apply different transformations sequentially.

### Is bytekin thread-safe?

After building, `BytekinTransformer.transform()` is thread-safe and can be called from multiple threads concurrently.

### Can I use bytekin with Java agents?

Yes! bytekin works well with Java agents. Use it within your agent's `transform()` method.

## Migration and Upgrade Questions

### How do I migrate from another bytecode tool?

The concepts are similar:
1. Define target classes
2. Create hook methods with transformation annotations
3. Build transformers
4. Apply transformations

### Can I upgrade bytekin without changing my code?

Yes, bytekin maintains backward compatibility. Always check release notes before upgrading.

## License and Legal Questions

### What license is bytekin under?

bytekin is licensed under the **Apache License 2.0**.

### Can I use bytekin in commercial projects?

Yes! Apache 2.0 allows commercial use.

### Do I need to open-source my code if I use bytekin?

No, Apache 2.0 does not require you to open-source your code. Just include the license notice.

## Community Questions

### How do I report bugs?

Report bugs on the [GitHub Issues](https://github.com/brqnko/bytekin/issues) page.

### How can I contribute?

Contributions are welcome! See the GitHub repository for contribution guidelines.

### Where can I get help?

- Check the [documentation](./introduction.md)
- Search [GitHub Issues](https://github.com/brqnko/bytekin/issues)
- Review [Examples](./examples.md)

## Still Have Questions?

If your question isn't answered here:
1. Check the [API Reference](./api-reference.md)
2. Review [Best Practices](./best-practices.md)
3. Look at [Examples](./examples.md)
4. Open an issue on GitHub

## Next Steps

- Explore [Examples](./examples.md)
- Review [Best Practices](./best-practices.md)
- Check [Troubleshooting](./troubleshooting.md)
