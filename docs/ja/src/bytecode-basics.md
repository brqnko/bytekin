# Bytecode Basics

## What is Java Bytecode?

Java source code (`.java` files) is compiled into Java bytecode (contained in `.class` files). This bytecode is a platform-independent intermediate language that the Java Virtual Machine (JVM) understands.

### Source Code vs Bytecode

**Java Source Code:**
```java
public class Example {
    public void greet(String name) {
        System.out.println("Hello, " + name);
    }
}
```

**Compiled Bytecode (Conceptual):**
```
aload_0
aload_1
invokedynamic <concat>
getstatic System.out
swap
invokevirtual println
return
```

The bytecode version is more verbose and operating-system independent.

## Why is Bytecode Important?

1. **Platform Independence**: Works on any system with a JVM
2. **Runtime Flexibility**: Can be transformed before loading
3. **Security**: JVM can verify bytecode correctness
4. **Optimization**: JVM can JIT-compile to native code
5. **Introspection**: Tools can analyze bytecode without source code

## Reading Bytecode

### Tools for Inspection

- **javap**: Built-in Java disassembler
- **Bytecode Viewer**: GUI tool for inspecting bytecode
- **ASM Tree View**: Plugin for IDE visualization

### Example: Using javap

```bash
javap -c Example.class
```

Output shows the bytecode instructions for each method.

## Common Bytecode Instructions

Some frequently encountered bytecode instructions:

| Instruction | Purpose |
|-----------|---------|
| `aload` | Load object reference |
| `iload` | Load integer |
| `invoke*` | Call methods (static, virtual, etc) |
| `return` | Return from method |
| `getstatic` | Read static field |
| `putstatic` | Write static field |
| `new` | Create new object |

## Method Descriptors (Signatures)

Bytecode uses a special notation for method signatures:

```
(ParameterTypes)ReturnType
```

### Primitive Types

- `Z` - boolean
- `B` - byte
- `C` - char
- `S` - short
- `I` - int
- `J` - long
- `F` - float
- `D` - double
- `V` - void

### Object Types

- `Ljava/lang/String;` - String class
- `L...classname...;` - Any class

### Array Types

- `[I` - int array
- `[Ljava/lang/String;` - String array

### Examples

- `(II)I` - Adds two integers: `int method(int a, int b) { return ...; }`
- `(Ljava/lang/String;)V` - Takes string, returns nothing: `void method(String s) { ... }`
- `()Ljava/lang/String;` - No parameters, returns string: `String method() { ... }`
- `([Ljava/lang/String;)V` - Takes string array: `void method(String[] args) { ... }`

## Class References

Classes are referenced using their fully qualified names with `/` separators:

- `java/lang/String`
- `java/util/ArrayList`
- `com/mycompany/MyClass`

In bytekin, we typically use the standard Java notation with dots:

- `java.lang.String`
- `java.util.ArrayList`
- `com.mycompany.MyClass`

## The Class File Format

A compiled `.class` file contains:

1. **Magic Number**: Identifies it as a class file (`0xCAFEBABE`)
2. **Version**: Java version information
3. **Constant Pool**: Strings, method names, field names, type information
4. **Access Flags**: public, final, abstract, etc.
5. **This Class**: Class name
6. **Super Class**: Parent class
7. **Interfaces**: Implemented interfaces
8. **Fields**: Class member variables
9. **Methods**: Methods with their bytecode
10. **Attributes**: Additional metadata

## Important Notes

- Bytecode is **not** human-readable but is **systematic and analyzable**
- **Every Java source construct** maps to bytecode
- **Bytecode is verifiable** - JVM checks correctness before execution
- **Bytecode can be manipulated** programmatically without source code

## Next Steps

- Learn how [bytekin Works](./how-it-works.md)
- Understand [Core Concepts](./core-concepts.md)
