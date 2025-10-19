# Troubleshooting Guide

This guide helps you resolve common issues when using bytekin.

## Transformation Not Applied

### Symptoms

- Hook methods are never called
- Original code runs without modifications
- Breakpoints in hooks are never hit

### Causes and Solutions

#### 1. Incorrect Class Name

The `@ModifyClass` value must exactly match the bytecode class name.

**Problem:**
```java
@ModifyClass("Calculator")  // Wrong!
public class CalcHooks { }
```

**Solution:**
```java
@ModifyClass("com.example.Calculator")  // Correct
public class CalcHooks { }
```

**How to verify:**
```bash
# List all classes in JAR
jar tf myapp.jar | grep -i calculator
```

#### 2. Wrong Method Descriptor

The `methodDesc` must exactly match the method signature in bytecode.

**Problem:**
```java
// Method in bytecode: public int add(int a, int b)
@Inject(methodName = "add", methodDesc = "(int, int)int", at = At.HEAD)  // Wrong!
public static CallbackInfo hook() { }
```

**Solution:**
```java
@Inject(methodName = "add", methodDesc = "(II)I", at = At.HEAD)  // Correct
public static CallbackInfo hook() { }
```

**How to find correct descriptor:**
```bash
# Use javap to see method signatures
javap -c com.example.Calculator | grep -A 5 "public int add"
```

#### 3. Hook Class Not Passed to Builder

The hook class must be passed to the Builder.

**Problem:**
```java
BytekinTransformer transformer = new BytekinTransformer.Builder()
    .build();  // Where are the hooks?
```

**Solution:**
```java
BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class)
    .build();  // Pass hook class
```

#### 4. Class Not Yet Loaded

Transformations must be applied before the class is loaded by the JVM.

**Problem:**
```java
// Class already loaded
Class<?> clazz = Class.forName("com.example.MyClass");

// Now trying to transform - too late!
byte[] transformed = transformer.transform("com.example.MyClass", bytecode);
```

**Solution:**
- Use a custom `ClassLoader` that applies transformations during loading
- Or use Java instrumentation/agents to intercept class loading

## Type Mismatch Errors

### Symptoms

- `java.lang.ClassCastException`
- Wrong values returned from methods
- Type incompatibility errors

### Common Causes

#### 1. Wrong Return Type in CallbackInfo

**Problem:**
```java
@Inject(methodName = "getCount", methodDesc = "()I", at = At.HEAD)
public static CallbackInfo wrongReturn() {
    CallbackInfo ci = new CallbackInfo();
    ci.cancelled = true;
    ci.returnValue = "42";  // String instead of int!
    return ci;
}
```

**Solution:**
```java
@Inject(methodName = "getCount", methodDesc = "()I", at = At.HEAD)
public static CallbackInfo correctReturn() {
    CallbackInfo ci = new CallbackInfo();
    ci.cancelled = true;
    ci.returnValue = 42;  // Correct: int
    return ci;
}
```

#### 2. Wrong Parameter Types in Hook Method

**Problem:**
```java
// Target method: void process(int count, String name)
@Inject(methodName = "process", methodDesc = "(ILjava/lang/String;)V", at = At.HEAD)
public static CallbackInfo wrongParams(String name, int count) {  // Reversed!
    return CallbackInfo.empty();
}
```

**Solution:**
```java
@Inject(methodName = "process", methodDesc = "(ILjava/lang/String;)V", at = At.HEAD)
public static CallbackInfo correctParams(int count, String name) {  // Correct order
    return CallbackInfo.empty();
}
```

#### 3. Modifying Arguments to Wrong Type

**Problem:**
```java
@Invoke(..., shift = Shift.BEFORE)
public static CallbackInfo wrongArgType() {
    CallbackInfo ci = new CallbackInfo();
    ci.modifyArgs = new Object[]{"100"};  // String instead of int
    return ci;
}
```

**Solution:**
```java
@Invoke(..., shift = Shift.BEFORE)
public static CallbackInfo correctArgType() {
    CallbackInfo ci = new CallbackInfo();
    ci.modifyArgs = new Object[]{100};  // Correct: int
    return ci;
}
```

## Null Pointer Exceptions

### Symptoms

- NPE during transformation
- NPE when calling transformed methods
- Stack trace originates from bytecode

### Causes and Solutions

#### 1. Returning null from Injection

**Problem:**
```java
@Inject(methodName = "getValue", methodDesc = "()Ljava/lang/String;", at = At.HEAD)
public static CallbackInfo returnNull() {
    CallbackInfo ci = new CallbackInfo();
    ci.cancelled = true;
    ci.returnValue = null;  // Valid for objects, but may not be expected
    return ci;
}
```

**Solution:**
- Document that null can be returned
- Or return a default value instead:
```java
ci.returnValue = "";  // Empty string instead of null
```

#### 2. Accessing null Parameters in Hooks

**Problem:**
```java
@Inject(methodName = "process", methodDesc = "(Ljava/lang/String;)V", at = At.HEAD)
public static CallbackInfo unsafeAccess(String input) {
    System.out.println(input.length());  // NPE if input is null!
    return CallbackInfo.empty();
}
```

**Solution:**
```java
@Inject(methodName = "process", methodDesc = "(Ljava/lang/String;)V", at = At.HEAD)
public static CallbackInfo safeAccess(String input) {
    if (input != null) {
        System.out.println(input.length());
    }
    return CallbackInfo.empty();
}
```

