# Best Practices

This guide covers best practices for using bytekin effectively and safely.

## Design Principles

### 1. Keep Hooks Simple

Keep hook methods focused and simple:

**Good:**
```java
@Inject(methodName = "process", methodDesc = "()V", at = At.HEAD)
public static CallbackInfo log() {
    System.out.println("Starting process");
    return CallbackInfo.empty();
}
```

**Avoid:**
```java
@Inject(methodName = "process", methodDesc = "()V", at = At.HEAD)
public static CallbackInfo complexLogic() {
    // Multiple database calls
    // Complex calculations
    // File I/O operations
    // This is too much for a hook!
    return CallbackInfo.empty();
}
```

### 2. Extract Complex Logic

Move complex logic to separate methods:

```java
@Inject(methodName = "validate", methodDesc = "(Ljava/lang/String;)Z", at = At.HEAD)
public static CallbackInfo onValidate(String input, CallbackInfo ci) {
    if (!isValidInput(input)) {
        ci.cancelled = true;
        ci.returnValue = false;
    }
    return ci;
}

private static boolean isValidInput(String input) {
    // Complex validation logic here
    return !input.isEmpty() && input.length() < 256;
}
```

## Performance Guidelines

### 1. Minimize Hook Overhead

Hooks are executed frequently. Keep them fast:

**Good:**
```java
@Inject(methodName = "getData", methodDesc = "()Ljava/lang/Object;", at = At.HEAD)
public static CallbackInfo checkCache() {
    if (cacheHit()) {
        // Quick cache lookup
        return new CallbackInfo(true, getFromCache(), null);
    }
    return CallbackInfo.empty();
}
```

**Avoid:**
```java
@Inject(methodName = "getData", methodDesc = "()Ljava/lang/Object;", at = At.HEAD)
public static CallbackInfo expensiveCheck() {
    // Scanning entire database
    List<Item> results = database.queryAll();
    // Processing results
    // ...this is too slow!
    return CallbackInfo.empty();
}
```

### 2. Reuse Builder

Build transformers once and reuse:

**Good:**
```java
// In initialization code
BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class)
    .build();

// Use transformer multiple times
byte[] transformed1 = transformer.transform("com.example.Class1", bytes1);
byte[] transformed2 = transformer.transform("com.example.Class2", bytes2);
```

**Avoid:**
```java
// DON'T do this in a loop!
for (String className : classNames) {
    // Creating transformer for each class is wasteful
    BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class)
        .build();
    byte[] transformed = transformer.transform(className, bytes);
}
```

## Error Handling

### 1. Handle Exceptions in Hooks

Exceptions in hooks can break transformations:

**Good:**
```java
@Inject(methodName = "process", methodDesc = "()V", at = At.HEAD)
public static CallbackInfo safeLogging() {
    try {
        System.out.println("Processing started");
    } catch (Exception e) {
        // Handle gracefully, don't let it propagate
        e.printStackTrace();
    }
    return CallbackInfo.empty();
}
```

**Avoid:**
```java
@Inject(methodName = "process", methodDesc = "()V", at = At.HEAD)
public static CallbackInfo unsafeLogging() {
    // If this throws, it breaks the transformation!
    Path path = Paths.get("/invalid/path");
    Files.writeString(path, "log");
    return CallbackInfo.empty();
}
```

### 2. Validate Return Values

When modifying CallbackInfo, ensure types are correct:

**Good:**
```java
@Inject(methodName = "getValue", methodDesc = "()I", at = At.HEAD)
public static CallbackInfo returnCustomValue() {
    CallbackInfo ci = new CallbackInfo();
    ci.cancelled = true;
    ci.returnValue = 42;  // Integer matches return type
    return ci;
}
```

**Avoid:**
```java
@Inject(methodName = "getValue", methodDesc = "()I", at = At.HEAD)
public static CallbackInfo wrongType() {
    CallbackInfo ci = new CallbackInfo();
    ci.cancelled = true;
    ci.returnValue = "42";  // String doesn't match int return type!
    return ci;
}
```

## Documentation

### 1. Document Transformations

Clearly document what each hook does:

```java
/**
 * Adds authentication check to all data access methods.
 * If user is not authenticated, cancels the method and returns false.
 */
@ModifyClass("com.example.DataStore")
public class DataStoreHooks {

    /**
     * Injects authentication check at the start of read operations.
     * 
     * @param ci Callback info - set cancelled=true if not authenticated
     */
    @Inject(methodName = "read", methodDesc = "()Ljava/lang/Object;", at = At.HEAD)
    public static CallbackInfo ensureAuthenticated(CallbackInfo ci) {
        if (!isAuthenticated()) {
            ci.cancelled = true;
            ci.returnValue = null;
        }
        return ci;
    }
}
```

### 2. Document Parameters

Clearly indicate which parameters correspond to method arguments:

```java
/**
 * Sanitizes user input before processing.
 * 
 * @param userId the user ID (first parameter of target method)
 * @param action the requested action (second parameter)
 */
@Inject(methodName = "execute", methodDesc = "(Ljava/lang/String;Ljava/lang/String;)V", 
        at = At.HEAD)
public static CallbackInfo sanitizeInput(String userId, String action) {
    // userId and action are from the target method's parameters
    return CallbackInfo.empty();
}
```

## Testing

### 1. Test Transformations

Always test your transformations:

