# Classes and Interfaces

Reference documentation for bytekin classes and interfaces.

## Core Classes

### BytekinTransformer

Main transformer class for bytecode manipulation.

**Methods:**
- `byte[] transform(String className, byte[] bytes, int api)` - Transform class bytecode
- `byte[] transform(String className, byte[] bytes)` - Transform (default API)

**Builder:**
- `new BytekinTransformer.Builder(Class<?>... classes)` - Create builder

### CallbackInfo

Data structure for controlling transformation behavior.

**Fields:**
- `boolean cancelled` - Skip original code execution
- `Object returnValue` - Custom return value
- `Object[] modifyArgs` - Modified method arguments

**Methods:**
- `static CallbackInfo empty()` - Create empty callback
- `CallbackInfo(boolean cancelled, Object returnValue, Object[] modifyArgs)` - Constructor

## Builder Class

### BytekinTransformer.Builder

Fluent builder for constructing transformers.

**Constructors:**
- `Builder(Class<?>... classes)` - Initialize with hook classes

**Methods:**
- `Builder mapping(IMappingProvider)` - Set mapping provider
- `Builder inject(String, Injection)` - Add injection
- `Builder invoke(String, Invocation)` - Add invocation
- `Builder redirect(String, RedirectData)` - Add redirect
- `Builder modifyConstant(String, ConstantModification)` - Add constant modification
- `Builder modifyVariable(String, VariableModification)` - Add variable modification
- `BytekinTransformer build()` - Build transformer

## Data Classes

### Injection

Represents an injection point.

**Purpose:** Store injection configuration data.

### Invocation

Represents an invocation point.

**Purpose:** Store invocation configuration data.

### RedirectData

Represents a redirect target.

**Purpose:** Store redirect configuration data.

### ConstantModification

Represents a constant modification.

**Purpose:** Store constant modification data.

### VariableModification

Represents a variable modification.

**Purpose:** Store variable modification data.

## Interfaces

### IMappingProvider

Interface for name mapping.

**Methods:**
- `String getClassName(String name)` - Map class name
- `String getMethodName(String className, String methodName, String descriptor)` - Map method name
- `String getFieldName(String className, String fieldName)` - Map field name

### Implementation Examples

**EmptyMappingProvider** - No-op mapping (returns unchanged names)

**Custom Mapping:**
```java
public class CustomMapping implements IMappingProvider {
    @Override
    public String getClassName(String name) {
        // Custom mapping logic
        return name;
    }
    
    @Override
    public String getMethodName(String className, String methodName, String descriptor) {
        // Custom mapping logic
        return methodName;
    }
    
    @Override
    public String getFieldName(String className, String fieldName) {
        // Custom mapping logic
        return fieldName;
    }
}
```

## Utility Classes

### DescriptorParser

Parse and validate method descriptors.

**Methods:**
- `static String parseDescriptor(String desc)` - Parse descriptor format

### BytecodeManipulator

Low-level bytecode utilities.

**Purpose:** Internal utilities for bytecode manipulation.

## Inheritance Hierarchy

```
Object
├── BytekinTransformer
│   └── BytekinTransformer.Builder
├── CallbackInfo
├── Injection
├── Invocation
├── RedirectData
├── ConstantModification
└── VariableModification
```

## Interface Implementations

```
IMappingProvider
├── EmptyMappingProvider
└── (Custom implementations)
```

## Next Steps

- Review [API Reference](./api-reference.md)
- Check [Annotations](./annotations.md)
- Explore [Examples](./examples.md)
