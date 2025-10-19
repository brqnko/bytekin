# API Reference

This section provides detailed API documentation for bytekin.

## Core Classes

### BytekinTransformer

The main entry point for bytecode transformation.

```java
public class BytekinTransformer {
    public byte[] transform(String className, byte[] bytes, int api);
    
    public static class Builder {
        public Builder(Class<?>... classes);
        public Builder mapping(IMappingProvider mapping);
        public Builder inject(String className, Injection injection);
        public Builder invoke(String className, Invocation invocation);
        public Builder redirect(String className, RedirectData redirect);
        public Builder modifyConstant(String className, ConstantModification modification);
        public Builder modifyVariable(String className, VariableModification modification);
        public BytekinTransformer build();
    }
}
```

### CallbackInfo

Controls transformation behavior within hook methods.

```java
public class CallbackInfo {
    public boolean cancelled;
    public Object returnValue;
    public Object[] modifyArgs;
    
    public static CallbackInfo empty();
}
```

## Annotations

### @ModifyClass

Marks a class as a hook container for bytecode transformations.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModifyClass {
    String className();
}
```

### @Inject

Injects code at specific points in methods.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
    String methodName();
    String methodDesc();
    At at();
}
```

### @Invoke

Intercepts method calls.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Invoke {
    String targetMethodName();
    String targetMethodDesc();
    String invokeMethodName();
    String invokeMethodDesc();
    Shift shift();
}
```

### @Redirect

Redirects method calls to different target.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Redirect {
    String targetMethodName();
    String targetMethodDesc();
    String redirectMethodName();
    String redirectMethodDesc();
}
```

### @ModifyConstant

Modifies constant values in bytecode.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModifyConstant {
    String methodName();
    Object oldValue();
    Object newValue();
}
```

### @ModifyVariable

Modifies local variable values.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModifyVariable {
    String methodName();
    int variableIndex();
}
```

## Enums

### At

Specifies where to inject code.

```java
public enum At {
    HEAD,      // Before method body
    RETURN,    // Before return statements
    TAIL       // At method end
}
```

### Shift

Specifies timing relative to method invocation.

```java
public enum Shift {
    BEFORE,    // Before the call
    AFTER      // After the call
}
```

## Interfaces

### IMappingProvider

Maps class and method names.

```java
public interface IMappingProvider {
    String getClassName(String name);
    String getMethodName(String className, String methodName, String descriptor);
    String getFieldName(String className, String fieldName);
}
```

## Data Classes

### Injection

Represents an injection transformation.

```java
public class Injection {
    // Constructor and methods
}
```

### Invocation

Represents an invocation transformation.

```java
public class Invocation {
    // Constructor and methods
}
```

### RedirectData

Represents a redirect transformation.

```java
public class RedirectData {
    // Constructor and methods
}
```

### ConstantModification

Represents a constant modification transformation.

```java
public class ConstantModification {
    // Constructor and methods
}
```

### VariableModification

Represents a variable modification transformation.

```java
public class VariableModification {
    // Constructor and methods
}
```

## Common Exceptions

### VerifyError

Thrown when transformed bytecode is invalid.

### ClassNotFoundException

Thrown when target class cannot be found.

### ClassFormatException

Thrown when bytecode format is invalid.

## Utility Classes

### DescriptorParser

Utility for parsing and validating method descriptors.

### BytecodeManipulator

Low-level bytecode manipulation utilities.

## Threading

All public methods are **thread-safe after initialization**:

- `BytekinTransformer.transform()` can be called from multiple threads
- `Builder` is **not** thread-safe during configuration
- `CallbackInfo` is local to each hook invocation

## Performance Characteristics

| Operation | Complexity |
|-----------|-----------|
| Builder.build() | O(n) where n = number of hook methods |
| transform() | O(m) where m = bytecode size |
| Hook execution | O(1) average case |

## Next Steps

- Review [Annotations](./annotations.md) in detail
- Check [Classes and Interfaces](./classes-interfaces.md)
- Explore [Examples](./examples.md)
