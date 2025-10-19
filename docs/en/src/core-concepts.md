# Core Concepts

This section explains the fundamental concepts behind bytekin and bytecode transformation.

## What is Bytecode?

Java source code is compiled into **bytecode** - a platform-independent intermediate representation that runs on the Java Virtual Machine (JVM). Unlike source code, bytecode is not human-readable but is standardized and can be manipulated programmatically.

### Bytecode Structure

Java bytecode consists of:
- **Constant Pool**: String literals, method references, field references
- **Methods**: Compiled methods with instruction sequences
- **Fields**: Class properties
- **Attributes**: Metadata like line numbers, local variables, exceptions

## What is Bytecode Transformation?

**Bytecode transformation** is the process of reading, analyzing, and modifying bytecode after compilation but before class loading. This allows you to alter class behavior without modifying source code.

### Use Cases

1. **Runtime Monitoring**: Add logging without changing source
2. **Cross-Cutting Concerns**: Apply behavior to multiple classes
3. **Testing**: Mock or stub methods
4. **Security**: Inject security checks
5. **Performance**: Add profiling instrumentation

## How bytekin Fits In

bytekin simplifies bytecode transformation by:

1. **Abstracting ASM Complexity**: Provides a clean API over raw ASM
2. **Annotation-Based Configuration**: Use Java annotations to define transformations
3. **Multiple Strategies**: Support for injections, method interception, and redirects
4. **Mapping Support**: Handle obfuscated or renamed classes
5. **Flexible Builder Pattern**: Programmatically compose transformations

## The Transformation Pipeline

```
Target Class Bytecode
        ↓
   BytekinTransformer
        ↓
  Scan Hook Classes
        ↓
  Apply Transformations
        ↓
   Modified Bytecode
```

## Key Components

### Hook Classes

Classes annotated with `@ModifyClass` that define how to transform target classes. They contain methods with transformation annotations.

### Transformers

Objects that apply transformations to bytecode. bytekin provides `BytekinTransformer` which handles the entire transformation process.

### Annotations

Special markers in Java code that define transformation behavior:
- `@ModifyClass`: Mark the target class
- `@Inject`: Insert code at specific points
- `@Invoke`: Intercept method calls
- And more...

### CallbackInfo

A data structure that controls transformation behavior:
- `cancelled`: Whether to skip original code
- `returnValue`: Custom return value
- `modifyArgs`: Modified method arguments

## Important Concepts

### Method Descriptors

Method descriptors describe method signatures in JVM format:
```
(ParameterTypes)ReturnType
```

Examples:
- `(II)I` - Takes two ints, returns int
- `(Ljava/lang/String;)V` - Takes String, returns void
- `()Ljava/lang/String;` - Takes nothing, returns String

### Class Names

In bytekin, class names use dot notation:
- Java notation: `java.lang.String`
- Internal notation: `java/lang/String`
- bytekin uses Java notation

## Next Steps

- [Learn about How bytekin Works](./how-it-works.md)
- [Explore All Features](./features.md)
