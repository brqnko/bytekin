# Builder Pattern

bytekin provides a fluent Builder API for constructing transformers programmatically.

## Basic Usage

```java
BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class)
    .build();
```

## Using Mappings

Apply name mappings during builder construction:

```java
BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class)
    .mapping(new CustomMappingProvider())
    .build();
```

## Adding Programmatic Transformations

Mix annotation-based and programmatic configurations:

```java
BytekinTransformer transformer = new BytekinTransformer.Builder(AnnotationHooks.class)
    .inject("com.example.Extra", new Injection(...))
    .invoke("com.example.Another", new Invocation(...))
    .build();
```

## Multiple Hook Classes

```java
BytekinTransformer transformer = new BytekinTransformer.Builder(
    LoggingHooks.class,
    SecurityHooks.class,
    PerformanceHooks.class
)
.mapping(myMappings)
.build();
```

## Builder Methods

### mapping(IMappingProvider)

Set a mapping provider for class/method name translation.

### inject(String, Injection)

Add injection transformation programmatically.

### invoke(String, Invocation)

Add invocation transformation programmatically.

### redirect(String, RedirectData)

Add redirect transformation programmatically.

### modifyConstant(String, ConstantModification)

Add constant modification programmatically.

### modifyVariable(String, VariableModification)

Add variable modification programmatically.

### build()

Build and return the transformer. This method:
1. Scans all hook classes
2. Extracts annotations
3. Adds programmatic transformations
4. Creates internal transformer map
5. Returns ready-to-use transformer

## Best Practices

1. **Build once**: Create transformers during initialization
2. **Reuse**: Use the same transformer for multiple transformations
3. **Combine patterns**: Mix annotations and programmatic API
4. **Document configuration**: Comment why specific transformations are applied

## Performance Tips

```java
// Good: Build once
BytekinTransformer transformer = new BytekinTransformer.Builder(Hooks.class).build();

for (String className : classes) {
    byte[] transformed = transformer.transform(className, bytecode);
}

// Bad: Building multiple times
for (String className : classes) {
    BytekinTransformer transformer = new BytekinTransformer.Builder(Hooks.class).build();
    byte[] transformed = transformer.transform(className, bytecode);
}
```

## Next Steps

- Learn [Advanced Usage](./advanced-usage.md)
- Review [Best Practices](./best-practices.md)