## Performance Issues

### Symptoms

- Application startup is slow
- Memory usage is high
- Response times are degraded

### Causes and Solutions

#### 1. Complex Hook Methods

**Problem:**
```java
@Inject(methodName = "process", methodDesc = "()V", at = At.HEAD)
public static CallbackInfo slowHook() {
    // Database queries
    List<Item> items = database.queryAll();
    // File I/O
    Files.write(Paths.get("log.txt"), data);
    // Expensive computations
    // ...
    return CallbackInfo.empty();
}
```

**Solution:**
- Keep hooks simple and fast
- Defer expensive work to background threads
- Use lazy initialization for resources

#### 2. Rebuilding Transformers Repeatedly

**Problem:**
```java
for (String className : classNames) {
    // Creating new transformer for each class!
    BytekinTransformer transformer = new BytekinTransformer.Builder(Hooks.class)
        .build();
    transformer.transform(className, bytecode);
}
```

**Solution:**
```java
// Build once, reuse many times
BytekinTransformer transformer = new BytekinTransformer.Builder(Hooks.class)
    .build();

for (String className : classNames) {
    transformer.transform(className, bytecode);
}
```

#### 3. Transforming Unnecessary Classes

**Problem:**
```java
// Applying transformation to all classes, even ones that don't need it
for (String className : allClasses) {
    byte[] transformed = transformer.transform(className, bytecode);
}
```

**Solution:**
- Transform only specific classes that need it
- Use filtering/naming patterns
- Profile to identify hotspots

## Bytecode Verification Errors

### Symptoms

- `java.lang.VerifyError` when loading class
- "Illegal type at offset X" errors
- Stack trace is hard to interpret

### Common Causes

#### 1. Invalid Bytecode Modifications

This usually means the transformation created invalid bytecode.

**How to Debug:**
1. Use `javap` to inspect the transformed bytecode
2. Look for unusual instruction sequences
3. Verify return types match

#### 2. Incorrect Method Descriptors

An incorrect descriptor can cause verification failures.

**Solution:**
- Double-check all method descriptors
- Use online descriptor converters to verify
- Compare with `javap` output

## Methods Not Found

### Symptoms

- Specific methods aren't being transformed
- Overloaded methods cause issues
- Constructor transformations fail

### Causes and Solutions

#### 1. Overloaded Methods

Overloaded methods must be distinguished by their full descriptor.

**Problem:**
```java
// Class has multiple add() methods
// add(int, int) and add(double, double)

@Inject(methodName = "add", methodDesc = "(II)I", at = At.HEAD)  // Only matches int version
public static CallbackInfo hook() { }
```

**Solution:**
- Use complete descriptor with parameter and return types
- The descriptor automatically distinguishes overloads

#### 2. Private or Internal Methods

Some private methods might not be accessible.

**Problem:**
```java
@Inject(methodName = "internalMethod", methodDesc = "()V", at = At.HEAD)  // Private method
public static CallbackInfo hook() { }
```

**Solution:**
- Verify the method is not synthetic or bridge method
- Check that method name and descriptor are exactly correct

## Cannot Load Transformed Classes

### Symptoms

- ClassNotFoundException after transformation
- Class appears to be missing
- Custom ClassLoader issues

### Causes and Solutions

#### 1. Incorrect ClassLoader Setup

**Problem:**
```java
// Trying to use transformed bytecode with default classloader
byte[] transformed = transformer.transform("com.example.MyClass", bytecode);
Class<?> clazz = Class.forName("com.example.MyClass");  // Won't use transformed bytecode!
```

**Solution:**
- Create custom ClassLoader to use transformed bytecode
- Or use instrumentation/agents to intercept loading

#### 2. Bytecode Corruption

The transformation might have produced invalid bytecode.

**Solution:**
- Verify transformation didn't corrupt bytecode
- Check bytecode size/integrity
- Use bytecode inspection tools

## Debugging Tips

### 1. Enable Verbose Output

```java
// Add debug logging in hooks
@Inject(methodName = "process", methodDesc = "()V", at = At.HEAD)
public static CallbackInfo debug() {
    System.out.println("[DEBUG] Hook executed");
    System.out.println("[DEBUG] Stack: " + Arrays.toString(Thread.currentThread().getStackTrace()));
    return CallbackInfo.empty();
}
```

### 2. Inspect Bytecode

```bash
# View transformed bytecode
javap -c -private TransformedClass.class

# Look for your injected calls
```

### 3. Use a Bytecode Viewer

Tools like Bytecode Viewer or IDEA plugins help visualize bytecode.

### 4. Profile Performance

```bash
# Use JProfiler or YourKit to identify bottlenecks
# Monitor memory usage and CPU time
```

## Common Questions

**Q: Can I transform bootstrap classes?**
A: Not easily with standard classloaders. Use Java agents with instrumentation API.

**Q: Do transformations affect serialization?**
A: Transformed classes will have different bytecode but same serialization format if you don't change fields.

**Q: Can I use bytekin in Spring Boot?**
A: Yes, but you need to configure custom class loading or use agents.

## Getting Help

1. Check this troubleshooting guide
2. Review [Best Practices](./best-practices.md)
3. Look at [Examples](./examples.md)
4. Open an issue on [GitHub](https://github.com/brqnko/bytekin/issues)

## Next Steps

- Review [Best Practices](./best-practices.md)
- Check [Examples](./examples.md)
- Report issues on GitHub
