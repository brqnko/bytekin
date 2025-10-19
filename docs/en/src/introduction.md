# Introduction to bytekin

## What is bytekin?

**bytekin** is a lightweight, easy-to-use Java bytecode transformation framework built on top of [ASM](https://asm.ow2.io/). It allows developers to programmatically modify Java classes at the bytecode level, enabling powerful code injection, method interception, and transformation capabilities.

bytekin is designed to be:

- **Simple**: Intuitive annotation-based API
- **Lightweight**: Minimal dependencies (only ASM)
- **Flexible**: Multiple transformation strategies
- **Powerful**: Support for complex bytecode manipulations

## Key Features

- **Inject**: Insert custom code at specific points in methods (head, return, etc.)
- **Invoke**: Intercept method calls and modify their behavior
- **Redirect**: Change method call targets
- **Constant Modification**: Modify constant values in bytecode
- **Variable Modification**: Manipulate local variables
- **Mapping Support**: Transform class and method names automatically
- **Builder Pattern**: Fluent API for constructing transformers

## Use Cases

bytekin is ideal for scenarios where you need to:

1. **Add logging or monitoring** to existing classes without modifying source code
2. **Implement AOP (Aspect-Oriented Programming)** without framework overhead
3. **Add security checks** at runtime
4. **Modify third-party library behavior** at the bytecode level
5. **Implement mocking or stubbing** for testing
6. **Instrument code** for profiling or analytics
7. **Apply cross-cutting concerns** to multiple classes

## Why bytekin?

Unlike full-featured frameworks, bytekin is:

- **Minimal**: Only depends on ASM, no heavy dependencies
- **Direct**: Works with bytecode directly without reflection overhead
- **Flexible**: Supports both annotation-based and programmatic approaches
- **Fast**: Efficient bytecode manipulation at JVM load time

## Project Structure

The bytekin project is organized into several modules:

- **Transformation Engine**: Core bytecode manipulation logic
- **Injection System**: Method injection capabilities
- **Mapping System**: Class and method name mapping support
- **Utilities**: Helper classes for bytecode manipulation

## Next Steps

- [Get Started](./getting-started.md) with your first transformation
- [Learn the Core Concepts](./core-concepts.md) behind bytecode manipulation
- [Explore All Features](./features.md) available in bytekin