```java
public class TransformationTest {
    @Test
    public void testInjectionWorks() {
        BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class)
            .build();
        
        byte[] original = getClassBytecode("com.example.Target");
        byte[] transformed = transformer.transform("com.example.Target", original);
        
        // Load and test transformed class
        Class<?> clazz = loadFromBytecode(transformed);
        Object instance = clazz.newInstance();
        
        // Verify transformation was applied
        assertNotNull(instance);
    }
}
```

### 2. Verify No Regression

Ensure original behavior is preserved:

```java
@Test
public void testOriginalBehaviorPreserved() {
    // Test without transformation
    Calculator calc1 = new Calculator();
    int result1 = calc1.add(3, 4);
    
    // Test with transformation
    byte[] transformed = applyTransformation(Calculator.class);
    Calculator calc2 = loadTransformed(transformed);
    int result2 = calc2.add(3, 4);
    
    // Results should be the same
    assertEquals(result1, result2);
}
```

## Compatibility

### 1. Version Compatibility

Document supported Java versions:

```java
/**
 * These hooks work with Java 8+
 * Uses standard method descriptors compatible across versions
 */
@ModifyClass("com.example.Service")
public class CompatibleHooks {
    // ...
}
```

### 2. Library Compatibility

Check for incompatibilities with other bytecode tools:

```java
// Document conflicts with other bytecode manipulation
// For example: Spring, Mockito, AspectJ, etc.
```

## Security

### 1. Input Validation

Always validate inputs in hooks:

```java
@Inject(methodName = "processFile", methodDesc = "(Ljava/lang/String;)V", 
        at = At.HEAD)
public static CallbackInfo validatePath(String path, CallbackInfo ci) {
    if (path != null && isPathTraversal(path)) {
        // Prevent directory traversal attacks
        ci.cancelled = true;
    }
    return ci;
}

private static boolean isPathTraversal(String path) {
    return path.contains("..") || path.startsWith("/");
}
```

### 2. Avoid Sensitive Data Exposure

Don't log or expose sensitive information:

**Good:**
```java
@Inject(methodName = "login", methodDesc = "(Ljava/lang/String;Ljava/lang/String;)Z", 
        at = At.HEAD)
public static CallbackInfo logAttempt(String user) {
    System.out.println("Login attempt by: " + user);
    return CallbackInfo.empty();
}
```

**Avoid:**
```java
@Inject(methodName = "login", methodDesc = "(Ljava/lang/String;Ljava/lang/String;)Z", 
        at = At.HEAD)
public static CallbackInfo logAttempt(String user, String password) {
    // Don't log passwords!
    System.out.println("Login attempt: " + user + " / " + password);
    return CallbackInfo.empty();
}
```

## Debugging Tips

### 1. Bytecode Inspection

Inspect generated bytecode to verify transformations:

```bash
# Use javap to inspect the transformed class
javap -c TransformedClass.class

# Look for your injected method calls
```

### 2. Add Logging

Use logging to track transformation execution:

```java
@Inject(methodName = "critical", methodDesc = "()V", at = At.HEAD)
public static CallbackInfo logEntry() {
    System.out.println("[DEBUG] Entering critical method");
    System.out.println("[DEBUG] Stack trace: " + Arrays.toString(Thread.currentThread().getStackTrace()));
    return CallbackInfo.empty();
}
```

## Maintenance

### 1. Version Your Hooks

Keep track of hook versions:

```java
/**
 * Transformation hooks for version 2.0
 * 
 * Changes from v1.0:
 * - Added authentication checks
 * - Optimized caching strategy
 * - Fixed null pointer issue in legacy code
 */
@ModifyClass("com.example.Service")
public class ServiceHooksV2 {
    // ...
}
```

### 2. Keep Records

Document why each transformation exists:

```
Transform: Calculator.add() logging
Created: 2025-01-15
Reason: Performance monitoring for debug builds
Status: Active
Notes: Can be removed after profiling phase
```

## Common Pitfalls

### 1. Wrong Method Descriptors

❌ **Wrong:**
```java
@Inject(methodName = "add", methodDesc = "(I I)I", at = At.HEAD)  // Spaces in descriptor!
```

✅ **Right:**
```java
@Inject(methodName = "add", methodDesc = "(II)I", at = At.HEAD)
```

### 2. Type Mismatches

❌ **Wrong:**
```java
@Invoke(..., invokeMethodDesc = "(I)V", shift = Shift.BEFORE)
public static CallbackInfo hook(String param) {  // Type mismatch!
}
```

✅ **Right:**
```java
@Invoke(..., invokeMethodDesc = "(I)V", shift = Shift.BEFORE)
public static CallbackInfo hook(int param) {  // Correct type
}
```

### 3. Modifying Immutable Data

❌ **Wrong:**
```java
@ModifyVariable(methodName = "process", variableIndex = 1)
public static void modify(String str) {
    str = str.toUpperCase();  // Strings are immutable, won't work!
}
```

✅ **Right:**
```java
@Inject(methodName = "process", methodDesc = "(Ljava/lang/String;)V", at = At.HEAD)
public static CallbackInfo modifyByReplacing(String str, CallbackInfo ci) {
    ci.modifyArgs = new Object[]{str.toUpperCase()};
    return ci;
}
```

## Next Steps

- Review [API Reference](./api-reference.md)
- Check out [Examples](./examples.md)
- Join the community and share your patterns
