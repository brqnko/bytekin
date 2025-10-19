# bytekin Documentation

Welcome to the comprehensive documentation for **bytekin** - a lightweight Java bytecode transformation framework.

## 📚 Documentation Overview

This documentation is organized into several sections to help you get started and master bytekin:

### Getting Started
- [Introduction](./introduction.md) - Overview of bytekin and its capabilities
- [Installation](./installation.md) - How to add bytekin to your project
- [Your First Transformation](./first-transformation.md) - Step-by-step guide to your first bytecode transformation

### Understanding bytekin
- [Core Concepts](./core-concepts.md) - Fundamental concepts behind bytecode transformation
- [Bytecode Basics](./bytecode-basics.md) - Understanding Java bytecode
- [How bytekin Works](./how-it-works.md) - Internal architecture and mechanisms

### Features
- [Features Overview](./features.md) - All available transformation features
- [Inject Transformation](./inject.md) - Insert code at method points
- [Invoke Transformation](./invoke.md) - Intercept method calls
- [Redirect Transformation](./redirect.md) - Change method call targets
- [Constant Modification](./constant-modification.md) - Modify hardcoded values
- [Variable Modification](./variable-modification.md) - Transform local variables

### Advanced Topics
- [Advanced Usage](./advanced-usage.md) - Advanced patterns and techniques
- [Mappings](./mappings.md) - Handle obfuscated code with name mappings
- [Builder Pattern](./builder-pattern.md) - Fluent API for configuration
- [Custom Transformers](./custom-transformers.md) - Create custom transformers

### API Reference
- [API Reference](./api-reference.md) - Complete API documentation
- [Annotations](./annotations.md) - Annotation reference guide
- [Classes and Interfaces](./classes-interfaces.md) - Class documentation

### Examples and Guides
- [Basic Examples](./examples-basic.md) - Practical code examples
- [Advanced Examples](./examples-advanced.md) - Advanced use cases and patterns
- [Best Practices](./best-practices.md) - Recommended practices and patterns
- [FAQ](./faq.md) - Frequently asked questions
- [Troubleshooting](./troubleshooting.md) - Solutions to common issues

## 🚀 Quick Start

### 1. Installation

```xml
<!-- Maven -->
<dependency>
    <groupId>io.github.brqnko.bytekin</groupId>
    <artifactId>bytekin</artifactId>
    <version>1.0</version>
</dependency>
```

```gradle
// Gradle
implementation 'io.github.brqnko.bytekin:bytekin:1.0'
```

### 2. Create Hook Class

```java
@ModifyClass("com.example.Calculator")
public class CalculatorHooks {
    @Inject(methodName = "add", methodDesc = "(II)I", at = At.HEAD)
    public static CallbackInfo logAdd(int a, int b) {
        System.out.println("Adding: " + a + " + " + b);
        return CallbackInfo.empty();
    }
}
```

### 3. Build Transformer

```java
BytekinTransformer transformer = new BytekinTransformer.Builder(CalculatorHooks.class)
    .build();
```

### 4. Transform Bytecode

```java
byte[] original = getClassBytecode("com.example.Calculator");
byte[] transformed = transformer.transform("com.example.Calculator", original);
```

## 📖 Reading Guide

### I want to...

**Understand what bytekin is**
→ Start with [Introduction](./introduction.md)

**Get started immediately**
→ Follow [Installation](./installation.md) and [Your First Transformation](./first-transformation.md)

**Learn how bytecode transformation works**
→ Read [Core Concepts](./core-concepts.md) and [How bytekin Works](./how-it-works.md)

**Inject code into methods**
→ See [Inject Transformation](./inject.md) and [Basic Examples](./examples-basic.md)

**Intercept method calls**
→ Check [Invoke Transformation](./invoke.md)

**Handle obfuscated code**
→ Learn about [Mappings](./mappings.md)

**Solve a specific problem**
→ Browse [FAQ](./faq.md) and [Troubleshooting](./troubleshooting.md)

**See advanced patterns**
→ Review [Advanced Usage](./advanced-usage.md) and [Advanced Examples](./examples-advanced.md)

**Write better code**
→ Follow [Best Practices](./best-practices.md)

## 🎯 Key Concepts

### Method Descriptors
JVM method signatures use a special format:
- `(II)I` - takes two ints, returns int
- `(Ljava/lang/String;)V` - takes String, returns void

Learn more in [Bytecode Basics](./bytecode-basics.md)

### Hook Classes
Classes annotated with `@ModifyClass` that define transformations:
```java
@ModifyClass("target.ClassName")
public class MyHooks { }
```

### CallbackInfo
Controls transformation behavior:
```java
public class CallbackInfo {
    public boolean cancelled;      // Skip execution?
    public Object returnValue;     // Custom return?
    public Object[] modifyArgs;    // Modified args?
}
```

### Transformation Types
1. **Inject** - Insert code at specific points
2. **Invoke** - Intercept method calls
3. **Redirect** - Change call targets
4. **Constant Modification** - Modify hardcoded values
5. **Variable Modification** - Transform variables

## 📋 Common Tasks

### Add Logging
[Inject Transformation](./inject.md) → [Basic Examples](./examples-basic.md#example-1-adding-logging)

### Validate Parameters
[Inject Transformation](./inject.md) → [Basic Examples](./examples-basic.md#example-2-parameter-validation)

### Implement Caching
[Invoke Transformation](./invoke.md) → [Basic Examples](./examples-basic.md#example-3-caching)

### Add Security Checks
[Inject Transformation](./inject.md) → [Best Practices](./best-practices.md#security)

### Performance Profiling
[Inject Transformation](./inject.md) → [Advanced Examples](./examples-advanced.md#example-7-performance-profiling)

### Handle Obfuscated Code
[Mappings](./mappings.md)

### Create Custom Transformers
[Custom Transformers](./custom-transformers.md)

## ❓ Need Help?

1. **Check the FAQ** - [FAQ](./faq.md)
2. **Search Troubleshooting** - [Troubleshooting](./troubleshooting.md)
3. **Review Examples** - [Basic](./examples-basic.md) and [Advanced](./examples-advanced.md)
4. **Read Best Practices** - [Best Practices](./best-practices.md)
5. **Check API Reference** - [API Reference](./api-reference.md)

## 🔗 Navigation

- [Introduction](./introduction.md) - Start here
- [Getting Started](./getting-started.md) - Installation and setup
- [Core Concepts](./core-concepts.md) - Fundamental understanding
- [Features](./features.md) - All capabilities
- [Advanced Usage](./advanced-usage.md) - Patterns and techniques
- [Examples](./examples-basic.md) - Code samples
- [Best Practices](./best-practices.md) - Recommendations
- [API Reference](./api-reference.md) - Complete API
- [FAQ](./faq.md) - Questions and answers
- [Troubleshooting](./troubleshooting.md) - Problem solving

## 📝 Documentation Status

This documentation covers bytekin version **1.0** and includes:

- ✅ Complete feature documentation
- ✅ API reference
- ✅ Multiple examples
- ✅ Best practices guide
- ✅ Troubleshooting guide
- ✅ FAQ section
- ✅ Advanced patterns

## 🤝 Contributing

Found an error or want to improve the documentation?
- Report issues on [GitHub](https://github.com/brqnko/bytekin)
- Contribute improvements via pull requests

## 📄 License

bytekin is licensed under the Apache License 2.0.
See [LICENSE](LICENSE) for details.

## 🎉 Ready to Get Started?

→ [Begin with Installation](./installation.md)

→ [Build Your First Transformation](./first-transformation.md)

→ [Explore All Features](./features.md)
