# Inject Transformation

The `@Inject` annotation allows you to insert custom code at specific points in target methods.

## Basic Usage

```java
@ModifyClass("com.example.Calculator")
public class CalculatorHooks {

    @Inject(
        methodName = "add",
        methodDesc = "(II)I",
        at = At.HEAD
    )
    public static CallbackInfo onAddStart(int a, int b) {
        System.out.println("Adding: " + a + " + " + b);
        return CallbackInfo.empty();
    }
}
```

## Annotation Parameters

### methodName (required)
The name of the target method to inject into.

```java
methodName = "add"
```

### methodDesc (required)
The JVM descriptor of the target method signature.

```java
methodDesc = "(II)I"  // int add(int a, int b)
```

See [Method Descriptors](./bytecode-basics.md#method-descriptors-signatures) for details.

### at (required)
Where to inject the code within the method.

## At Enum - Injection Points

### At.HEAD
Inject at the very beginning of the method, before any existing code.

```java
@Inject(methodName = "calculate", methodDesc = "()I", at = At.HEAD)
public static CallbackInfo atMethodStart() {
    System.out.println("Method started");
    return CallbackInfo.empty();
}
```

**Result:**
```java
public int calculate() {
    System.out.println("Method started");  // Injected
    // Original code here
}
```

### At.RETURN
Inject before every return statement in the method.

```java
@Inject(methodName = "getValue", methodDesc = "()I", at = At.RETURN)
public static CallbackInfo beforeReturn(CallbackInfo ci) {
    System.out.println("Returning: " + ci.returnValue);
    return CallbackInfo.empty();
}
```

**Result:**
```java
public int getValue() {
    if (condition) {
        System.out.println("Returning: " + value);  // Injected
        return value;
    }
    
    System.out.println("Returning: " + defaultValue);  // Injected
    return defaultValue;
}
```

### At.TAIL
Inject at the very end of the method, after all code but before implicit return.

```java
@Inject(methodName = "cleanup", methodDesc = "()V", at = At.TAIL)
public static CallbackInfo atMethodEnd() {
    System.out.println("Cleanup complete");
    return CallbackInfo.empty();
}
```

## Hook Method Parameters

Hook methods receive the same parameters as the target method, plus `CallbackInfo`:

```java
// Target method:
public String process(String input, int count) { ... }

// Hook method:
@Inject(methodName = "process", methodDesc = "(Ljava/lang/String;I)Ljava/lang/String;", at = At.HEAD)
public static CallbackInfo processHook(String input, int count, CallbackInfo ci) {
    // Can access parameters
    return CallbackInfo.empty();
}
```

## CallbackInfo - Controlling Behavior

The `CallbackInfo` object allows you to control how the injection behaves:

```java
public class CallbackInfo {
    public boolean cancelled;      // Skip original code?
    public Object returnValue;     // Return custom value?
}
```

### Cancelling Execution

Skip the original method and return early:

```java
@Inject(methodName = "authenticate", methodDesc = "(Ljava/lang/String;)Z", at = At.HEAD)
public static CallbackInfo checkPermission(String user, CallbackInfo ci) {
    if (!user.equals("admin")) {
        ci.cancelled = true;
        ci.returnValue = false;  // Return false without running original code
    }
    return ci;
}
```

### Custom Return Values

Return a custom value instead of the original result:

```java
@Inject(methodName = "getCached", methodDesc = "()Ljava/lang/Object;", at = At.HEAD)
public static CallbackInfo useCachedValue(CallbackInfo ci) {
    Object cached = getFromCache();
    if (cached != null) {
        ci.cancelled = true;
        ci.returnValue = cached;
    }
    return ci;
}
```

## Multiple Injections

You can inject into the same method multiple times:

```java
@ModifyClass("com.example.Service")
public class ServiceHooks {

    @Inject(methodName = "handle", methodDesc = "(Ljava/lang/String;)V", at = At.HEAD)
    public static CallbackInfo logStart(String input) {
        System.out.println("Start: " + input);
        return CallbackInfo.empty();
    }

    @Inject(methodName = "handle", methodDesc = "(Ljava/lang/String;)V", at = At.RETURN)
    public static CallbackInfo logEnd(String input) {
        System.out.println("End: " + input);
        return CallbackInfo.empty();
    }
}
```

Both injections will be applied.

## Instance Methods vs Static Methods

For instance methods, the first parameter is usually `this` (or the object instance):

```java
// Target instance method:
public class Calculator {
    public int add(int a, int b) { return a + b; }
}

// Hook can receive 'this':
@Inject(methodName = "add", methodDesc = "(II)I", at = At.HEAD)
public static CallbackInfo onAdd(Calculator self, int a, int b) {
    System.out.println("Calculator instance: " + self);
    return CallbackInfo.empty();
}
```

## Best Practices

1. **Keep hooks simple**: Complex logic should be in separate methods
2. **Avoid exceptions**: Handle exceptions within hook methods
3. **Use At.HEAD for guards**: Check conditions early
4. **Be careful with At.RETURN**: Multiple returns need handling
5. **Test thoroughly**: Verify injections work correctly

## Examples

See [Examples - Inject](./examples-basic.md#inject-examples) for complete working examples.

## Next Steps

- Learn about [Invoke](./invoke.md) for method interception
- Explore [Advanced Usage](./advanced-usage.md)
- Check [API Reference](./api-reference.md)
